package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.NotFoundEntity;
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

public class StreamingResource extends ServerResource {

	private Conference conference;

	/**
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		long conferenceID = Long.parseLong((String) getRequest()
				.getAttributes().get("conferenceID"));

		conference = ConferenceRegistry.get(conferenceID);

		if (conference == null) {
			NotFoundEntity notFound = new NotFoundEntity();
			notFound.event_not_found = (String) getRequestAttributes().get(
					"conferenceID");
			getResponse().setEntity(
					new JaxbRepresentation<NotFoundEntity>(notFound));
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Not Found");
		}
	}

	/**
	 * Devuelve el codigo que hay que empotrar en la pagina para mostrar el
	 * reproductor.
	 * 
	 * @return
	 */
	@Get
	public StringRepresentation getPlayer() {
		Form form = getRequest().getResourceRef().getQueryAsForm();
		Parameter widthParam = form.getFirst("width");
		Parameter heightParam = form.getFirst("height");
		Parameter typeParam = form.getFirst("type");
		String width = "640";
		String height = "480";
		String type = "flash";
		if (widthParam != null)
			width = widthParam.getValue();
		if (heightParam != null)
			height = heightParam.getValue();
		if (typeParam != null)
			type = typeParam.getValue();
		/*String html = "<embed name=\"player\" allowfullscreen=\"true\" src=\""
				+ ConfigurationParser.streamSWF
				+ "?id=IsabelClient_VIDEO&searchbar=false&displayheight="
				+ height + "&displaywidth=" + width
				+ "&autostart=true&bufferlength=3&file="
				+ ConfigurationParser.streamURL + conference.getId()
				+ "\" height=\"" + (new Integer(height) + 20)
				+ "\" width=\"" + width + "\" />";
				*/
		String html = "";
		if (type.equalsIgnoreCase("flash")) {
			html = "<embed name=\"player\" allowfullscreen=\"true\" src=\""
				+ ConfigurationParser.webSWF + "?session=" + conference.getId()
				+ "&type=streaming\" height=\"" + height 
				+ "\" width=\""
				+ width + "\" />";
		} else if (type.equalsIgnoreCase("ipad")) {
			html = "<video src=\"" + ConfigurationParser.igwURL + conference.getId() + ".m3u8\" controls autoplay  width=\""
				+ width + "\" height=\"" + height 
				+ "\" />";
		}
		return new StringRepresentation(html, MediaType.TEXT_HTML);
	}
}
