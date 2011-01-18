package isabel.component.conference.scheduler.jobs;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.Session.Status;
import isabel.component.conference.monitoring.MonitorManager;
import isabel.component.conference.util.ConfigurationParser;
import isabel.component.conference.venus.api.VenusRestController;

import java.net.MalformedURLException;
import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartSessionJob extends SessionJob {
	
	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory.getLogger(StartSessionJob.class);
	
	public StartSessionJob() {
		super();
	}
	
	public StartSessionJob(Conference conference, Session session) {
		super(conference, session);
	}
	
	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("Ejecutando StartSessionJob");
		// Este metodo se ejecuta en la programacion automatica.
		Long id = (Long)context.getJobDetail().getJobDataMap().get("conference");
		conference = ConferenceRegistry.get(id);
		
		if (conference == null) {
			throw new JobExecutionException("Error retrieving conference information");
		}
		
		Long sessionId = (Long)context.getJobDetail().getJobDataMap().get("session");
		Session sessionRes = null;
		for (Session session : conference.getSession()) {
			if (session.getId().equals(sessionId)) {
				sessionRes = session;
			}
		}
		if (sessionRes == null) {
			throw new JobExecutionException("Error retrieving session information");
		}
		session = sessionRes;
		if (sessionRes.isAutomaticRecord()) {
			startSession();
		}

	}
	
	public void startSession() {
		
		// Si hemos llegado tarde no comenzamos a emitir.
		if (session.getStopDate().before(new Date())) {
			log.error("Session "+ session.getName() + " does nothing. Out of time!");
			return;
		}

		// Simplemente inicia la grabacion
		log.info("Session " + session.getName() + " of conference " + conference.getName() + " starts at " + new Date());
		log.info("Conference: " + conference.getName() + "; enabling recording");
		String data = "state: play; streamURL: " + ConfigurationParser.streamURL + "" + conference.getId() + "; streamName: IsabelClient_VIDEO; conferenceName: " + conference.getId() + "; sessionName: " + session.getId();
		log.info("Sending HTTP request to: " + ConfigurationParser.recordURL + " - " + data);
		
		try {
			if (controller == null)
				controller = new VenusRestController(ConfigurationParser.recordURL);
			if (!ConfigurationParser.debug) {
				controller.startRecording(ConfigurationParser.streamURL + conference.getId(), "IsabelClient_VIDEO",""+ conference.getId(),""+ session.getId());
			}
			log.debug("Recording session " + session.getId());
			session.setStatus(Status.RECORDING);
			ConferenceRegistry.updateSession(session.getId(), session);
		} catch (MalformedURLException e) {
			log.info("Error sending REST request" + e.getMessage());
		}
		
		//Iniciamos la monitorizacion.
		MonitorManager.getInstance().startSessionMonitoring(session);
		

	}

}
