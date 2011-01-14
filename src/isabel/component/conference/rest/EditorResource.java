package isabel.component.conference.rest;

import java.util.List;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.Session;
import isabel.component.conference.util.ConfigurationParser;

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

public class EditorResource extends ServerResource {
	
	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(EditorResource.class);
	
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

			getResponse().setEntity(
					new JaxbRepresentation<InternalErrorEntity>(
							new InternalErrorEntity().setException(e)));
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Internal Server Error");
			setExisting(false);
		}
	}

	/**
	 * Devuelve el c�digo que hay que empotrar en la p�gina para mostrar el
	 * editor
	 * 
	 * @return
	 */
	@Get
	public StringRepresentation getEditor() {
		Form form = getRequest().getResourceRef().getQueryAsForm();
		Parameter widthParam = form.getFirst("width");
		Parameter heightParam = form.getFirst("height");
		String width = "640";
		String height = "480";
		if (widthParam != null)
			width = widthParam.getValue();
		if (heightParam != null)
			height = heightParam.getValue();
		String metadataURL = ConfigurationParser.metadataPath + "/" + conference.getId() + "/" + 
							conference.getId() + "_" + session.getId() + ".smil";
		String html = "<embed name=\"player\" allowfullscreen=\"true\" src=\""
				+ ConfigurationParser.editorSWF
				+ "?metadataurl="
				+ metadataURL
				+ "\" height=\"" + (new Integer(height) + 20)
				+ "\" wmode=\"transparent\" width=\"" + width + "\" />";
		return new StringRepresentation(html, MediaType.TEXT_HTML);
	}
}
