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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopSessionJob implements Job {

	/**
	 * Logs.
	 */
	protected static Logger log = LoggerFactory.getLogger(StopSessionJob.class);
	
	private Session session;
	private Conference conference;
	
	public StopSessionJob() {
		super();
	}
	
	public StopSessionJob(Conference conference, Session session) {
		super();
		this.session = session;
		this.conference = conference;
	}

	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
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
			stopSession();
			publishSession();
		}

	}

	public void stopSession() {

		// Simplemente para la grabacion.
		log.info("Session " + session.getName() + " of conference " + conference.getName() + " stops at " + new Date());

		log.info("Conference: " + conference.getName() + "; disabling recording");

		// Paramos la monitorizacion
		MonitorManager.getInstance().stopSessionMonitoring(session);

		String data = "state: stop; streamURL: " + ConfigurationParser.streamURL + "" + conference.getId() +  "; streamName: IsabelClient_VIDEO; conferenceName: " + conference.getId() + "; sessionName: " + session.getId();
		log.info("Sending HTTP request to: " + ConfigurationParser.recordURL + " - " + data);

		try {
			VenusRestController controller = new VenusRestController(ConfigurationParser.recordURL);
			if (!ConfigurationParser.debug) {
				controller.stopRecording(ConfigurationParser.streamURL + conference.getId(), "IsabelClient_VIDEO", ""+conference.getId(), ""+session.getId());
			}
			session.setStatus(Status.RECORDED);
			ConferenceRegistry.updateSession(session.getId(), session);
		} catch (MalformedURLException e) {
			log.error("Error sending REST request" + e.getMessage());
		}
	}
	
	public void publishSession() {
		// Simplemente para la grabacion.
		log.info("Session " + session.getName() + " of conference " + conference.getName() + " publishes at " + new Date());

		log.info("Conference: " + conference.getName() + "; disabling recording");

		String data = "state: publish; streamURL: " + ConfigurationParser.streamURL + "" + conference.getId() +  "; streamName: IsabelClient_VIDEO; conferenceName: " + conference.getId() + "; sessionName: " + session.getId();
		log.info("Sending HTTP request to: " + ConfigurationParser.recordURL + " - " + data);
		try {
			VenusRestController controller = new VenusRestController(ConfigurationParser.recordURL);
			if (!ConfigurationParser.debug) {
				controller.publishRecording(ConfigurationParser.streamURL + conference.getId(), "IsabelClient_VIDEO", ""+conference.getId(), ""+session.getId());
			}
			session.setStatus(Status.PUBLISHED);
			ConferenceRegistry.updateSession(session.getId(), session);
		} catch (MalformedURLException e) {
			log.error("Error sending REST request" + e.getMessage());
		}
		
	}


}
