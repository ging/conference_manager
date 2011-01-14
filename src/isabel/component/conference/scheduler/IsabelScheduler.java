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
 * Programador de eventos de Isabel. Se encarga de programar y mantener la consistencia
 * de las programaciones de eventos de inicio y parada de conferencias de Isabel al igual que
 * eventos de inicio y parada de sesiones de esas conferencias.
 * 
 * La programacion de estos eventos es persistente ya que se guarda en una base de datos que 
 * se consulta cada vez que se inicia la aplicaci�n. Esto se debe a la utilizacion de la 
 * biblioteca Quartz de Java.
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class IsabelScheduler {

	/**
	 * Logs de la aplicacion
	 */
	protected static Logger log = LoggerFactory
			.getLogger(IsabelScheduler.class);

	/**
	 * Instancia del programador. Utilizado para hacer Singleton.
	 */
	private static IsabelScheduler isScheduler;
	
	/**
	 * Programador de trabajos
	 */
	private JobScheduler jobScheduler;
	
	/**
	 * Constructor privado del programador.
	 */
	private IsabelScheduler() {
		jobScheduler = new JobScheduler();
	}
	
	public JobScheduler getJobScheduler() {
		return jobScheduler;
	}

	/**
	 * Inicia el programador.
	 */
	public void start() {
		jobScheduler.start();
	}

	/**
	 * Para el programador.
	 */
	public void stop() {
		jobScheduler.stop();
	}

	/**
	 * Devuelve una instancia Singleton del programador.
	 * 
	 * @return Unica instancia del programador.
	 */
	public static IsabelScheduler getInstance() {
		if (isScheduler == null)
			isScheduler = new IsabelScheduler();
		return isScheduler;
	}
	
	/**
	 * Programacion inicial de una conferencia.
	 * 
	 * @param conference
	 * Conferencia que se quiere programar para iniciar y para en unas fechas determinadas.
	 * 
	 * @return
	 * Un flag indicando si se ha podido programar la conferencia.
	 */
	public synchronized boolean scheduleConference(Conference conference) {
		
		log.debug("Scheduling conference " + conference.getName());
		
		Date startDateTime = conference.getResourcesStartTime();
		Date stopDateTime = conference.getResourcesStopTime();
		
		if (!startDateTime.after(new Date()))
			return false;
		
		if (IsabelMachineRegistry.checkAvailability(startDateTime, stopDateTime, conference)) {
			ConferenceRegistry.add(conference);
			jobScheduler.scheduleConferenceJobs(conference);
			return true;	
		} else {
			return false;
		}
		
	}

	/**
	 * Vuelve a programar una conferencia.
	 * 
	 * @param newConference
	 * La nueva configuracion de la conferencia.
	 * 
	 * @param conference
	 * La antigua configuracion de la conferencia.
	 * 
	 * @return
	 * Un flag indicando si se ha podido programar o no.
	 */
	public synchronized boolean rescheduleConference(Conference newConference, Conference conference) {
		
		log.debug("Rescheduling conference " + newConference.getName());
		
		Date startDateTime = conference.getResourcesStartTime();
		Date stopDateTime = conference.getResourcesStopTime();
		
		// Comprobamos si las sesiones terminarian mas tarde que al conferencia.
		if (Math.abs(startDateTime.compareTo(newConference.getResourcesStartTime())) > 0 || 
				Math.abs(stopDateTime.compareTo(newConference.getResourcesStopTime())) > 0) {
			List<Session> sessions = conference.getSession();
			for (Session session:sessions) {
				Date stopDate = new Date(newConference.getStartTime().getTime() + session.getStopTime());
				if (stopDate.after(newConference.getStopTime())) {
					log.error("Error rescheduling conference " + conference.getId() + ": the conference ends before the last session");
					return false;
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
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Quita la programacion de la conferencia.
	 * 
	 * @param conference
	 * Programacion que se quiere desprogramar.
	 */
	public synchronized void unscheduleConference(Conference conference) {
		for (Session session : conference.getSession()) {
			unscheduleSession(conference, session);
		}
		jobScheduler.unscheduleConferenceJobs(conference);
		ConferenceRegistry.remove(conference);
	}
	
	
	/**
	 * Programa una nueva sesion en la conferencia dada.
	 * 
	 * @param conference
	 * Conferencia de la que depende la sesion.
	 * 
	 * @param session
	 * Sesion que se quiere programar dentro de la conferencia.
	 * 
	 * @return
	 * Un flag indicando si se ha podido programar la sesion dentro de la conferencia.
	 */
	public synchronized boolean scheduleSession(Conference conference, Session session) {
		
		log.debug("Programando la sesion " + session.getName() + " de la conferencia " + conference.getName());
		
		conference = ConferenceRegistry.get(conference.getId());
		
		Date nowWithMargin = new Date();
		
		session.setConference(conference);

		if (session.getStartDate().before(nowWithMargin)
				|| session.getStopDate().before(nowWithMargin)
				|| !session.getStartDate().before(session.getStopDate())) {
			log.debug("Sesion ya iniciada: " + session.getStartDate() + " - " + session.getStopDate() + " - " + nowWithMargin);
			return false;
		}
		
		if (conference.getSession().size() > 0 && conference.getResourcesStopTime().before(new Date())) {
			log.debug("Conferencia terminada. No se puede a�adir la nueva sesi�n");
			return false;
		}
		
		Date startDate = session.getStartDate();
		Date stopDate = session.getStopDate();
		
		if (startDate.before(conference.getStartTime()) ||
				stopDate.after(conference.getStopTime())) {
			log.debug("La sesion se sale de los margenes de la conferencia");
			return false;
		}
		
		// Hay que guardarla en la base de datos.
		ConferenceRegistry.addSessionToConference(conference, session);

		// Programamos las tareas.
		jobScheduler.scheduleSessionJobs(session);
				
		return true;
	}

	/**
	 * Reprograma la nueva sesion. La sesion antigua debe tener una conferencia asignada.
	 * 
	 * @param oldSession
	 * Antigua configuracion de la sesion.
	 * 
	 * @param newSession
	 * Nueva configuracion de la sesion.
	 * 
	 * @return
	 * Un flag indicando si se ha podido reprogramar la sesion o no.
	 */
	public synchronized boolean rescheduleSession(Session oldSession, Session newSession) {

		Conference conference = oldSession.getConference();
		conference = ConferenceRegistry.get(conference.getId());
		
		log.debug("Reprogramando la sesion " + oldSession.getName() + " de la conferencia " + conference.getName());
		
		Date nowWithMargin = new Date();
		
		if (oldSession.getStartDate().before(nowWithMargin) && newSession.getStartDate().after(nowWithMargin)) {
			log.debug("Sesion previamente iniciada: " + oldSession.getStartDate() + " - " + newSession.getStartDate());
			return false;
		}
		
		if (oldSession.getStartDate().after(nowWithMargin) && newSession.getStartDate().before(nowWithMargin)) {
			log.debug("Sesion no iniciada: " + oldSession.getStartDate() + " - " + newSession.getStartDate());
			return false;
		}
		
		if (oldSession.getStopDate().before(nowWithMargin) && newSession.getStopDate().after(nowWithMargin)) {
			log.debug("Sesion previamente finalizada: " + oldSession.getStopDate() + " - " + newSession.getStopDate());
			return false;
		}
		
		if (oldSession.getStopDate().after(nowWithMargin) && newSession.getStopDate().before(nowWithMargin)) {
			log.debug("Sesion no finalizada: " + oldSession.getStopDate() + " - " + newSession.getStopDate());
			return false;
		}
		
		Date startDate = newSession.getStartDate();
		Date stopDate = newSession.getStopDate();
		
		if (startDate.before(conference.getStartTime()) ||
				stopDate.after(conference.getStopTime())) {
			log.debug("La sesion se sale de los margenes de la conferencia");
			return false;
		}
		
		log.debug("Old dates: " + oldSession.getStartDate() + " - " + oldSession.getStopDate());
		log.debug("New dates: " + newSession.getStartDate() + " - " + newSession.getStopDate());
		
		// Actualizamos la sesion.
		newSession = ConferenceRegistry.updateSession(oldSession.getId(),
				newSession);
		
		// Obtenemos la version actualizada (muy importante)
		conference = ConferenceRegistry.get(conference.getId());
		newSession.setConference(conference);
		
		// Cambiamos las tareas de la sesi�n.
		if (newSession.getStartDate().after(new Date())) {
			jobScheduler.unscheduleStartSessionJob(oldSession);
			jobScheduler.scheduleStartSessionJob(newSession);
		} 
		if (newSession.getStopDate().after(new Date())) {
			jobScheduler.unscheduleStopSessionJob(oldSession);
			jobScheduler.scheduleStopSessionJob(newSession);
		}
		
		log.debug("Sesion " + newSession.getName() + " reprogramada correctamente");
		
		return true;
	}
	
	/**
	 * Elimina una sesion programada en una conferencia dada.
	 * 
	 * @param conference
	 * Conferencia en la cual esta la sesion que queremos eliminar.
	 * 
	 * @param session
	 * Sesion que queremos eliminar.
	 */
	public synchronized void unscheduleSession(Conference conference, Session session) {
		ConferenceRegistry.removeSession(session);
		jobScheduler.unscheduleSessionJobs(session);
	}
}
