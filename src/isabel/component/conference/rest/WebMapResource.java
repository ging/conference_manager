package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.util.ConfigurationParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebMapResource extends ServerResource {

	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(WebMapResource.class);
	
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
	 * Devuelve el codigo que hay que empotrar en la p�gina para mostrar el
	 * reproductor.
	 * 
	 * @return
	 */
	@Get("html")
	public Representation getMap() {
		try {
			
			String rtmpUrl=ConfigurationParser.webURL+"map.jsp?session="+conference.getId();
			String url = rtmpUrl.replace("rtmp://", "http://");
			
			InputStream is = new URL(url).openStream();		
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			byte[] data = new byte[1024];
			int i = 0;
			
			while ((i = is.read(data,0,1024)) != -1) {
				baos.write(data,0,i);
			}
			baos.flush();
			String html = new String(baos.toByteArray());
		
			return new StringRepresentation(html, MediaType.TEXT_HTML);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			NotFoundEntity notFound = new NotFoundEntity();
			notFound.event_not_found = (String) getRequestAttributes().get(
					"conferenceID");
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Not Found");
			return	new JaxbRepresentation<NotFoundEntity>(notFound);
		} catch (IOException e) {
			NotFoundEntity notFound = new NotFoundEntity();
			notFound.event_not_found = (String) getRequestAttributes().get(
					"conferenceID");
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Not Found");
			return new JaxbRepresentation<NotFoundEntity>(notFound);
		}
	}
}
