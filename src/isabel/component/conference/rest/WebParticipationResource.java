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
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class WebParticipationResource extends ServerResource {

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
	 * Devuelve el codigo que hay que empotrar en la p‡gina para mostrar el
	 * reproductor.
	 * 
	 * @return
	 */
	@Get("json|html")
	public Representation getPlayer() {
		Form form = getRequest().getResourceRef().getQueryAsForm();
		Parameter widthParam = form.getFirst("width");
		Parameter heightParam = form.getFirst("height");
		String params="";
		for (String name:form.getNames()) {
			if (!name.equals("width") && !name.equals("height")) {
				String value = form.getFirst(name).getValue();
				params+="&" + name.trim() + "=" + value.trim() + "";
			}
		}
		
		String width = "640";
		String height = "480";
		
		if (widthParam != null)
			width = widthParam.getValue();
		
		if (heightParam != null)
			height = heightParam.getValue();
		
		String html = "<embed name=\"player\" allowfullscreen=\"true\" src=\""
				+ ConfigurationParser.webSWF + "?session=" + conference.getId()
				+ params + "\" height=\"" + height 
				+ "\" wmode=\"transparent\" width=\""
				+ width + "\" />";
		
		setStatus(Status.SUCCESS_OK, "OK");
		return new StringRepresentation(html, MediaType.TEXT_HTML);
	}
}
