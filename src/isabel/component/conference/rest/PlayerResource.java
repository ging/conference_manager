package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.Session;
import isabel.component.conference.util.ConfigurationParser;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerResource extends ServerResource {

	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(PlayerResource.class);
	
	private Conference conference;
	private Session session;

	/**
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		try {
			long conferenceID = Long.parseLong((String) getRequest()
					.getAttributes().get("conferenceID"));

			conference = ConferenceRegistry.get(conferenceID);

			if (getRequestAttributes().get("sessionID") != null) {
				long sessionID = Long.parseLong((String) getRequest()
						.getAttributes().get("sessionID"));
				if (conference != null) {
					List<Session> sessions = conference.getSession();
					for (Session session : sessions) {
						if (session.getId() == sessionID) {
							this.session = session;
							getRequestAttributes().put("session", session);
							break;
						}
					}
				}

				if (this.session == null) {
					NotFoundEntity notFound = new NotFoundEntity();
					notFound.session_not_found = (String) getRequestAttributes()
							.get("sessionID");
					if (conference == null) {
						notFound.event_not_found = (String) getRequestAttributes()
								.get("conferenceID");
					}
					getResponse().setEntity(
							new JaxbRepresentation<NotFoundEntity>(notFound));
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
							"Not Found");
				}
				setExisting(session != null);
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			getResponse().setEntity(
					new JaxbRepresentation<InternalErrorEntity>(
							new InternalErrorEntity().setException(e)));
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Internal Server Error");
			setExisting(false);
		}
	}

	/**
	 * Devuelve el codigo que hay que empotrar en la p�gina para mostrar el
	 * reproductor.
	 * 
	 * @return
	 */
	@Get
	public StringRepresentation getPlayer() {
		Form form = getRequest().getResourceRef().getQueryAsForm();
		Parameter widthParam = form.getFirst("width");
		Parameter heightParam = form.getFirst("height");
		String width = "640";
		String height = "480";
		if (widthParam != null)
			width = widthParam.getValue();
		if (heightParam != null)
			height = heightParam.getValue();
 
		String filename = conference.getId() + "_" + session.getId();
		
//		String html = "<object data='TinySmil_3_0_Player51.swf' type='application/x-shockwave-flash' width='"+width+"' height='"+height+"' >"
//				+ "<param name='movie' value='TinySmil_3_0_Player51.swf'/>"
//				+ "<param name='FlashVars' value='streamer=rtmp://stream4.dit.upm.es/isabelStore/"+filename
//				+ "&file=mp4:" + filename + ".f4v&resizing=true&allowfullscreen=true'/>"
//				+ "<param name='quality' value='high'/>"
//				+ "<param name='bgcolor' value='#000000'/>"
//				+ "<param name='wmode' value='transparent'/>"
//				+ "<param name='allowfullscreen' value='true'/>"
//				+ "</object>";
		String html = "";
		try {
			if (conference.getStartTime().before(DateFormat.getDateInstance(DateFormat.SHORT).parse("11/25/2010"))) {
				log.info("Showing old video player " + conference.getStartTime() + " -->" + DateFormat.getDateInstance(DateFormat.SHORT).parse("11/25/2010"));
				html += "<embed name=\"player\" allowfullscreen=\"true\" src=\""
					+ ConfigurationParser.recordSWF + "?streamer=" + ConfigurationParser.recordRTMP + conference.getId()+"&file=mp4:" + filename + ".mp4&resizing=true&allowfullscreen=true\" height=\"" + height 
					+ "\" wmode=\"transparent\" width=\""
					+ width + "\" />";
			} else {
				log.info("Showing new video player " + conference.getStartTime() + " -->" + DateFormat.getDateInstance(DateFormat.SHORT).parse("11/25/2010"));
				String metadataURL = ConfigurationParser.metadataPath + "/" + conference.getId() + "/" + conference.getId() + "_" + session.getId() + ".xml";
				html+="<embed name=\"player\" allowfullscreen=\"true\" src=\""
					+ ConfigurationParser.recordSWF + "?file=" + metadataURL + "&resizing=true&allowfullscreen=true\" height=\"" + height 
					+ "\" wmode=\"transparent\" width=\""
					+ width + "\" />";
			}
		} catch (ParseException e) {
			log.error("Error parsing date");
		} catch (Exception e) {
			log.error("Error on player " + e.getLocalizedMessage());
		}
		return new StringRepresentation(html, MediaType.TEXT_HTML);
	}
}
