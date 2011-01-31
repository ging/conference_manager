package isabel.component.conference.scheduler;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.Session;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Isabel events scheduler. It is responsible for scheduling and mantaining start and stop
 * events consistency for conference as well as sessions.
 * 
 * The Isabel event scheduling is persistent because it stores all the information in 
 * a data base that it query each time that the application starts. For doing this Isabel
 * Scheduler uses the Java library named Quartz.
 *  
 * @author jcervino@dit.upm.es
 *
 */
public class IsabelScheduler {

	/**
	 * Application logs.
	 */
	protected static Logger log = LoggerFactory
			.getLogger(IsabelScheduler.class);

	/**
	 * Scheduler instance. Used for doing Singleton.
	 */
	private static IsabelScheduler isScheduler;
	
	/**
	 * Jobs scheduler.
	 */
	private JobScheduler jobScheduler;
	
	/**
	 * Private constructor of the scheduler.
	 */
	private IsabelScheduler() {
		jobScheduler = new JobScheduler();
	}
	
	/**
	 * Retrieve the job scheduler.
	 * 
	 * @return
	 * Returns the Scheduler of start/stop jobs.
	 */
	public JobScheduler getJobScheduler() {
		return jobScheduler;
	}

	/**
	 * Inits the Isabel Scheduler.
	 */
	public void start() {
		jobScheduler.start();
	}

	/**
	 * Stops the Isabel Scheduler actions.
	 */
	public void stop() {
		jobScheduler.stop();
	}

	/**
	 * Returns or creates a singleton instance of the Isabel Scheduler.
	 * 
	 * @return The single instance of the scheduler.
	 */
	public static IsabelScheduler getInstance() {
		if (isScheduler == null)
			isScheduler = new IsabelScheduler();
		return isScheduler;
	}
	
	/**
	 * It schedules a conference.
	 * 
	 * @param conference
	 * Conference information to use in the initial scheduling.
	 * 
	 * @return
	 * Response with the result of the scheduling.
	 */
	public synchronized SchedulerResponse scheduleConference(Conference conference) {
		
		log.debug("Scheduling conference " + conference.getName());
		
		Date startDateTime = conference.getResourcesStartTime();
		Date stopDateTime = conference.getResourcesStopTime();
		
		if (!startDateTime.after(new Date())) {
			return SchedulerResponse.error("Conference request has and old start date");
		} else if (!conference.getStartTime().before(conference.getStopTime())) {
			// La conferencia ha terminado.
			String errorMessage = "Start time after stop time";
			log.error(errorMessage);
			
			return SchedulerResponse.error(errorMessage);
		}
		
		
		if (IsabelMachineRegistry.checkAvailability(startDateTime, stopDateTime, conference)) {
			ConferenceRegistry.add(conference);
			jobScheduler.scheduleConferenceJobs(conference);
			return SchedulerResponse.ok();	
		} else {
			return SchedulerResponse.error("There are not available resources for this conference");
		}
		
	}

	/**
	 * Reschedule an existent conference.
	 * 
	 * @param newConference
	 * New conference information.
	 * 
	 * @param conference
	 * Old conference information, that should contains the ID of the conference.
	 * 
	 * @return
	 * Response with the result of the rescheduling.
	 */
	public synchronized SchedulerResponse rescheduleConference(Conference newConference, Long conferenceID) {
		
		log.debug("Rescheduling conference " + newConference.getName());
		
		Conference conference = ConferenceRegistry.get(conferenceID);
		
		Date startDateTime = conference.getResourcesStartTime();
		Date stopDateTime = conference.getResourcesStopTime();
		
		String errorMessage = "";
		
		if (!newConference.getStartTime().before(newConference.getStopTime())) {
			errorMessage = "Start time after stop time";
			return SchedulerResponse.error(errorMessage);
		} else if (conference.getResourcesStopTime().before(new Date())) {
			errorMessage = "Old conference";
			return SchedulerResponse.error(errorMessage);
		} else if (conference.getResourcesStartTime().before(new Date()) &&
				(!newConference.getResourcesStartTime().equals(conference.getResourcesStartTime()))	
			) {
			errorMessage = "Cannot change start date from a conference that is running";
			return SchedulerResponse.error(errorMessage);
		} else if (conference.getResourcesStartTime().after(new Date()) &&
				newConference.getResourcesStartTime().before(new Date())) {
			errorMessage = "Start date is before now";
			return SchedulerResponse.error(errorMessage);
		} else if (conference.getResourcesStopTime().after(new Date()) &&
				newConference.getResourcesStopTime().before(new Date())) {
			errorMessage = "Stop date is before now.";
			return SchedulerResponse.error(errorMessage);
		} else if (Math.abs(startDateTime.compareTo(newConference.getResourcesStartTime())) > 0 || 
				Math.abs(stopDateTime.compareTo(newConference.getResourcesStopTime())) > 0) {
			// Comprobamos si las sesiones terminarian mas tarde que al conferencia.
			List<Session> sessions = conference.getSession();
			for (Session session:sessions) {
				Date stopDate = new Date(newConference.getStartTime().getTime() + session.getStopTime());
				if (stopDate.after(newConference.getStopTime())) {
					errorMessage = "Error rescheduling conference " + conference.getId() + ": the conference ends before the last session";
					return SchedulerResponse.error(errorMessage);
				}
			}
		}
		
		// Comprobamos si se puede cambiar de fecha la conferencia.
		if (IsabelMachineRegistry.checkAvailability(newConference.getResourcesStartTime(), newConference.getResourcesStopTime(), conference)) {
			
			// Si se puede. Guardamos la conferencia en la base de datos.
			Conference result = ConferenceRegistry.updateConference(conference.getId(), newConference);
			List<Session> sessions = ConferenceRegistry.get(result.getId()).getSession();
			
			// Comprobamos si cambian las fechas de inicio y fin.
			if (!startDateTime.equals(newConference.getResourcesStartTime())) {
				
				// Vamos sesion a sesion cambiando los tiempos de inicio y fin y las correspondientes tareas.
				for (Session session: sessions) {
					jobScheduler.rescheduleSessionJobs(session);
				}
			}

			jobScheduler.rescheduleConferenceJobs(result);
			return SchedulerResponse.ok();
		} else {
			return SchedulerResponse.error("There are not available resources for these dates.");
		}
	}

	/**
	 * It removes a scheduled conference.
	 * 
	 * @param conference
	 * Conference information that should have the conference ID.
	 */
	public synchronized void unscheduleConference(Conference conference) {
		for (Session session : conference.getSession()) {
			unscheduleSession(session);
		}
		jobScheduler.unscheduleConferenceJobs(conference);
		ConferenceRegistry.remove(conference);
	}
	
	
	/**
	 * Schedules a new session for the given conference.
	 * 
	 * @param conferenceID
	 * Conference that will own the session.
	 * 
	 * @param session
	 * Session information that is going to be used for scheduling.
	 * 
	 * @return
	 * Scheduler response indicating the result of the scheduling.
	 */
	public synchronized SchedulerResponse scheduleSession(Long conferenceID, Session session) {
		
		Conference conference = ConferenceRegistry.get(conferenceID);
		
		log.debug("Programando la sesion " + session.getName() + " de la conferencia " + conference.getName());
		
		Date nowWithMargin = new Date();
		
		session.setConference(conference);
		String errorMessage = "";
		if (session.getStartDate().before(nowWithMargin)
				|| session.getStopDate().before(nowWithMargin)
				|| !session.getStartDate().before(session.getStopDate())) {
			errorMessage = "Session is already initialized: " + session.getStartDate() + " - " + session.getStopDate() + " - " + nowWithMargin;
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		}
		
		if (conference.getSession().size() > 0 && conference.getResourcesStopTime().before(new Date())) {
			errorMessage = "Conference is ended. Impossible to add new session.";
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		}
		
		Date startDate = session.getStartDate();
		Date stopDate = session.getStopDate();
		
		if (startDate.before(conference.getStartTime()) ||
				stopDate.after(conference.getStopTime())) {
			errorMessage = "Session is out of the conference margins";
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		}
		
		// Hay que guardarla en la base de datos.
		ConferenceRegistry.addSessionToConference(conference, session);

		// Programamos las tareas.
		jobScheduler.scheduleSessionJobs(session);
				
		return SchedulerResponse.ok();
	}

	/**
	 * Reschedules an existent session. The old session must have a conference assigned.
	 * 
	 * @param oldSession
	 * Old configuration of the session.
	 * 
	 * @param newSession
	 * New configuration of the session.
	 * 
	 * @return
	 * Scheduler response with the result of the rescheduling.
	 */
	public synchronized SchedulerResponse rescheduleSession(Long sessionID, Session newSession) {

		Session oldSession = ConferenceRegistry.getSession(sessionID);
		
		Conference conference = oldSession.getConference();
		conference = ConferenceRegistry.get(conference.getId());
		
		log.debug("Reprogramando la sesion " + oldSession.getName() + " de la conferencia " + conference.getName());
		
		Date nowWithMargin = new Date();
		
		String errorMessage = "";
		
		if (newSession.getStartDate().after(newSession.getStopDate())) {
			errorMessage = "Wrong dates"; 
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (oldSession.getStopDate().before(new Date())) {
			errorMessage = "Old session"; 
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (newSession.getStopDate().before(new Date())) {
			errorMessage = "New stop date is before now"; 
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (oldSession.getStartDate().before(new Date()) && 
					!oldSession.getStartDate().equals(newSession.getStartDate())) {
			errorMessage = "Cannot change start date from a running session"; 
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (oldSession.getStartDate().after(new Date()) &&
				newSession.getStartDate().before(new Date())) {
			errorMessage = "New start date is before now"; 
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (oldSession.getStartDate().before(nowWithMargin) && newSession.getStartDate().after(nowWithMargin)) {
			errorMessage = "Session previously started: " + oldSession.getStartDate() + " - " + newSession.getStartDate(); 
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (oldSession.getStartDate().after(nowWithMargin) && newSession.getStartDate().before(nowWithMargin)) {
			errorMessage = "Sesion not initialized: " + oldSession.getStartDate() + " - " + newSession.getStartDate(); 
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (oldSession.getStopDate().before(nowWithMargin) && newSession.getStopDate().after(nowWithMargin)) {
			errorMessage = "Sesion is previously finalized: " + oldSession.getStopDate() + " - " + newSession.getStopDate();
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		} else if (oldSession.getStopDate().after(nowWithMargin) && newSession.getStopDate().before(nowWithMargin)) {
			errorMessage = "Sesion not finalized: " + oldSession.getStopDate() + " - " + newSession.getStopDate();
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		}
		
		Date startDate = newSession.getStartDate();
		Date stopDate = newSession.getStopDate();
		
		if (startDate.before(conference.getStartTime()) ||
				stopDate.after(conference.getStopTime())) {
			errorMessage = "Session is out of the conference margins";
			log.debug(errorMessage);
			return SchedulerResponse.error(errorMessage);
		}
		
		log.debug("Old dates: " + oldSession.getStartDate() + " - " + oldSession.getStopDate());
		log.debug("New dates: " + newSession.getStartDate() + " - " + newSession.getStopDate());
		
		// We update the session.
		newSession = ConferenceRegistry.updateSession(oldSession.getId(),
				newSession);
		
		// Get the updated conference (important!)
		conference = ConferenceRegistry.get(conference.getId());
		newSession.setConference(conference);
		
		// Change the session jobs.
		if (newSession.getStartDate().after(new Date())) {
			jobScheduler.unscheduleStartSessionJob(oldSession);
			jobScheduler.scheduleStartSessionJob(newSession);
		} 
		if (newSession.getStopDate().after(new Date())) {
			jobScheduler.unscheduleStopSessionJob(oldSession);
			jobScheduler.scheduleStopSessionJob(newSession);
		}
		
		log.debug("Session " + newSession.getName() + " correctly rescheduled");
		
		return SchedulerResponse.ok();
	}
	
	/**
	 * Removes a scheduled session in a given conference.
	 * 
	 * @param session
	 * Session that we want to remove.
	 */
	public synchronized void unscheduleSession(Session session) {
		ConferenceRegistry.removeSession(session);
		jobScheduler.unscheduleSessionJobs(session);
	}
}
