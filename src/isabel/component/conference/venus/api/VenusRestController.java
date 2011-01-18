package isabel.component.conference.venus.api;

import isabel.component.conference.scheduler.jobs.Controller;
import isabel.component.conference.venus.api.generated.RecordingType;
import isabel.component.conference.venus.api.generated.StateType;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;


/**
 * Clase que controla las grabaciones en Venus 
 * mediante llamadas REST
 *  
 * @author pedro
 *
 */
public class VenusRestController implements Controller {
	
	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory.getLogger(VenusRestController.class);
	
	private String venusServer;
	
	/**
	 * Constructor
	 * @param server Servidor web al que mandar las llamadas REST;
	 */
	public VenusRestController(String server) throws MalformedURLException{
		new URL(server);
		this.venusServer = server;
	}
	
	/**
	 * Empieza la grabacion en Venus
	 * @param streamURL URL del servidor RTMP incluyendo la aplicacion en la que se publica el video
	 * @param streamName Nombre con el que se ha publicado el video
	 * @param conference Conferencia a grabar
	 * @param session  sesion a grabar
	 * @return true si todo ha ido bien
	 */
	public boolean startRecording(String streamURL, String streamName, String conference, String session) {
		try {
			Client c = Client.create();
			WebResource lista = c.resource(venusServer); 
			RecordingType rec = new RecordingType();
			
			rec.setConferenceName(conference);
			rec.setSessionName(session);
			rec.setState(StateType.PLAY);
			rec.setStreamName(streamName);
			rec.setStreamURL(streamURL);
			log.debug("Sending http request: " + rec);
			String s = lista.accept(MediaType.APPLICATION_XML_TYPE).type(MediaType.APPLICATION_XML_TYPE).put(String.class,rec);			
			log.debug("Response : " + s);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return true;
	}
	
	
	/**
	 * Para la grabacion en Venus
	 * @param streamURL URL del servidor RTMP incluyendo la aplicacion en la que se publica el video
	 * @param streamName Nombre con el que se ha publicado el video
	 * @param conference Conferencia a grabar
	 * @param session  sesion a grabar
	 * @return true si todo ha ido bien
	 */
	public boolean stopRecording(String streamURL, String streamName, String conference, String session) {
		Client c = Client.create();
		
		WebResource lista = c.resource(venusServer); 
		RecordingType rec = new RecordingType();
		
		
		rec.setConferenceName(conference);
		rec.setSessionName(session);
		rec.setState(StateType.STOP);
		rec.setStreamName(streamName);
		rec.setStreamURL(streamURL);
		log.debug("Sending http request: " + rec);
		String s = lista.accept(MediaType.APPLICATION_XML_TYPE).type(MediaType.APPLICATION_XML_TYPE).put(String.class,rec);			
		log.debug("Response : " + s);
		return true;
	}
	
	/**
	 * Publica la grabacion en Venus
	 * @param streamURL URL del servidor RTMP incluyendo la aplicacion en la que se publica el video
	 * @param streamName Nombre con el que se ha publicado el video
	 * @param conference Conferencia a grabar
	 * @param session  sesion a grabar
	 * @return true si todo ha ido bien
	 */
	public boolean publishRecording(String streamURL, String streamName, String conference, String session) {
		Client c = Client.create();
		
		WebResource lista = c.resource(venusServer); 
		RecordingType rec = new RecordingType();
		
		
		rec.setConferenceName(conference);
		rec.setSessionName(session);
		rec.setState(StateType.PUBLISH);
		rec.setStreamName(streamName);
		rec.setStreamURL(streamURL);
		log.debug("Sending http request: " + rec);
		
		String s = lista.accept(MediaType.APPLICATION_XML_TYPE).type(MediaType.APPLICATION_XML_TYPE).put(String.class,rec);			
		log.debug("Response : " + s);
		return true;
	}
	
	/**
	 * Devuelve el estado de la grabacion especificada
	 * @param conference Conferencia sobre la que se quiere preguntar
	 * @param session Sesion a consultar
	 * @return true si esta grabando actualmente
	 */
	public boolean isRecording (String conference, String session){
		log.debug("IsRecording");
		Client c = Client.create();			
		WebResource lista = c.resource(this.venusServer+"/?conferenceName="+conference+"&sessionName="+session);
		RecordingType s = new RecordingType(); 
		s = lista.accept(MediaType.APPLICATION_XML_TYPE).get(RecordingType.class);			
		log.debug(s.getConferenceName());
		log.debug(s.getSessionName());
		log.debug(s.getStreamName());
		log.debug(s.getStreamURL());
		log.debug(s.getState()+"");
		return (s.getState()==StateType.PLAY);
	}
	
	public static void main (String[] args){
		String streamURL = "rtmp://stream4.dit.upm.es/isabelStore/test/";
		String streamName = "IsabelClient_VIDEO";
		String conference = "test";
		String session = "test5";
		try {
			VenusRestController venus = new VenusRestController("http://stream4.dit.upm.es:8080/rest");
			venus.startRecording(streamURL, streamName, conference, session);
			Thread.sleep(10*1000);
			venus.stopRecording(streamURL, streamName, conference, session);
			venus.publishRecording(streamURL, streamName, conference, session);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
