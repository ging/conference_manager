package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.util.ConfigurationParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

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

public class WebStatsResource extends ServerResource {

	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(WebStatsResource.class);
	
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
			Calendar start = Calendar.getInstance();
			start.setTime(conference.getStartTime());
			
			Calendar stop = Calendar.getInstance();
			stop.setTime(conference.getStopTime());
			
			int initDay = start.get(Calendar.DAY_OF_MONTH);
			int endDay = start.get(Calendar.DAY_OF_MONTH);
			String html = "";
			
			String rtmpUrl = 
				ConfigurationParser.webURL + 
				"generalStats.jsp?session=" + conference.getId();
			String url = rtmpUrl.replace("rtmp://", "http://");
			
			html+=getHtml(url);
			
			rtmpUrl = 
				ConfigurationParser.webURL + 
				"stats.jsp?session=" + conference.getId() + 
				"&date=";
			url = rtmpUrl.replace("rtmp://", "http://");
			
			for (int day = initDay; day<=endDay; day++) {
				html+= getHtml(url+day);
			}
			return new StringRepresentation(html, MediaType.TEXT_HTML);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			InternalErrorEntity error = new InternalErrorEntity();
			error.message = "MalformedURLException in WebStatsResource";
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Internal server error");
			return	new JaxbRepresentation<InternalErrorEntity>(error);
		} catch (IOException e) {
			InternalErrorEntity error = new InternalErrorEntity();
			error.message = "Error while getting stats";
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Internal server error");
			return	new JaxbRepresentation<InternalErrorEntity>(error);
		}
	}
	
	public String getHtml(String url) throws MalformedURLException, IOException {
		
		InputStream is = new URL(url).openStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] data = new byte[1024];
		int i = 0;
		
		while ((i = is.read(data,0,1024)) != -1) {
			baos.write(data,0,i);
		}
		baos.flush();
		String html = new String(baos.toByteArray());
		return html;
	}
	
}
