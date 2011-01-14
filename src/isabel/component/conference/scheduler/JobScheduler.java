package isabel.component.conference.scheduler;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.Session;
import isabel.component.conference.scheduler.jobs.StartConferenceJob;
import isabel.component.conference.scheduler.jobs.StartSessionJob;
import isabel.component.conference.scheduler.jobs.StopConferenceJob;
import isabel.component.conference.scheduler.jobs.StopSessionJob;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobScheduler {
	
	/**
	 * Logs de la aplicacion
	 */
	protected static Logger log = LoggerFactory
			.getLogger(JobScheduler.class);
	
	public static String configFile = "config/quartz2.properties";
	
	/**
	 * Instancia del programador Quartz
	 */
	private Scheduler scheduler;
	
	public JobScheduler() {
		try {
			StdSchedulerFactory factory = new StdSchedulerFactory(
					configFile);
			log.debug("Init Scheduler");
			
			scheduler = factory.getScheduler();
			
		} catch (SchedulerException e) {
			log.error("Error creating Quartz Scheduler");
			e.printStackTrace();
		}
		
	}
	
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	/**
	 * Inicia el programador.
	 */
	public void start() {
		try {
			scheduler.start();
		} catch (SchedulerException e) {
			log.error("Error starting Quartz Scheduler");
		}
	}

	/**
	 * Para el programador.
	 */
	public void stop() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			log.error("Error stopping Quartz Scheduler");
		}
	}
	/**
	 * Programador de eventos relativos a la conferencia en s�.
	 * 
	 * @param conference
	 *            Conferencia sobre la que queremos programar eventos.
	 */
	public void scheduleConferenceJobs(Conference conference) {
		Date now = new Date();
		
		if (conference.getResourcesStartTime().after(now))
			scheduleStartConferenceJob(conference);
		if (conference.getResourcesStopTime().after(now))
			scheduleStopConferenceJob(conference);
	}
	
	/**
	 * Vuelve a programar los trabajos de una conferencia
	 * 
	 * @param conference
	 */
	public void rescheduleConferenceJobs(Conference conference) {
		unscheduleConferenceJobs(conference);
		scheduleConferenceJobs(conference);
	}

	/**
	 * Programador de eventos de inicio de una conferencia.
	 * 
	 * @param conference
	 *            Conferencia sobre la que queremos programar eventos de inicio.
	 */
	public void scheduleStartConferenceJob(Conference conference) {
		Date startTime = conference.getResourcesStartTime();
		Date oldNow = new Date();
		Date now = new Date(oldNow.getTime() + 5000); // Dentro de 6 segundos
		if (startTime.before(now)) {
			startTime = now;
		}

		scheduleStartConferenceJob(conference, startTime);
	}
	
	public void scheduleStartConferenceJob(Conference conference, Date startTime) {
		JobDetail startJob = new JobDetail(conference.getId() + "confstart",
				"isabel", StartConferenceJob.class, false, false, true);
		Trigger startTrigger = new SimpleTrigger(conference.getId()
				+ "confstart", "isabel", startTime);

		startJob.getJobDataMap().put("conference", conference.getId());

		startTrigger.setVolatility(false);
		startJob.setVolatility(false);

		scheduleJob(startJob, startTrigger);
	}

	/**
	 * Programador de eventos de parada de una conferencia.
	 * 
	 * @param conference
	 *            Conferencia sobre la que queremos programar eventos de parada.
	 */
	public void scheduleStopConferenceJob(Conference conference, Date stopTime, String name) {
		// Creamos las tareas de inicio y parada de la conferencia

		JobDetail stopJob = new JobDetail(name,
				"isabel", StopConferenceJob.class, false, false, true);
		Trigger stopTrigger = new SimpleTrigger(
				name, "isabel", stopTime);

		stopJob.getJobDataMap().put("conference", conference.getId());

		stopTrigger.setVolatility(false);
		stopJob.setVolatility(false);

		// Programamos las tareas.
		scheduleJob(stopJob, stopTrigger);
	}
	
	/**
	 * Programador de eventos de parada de una conferencia.
	 * 
	 * @param conference
	 *            Conferencia sobre la que queremos programar eventos de parada.
	 */
	public void scheduleStopConferenceJob(Conference conference) {
		scheduleStopConferenceJob(conference, conference.getResourcesStopTime(), conference.getId() + "confstop");
	}

	/**
	 * Borra la programaci�n de eventos relativos a una conferencia.
	 * 
	 * @param conference
	 *            Conferencia sobre la que queremos eliminar los eventos
	 *            programados.
	 */
	public void unscheduleConferenceJobs(Conference conference) {
		unscheduleStartConferenceJob(conference);
		unscheduleStopConferenceJob(conference);
	}

	/**
	 * Borra la programaci�n de eventos relativos al inicio de una conferencia.
	 * 
	 * @param conference
	 *            Conferencia sobre la que queremos eliminar los eventos de
	 *            inicio programados.
	 */
	public void unscheduleStartConferenceJob(Conference conference) {
		unscheduleJob(conference.getId() + "confstart", "isabel");
	}

	/**
	 * Borra la programaci�n de eventos relativos a la parada de una
	 * conferencia.
	 * 
	 * @param conference
	 *            Conferencia sobre la que queremos eliminar los eventos de
	 *            parada programados.
	 */
	public void unscheduleStopConferenceJob(Conference conference) {
		unscheduleJob(conference.getId() + "confstop", "isabel");
	}

	/**
	 * Programa los eventos relativos a una sesi�n concreta (tanto de inico como
	 * de parada).
	 * 
	 * @param session
	 *            Sesi�n sobre la que queremos programar eventos de inicio y
	 *            parada.
	 */
	public void scheduleSessionJobs(Session session) {
		scheduleStartSessionJob(session);
		scheduleStopSessionJob(session);
	}
	
	/**
	 * Vuelve a programar los trabajos de una sesion
	 * 
	 * @param conference
	 */
	public void rescheduleSessionJobs(Session session) {
		unscheduleSessionJobs(session);
		scheduleSessionJobs(session);
	}

	/**
	 * Programa los eventos relativos al inicio de una sesion concreta.
	 * Si es un evento antiguo no lo programamos.
	 * @param session
	 *            Sesion sobre la que queremos programar eventos de inicio.
	 */
	public void scheduleStartSessionJob(Session session) {
		Date startTime = session.getStartDate();
		scheduleStartSessionJob(session, startTime);
	}
	
	public void scheduleStartSessionJob(Session session, Date startTime) {
		Conference conference = session.getConference();
		Date now = new Date();
		
		if (startTime.before(now)) {
			return;
		}

		log.debug("Nuevo inicio de sesion para " + session.getName() + " a las " + startTime);
		
		// Creamos las tareas de inicio y parada de la conferencia
		JobDetail startJob = new JobDetail(session.getId() + "sesstart",
				"isabel", StartSessionJob.class, false, false, true);
		Trigger startTrigger = new SimpleTrigger(session.getId() + "sesstart",
				"isabel", startTime);

		startJob.getJobDataMap().put("conference", conference.getId());
		startJob.getJobDataMap().put("session", session.getId());

		startTrigger.setVolatility(false);
		startJob.setVolatility(false);

		// Programamos las tareas.
		scheduleJob(startJob, startTrigger);
	}

	/**
	 * Programa los eventos relativos a la parada de una sesion.
	 * Si es un evento antiguo no lo programamos.
	 * 
	 * @param session
	 *            Sesion sobre la que queremos programar los eventos de parada.
	 */
	public void scheduleStopSessionJob(Session session) {
		Date stopTime = session.getStopDate();
		scheduleStopSessionJob(session, stopTime);
	}
	
	public void scheduleStopSessionJob(Session session, Date stopTime) {
		Conference conference = session.getConference();
		
		Date now = new Date();
		
		if (stopTime.before(now)) {
			return;
		}
		
		JobDetail stopJob = new JobDetail(session.getId() + "sesstop",
				"isabel", StopSessionJob.class, false, false, true);
		Trigger stopTrigger = new SimpleTrigger(session.getId() + "sesstop",
				"isabel", session.getStopDate());

		stopJob.getJobDataMap().put("conference", conference.getId());
		stopJob.getJobDataMap().put("session", session.getId());

		stopTrigger.setVolatility(false);
		stopJob.setVolatility(false);
		scheduleJob(stopJob, stopTrigger);
	}

	/**
	 * Borra la programaci�n de los eventos de inicio y parada de una sesi�n
	 * concreta.
	 * 
	 * @param session
	 *            Sesi�n sobre la que queremos eliminar los eventos de inicio y
	 *            parada.
	 */
	public void unscheduleSessionJobs(Session session) {
		unscheduleStartSessionJob(session);
		unscheduleStopSessionJob(session);
	}

	/**
	 * Borra la programaci�n de eventos de inicio de sesi�n.
	 * 
	 * @param session
	 *            Sesi�n sobre la que queremos eliminar los eventos de inicio.
	 */
	public void unscheduleStartSessionJob(Session session) {
		unscheduleJob(session.getId() + "sesstart", "isabel");
	}

	/**
	 * Borra la programaci�n de eventos de parada de sesi�n.
	 * 
	 * @param session
	 *            Sesi�n sobre la que queremos eliminar los eventos de parada.
	 */
	public void unscheduleStopSessionJob(Session session) {
		unscheduleJob(session.getId() + "sesstop", "isabel");
	}

	private void scheduleJob(JobDetail job, Trigger trigger) {
		try {
			log.info("New Job scheduled: " + job.getName() + " at "
			+ trigger.getStartTime());
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			log.error("Error scheduling jobs" + e.getMessage());
		}
	}

	private void unscheduleJob(String job, String group) {
		try {
			log.info("Job unscheduled: " + job + " in " + group);
			scheduler.unscheduleJob(job, group);
		} catch (SchedulerException e) {
			log.error("Error unscheduling jobs" + e.getMessage());
		}
	}
	
	public void unscheduleAllJobs() throws Exception {
		String[] names = scheduler.getJobNames("isabel");
		for (String name:names) {
			scheduler.unscheduleJob(name, "isabel");
		}
	}
}
