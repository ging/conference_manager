package isabel.component.conference;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.UnprocessableEntity;
import isabel.component.conference.persistence.HibernateUtil;
import isabel.component.conference.util.ConfigurationParser;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author jcervino@dit.upm.es
 *
 */
public class ConferenceRegistry {

	/**
	 * Devuelve la conferencia a partir del identificador.
	 * 
	 * @param id 
	 * 		Identificador de la conferencia.
	 * 
	 * @return
	 *		Conferencia que quer�amos obtener.
	 */
	public static Conference get(long id) {
		Conference conference = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			conference = (Conference) session.get(Conference.class, id);
			conference.getSession();
			for (IsabelMachine machine : conference.getIsabelMachines()) {
				machine.getConference();
			}
		} catch (Exception e) {

		} finally {
			session.getTransaction().commit();
		}
		return conference;
	}

	/**
	 * Incluye una nueva conferencia en la base de datos.
	 * 
	 * @param conference
	 * Conferencia a incluir.
	 */
	public static void add(Conference conference) {
		Session hSession = HibernateUtil.getSessionFactory()
				.getCurrentSession();
		Transaction transaction = hSession.beginTransaction();
		hSession.save(conference);
		if (conference.getEnableIsabel())
			conference.setIsabelURL("isabel://" + conference.getId() + ".session.globalplaza.org");
		if (conference.getEnableSIP())
			conference.setSipURL("sip:" + conference.getId() + "@" + ConfigurationParser.sipRegisterAddress);
		if (conference.getEnableWeb())
			conference.setWebURL("http://" + ConfigurationParser.hostname + ":" + ConfigurationParser.port + "/events/" + conference.getId() + "/web");
		if (conference.getEnableHTTPLiveStreaming())
			conference.setWebURL("http://" + ConfigurationParser.hostname + ":" + ConfigurationParser.port + "/events/" + conference.getId() + "/web");
		hSession.update(conference);
		transaction.commit();
	}

	/**
	 * Elimina la conferencia de la base de datos.
	 * 
	 * @param conference
	 *  Conferencia que queremos eliminar.
	 */
	public static void remove(Conference conference) {
		removeIsabelMachinesFromConference(conference);
		removeSessions(conference);
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Conference conferenceDB = (Conference) session.get(Conference.class,
				conference.getId());
		
		session.delete(conferenceDB);
		session.getTransaction().commit();
	}

	/**
	 * Devuelve una lista de las conferencias que est�n en la base de datos.
	 * 
	 * @return
	 * 
	 * Lista de las conferencias que est�n en la base de datos.
	 */
	@SuppressWarnings("unchecked")
	public static List<Conference> getConferences() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<Conference> preResult = session.createQuery("from Conference")
				.list();
		
		for (Conference conference : preResult) {
			for (IsabelMachine machine : conference.getIsabelMachines()) {
				machine.getConference();
			}
			for (isabel.component.conference.data.Session confSession : conference.getSession()) {
				confSession.getConference();
			}
		}
		
		session.getTransaction().commit();
		return preResult;
	}

	/**
	 * Asigna una maquina de Isabel a una de las conferencia de la base de datos.
	 * 
	 * @param conference
	 * Conferencia en la que queremos incluir la maquina de Isabel.
	 * 
	 * @param machine
	 * Maquina de Isabel que queremos incluir en la conferencia.
	 */
	public static void addIsabelMachineToConference(Conference conference,
			IsabelMachine machine) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		IsabelMachine isabel = (IsabelMachine) session.get(IsabelMachine.class,
				machine.getId());
		Conference conf = (Conference) session.get(Conference.class, conference
				.getId());
		isabel.setConference(conf);
		session.getTransaction().commit();
		
	}
	
	/**
	 * Desasigna una maquina de Isabel de una de las conferencias de la base de datos.
	 * 
	 * @param conference
	 * Conferencia de la que queremos desasignar la maquina de Isabel
	 * 
	 * @param machine
	 * Maquina de Isabel que queremos desasignar.
	 * 
	 */
	public static void removeIsabelMachineFromConference(Conference conference,
			IsabelMachine machine) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Conference conf = (Conference) session.get(Conference.class, conference
				.getId());
		IsabelMachine isabel = (IsabelMachine) session.get(IsabelMachine.class, machine
				.getId());
		conf.removeMachine(isabel);
		session.getTransaction().commit();
	}
	
	
	/**
	 * Libera todas las maquinas de Isabel que estan asignadas a la conferencia.
	 * 
	 * @param conference
	 * Conferencia que tiene asignadas ciertas maquinas de Isabel.
	 */
	public static void removeIsabelMachinesFromConference(Conference conference) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Conference conf = (Conference) session.get(Conference.class, conference
				.getId());
		for (IsabelMachine isabel : conf.getIsabelMachines()) {
			isabel.setConference(null);
		}
		
		session.getTransaction().commit();
		
	}
	
	/**
	 * A�ade una sesion a una conferencia de la base de datos.
	 * 
	 * @param conference
	 * Conferencia de la base de datos.
	 * 
	 * @param confSession
	 * Sesion que queremos a�adir a la base de datos.
	 * 
	 */
	public static void addSessionToConference(Conference conference,
			isabel.component.conference.data.Session confSession) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		session.save(confSession);
		
		Conference conf = (Conference) session.get(Conference.class, conference
				.getId());
		
		confSession.setConference(conf);

		session.getTransaction().commit();
	}

	/**
	 * Actualiza la conferencia dado el ID y la nueva configuracion de la conferencia. Esta actualizacion
	 * no "toca" las maquinas de Isabel y las sesiones.
	 * 
	 * @param conferenceID
	 * ID de la conferencia.
	 * 
	 * @param conference
	 * Configuracion de la conferencia que queremos modificar.
	 * 
	 * @return
	 * Devuelve la nueva conferencia.
	 * 
	 */
	public static Conference updateConference(Long conferenceID,
			Conference conference) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		Conference conf = (Conference) session.get(Conference.class,
				conferenceID);
		conf.setEnableIsabel(conference.getEnableIsabel());
		conf.setEnableSIP(conference.getEnableSIP());
		conf.setEnableWeb(conference.getEnableWeb());
		conf.setEnableHTTPLiveStreaming(conference.getEnableHTTPLiveStreaming());
		
		if (conf.getEnableIsabel())
			conf.setIsabelURL("isabel://" + conf.getId() + ".session.globalplaza.org");
		else
			conf.setIsabelURL(null);
		
		if (conf.getEnableSIP())
			conf.setSipURL("sip:" + conf.getId() + "@" + ConfigurationParser.sipRegisterAddress);
		else
			conf.setSipURL(null);
		
		if (conf.getEnableWeb())
			conf.setWebURL("http://" + ConfigurationParser.hostname + ":" + ConfigurationParser.port + "/events/" + conf.getId() + "/web");
		else
			conf.setWebURL(null);
		
		if (conf.getEnableHTTPLiveStreaming()) 
			conf.setHTTPLiveStreamingURL("http://" + ConfigurationParser.hostname + ":" + ConfigurationParser.port + "/events/" + conf.getId() + "/web");
		else 
			conf.setHTTPLiveStreamingURL(null);
		
		conf.setName(conference.getName());
		conf.setType(conference.getType());
		conf.setHTTPLiveStreamingBW(conference.getHTTPLiveStreamingBW());
		conf.setIsabelBW(conference.getIsabelBW());
		conf.setRecordingBW(conference.getRecordingBW());
		conf.setWebBW(conference.getWebBW());
		conf.setWebCodec(conference.getWebCodec());
		conf.setRecordingCodec(conference.getRecordingCodec());
		conf.setStartTime(conference.getStartTime());
		conf.setStopTime(conference.getStopTime());
		session.update(conf);
		session.getTransaction().commit();
		return conf;
	}

	/**
	 * Actualiza una de las sesiones de la base de datos. No afecta a que conferencia
	 * pertenece ni las maquinas de Isabel.
	 * 
	 * @param id
	 * ID de la sesion.
	 * 
	 * @param newSession
	 * Nueva configuracion de la sesion.
	 * 
	 * @return
	 * Sesion actualizada.
	 * 
	 */
	public static isabel.component.conference.data.Session updateSession(Long id,
			isabel.component.conference.data.Session newSession) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		isabel.component.conference.data.Session confSession = (isabel.component.conference.data.Session) session
				.get(isabel.component.conference.data.Session.class, id);
		confSession.setName(newSession.getName());
		confSession.setAutomaticRecord(newSession.isAutomaticRecord());
		confSession.setEnableStreaming(newSession.getEnableStreaming());
		confSession.setStartTime(newSession.getStartTime());
		confSession.setStopTime(newSession.getStopTime());
		confSession.setStatus(newSession.getStatus());
		session.update(confSession);
		session.getTransaction().commit();
		return confSession;
	}

	/**
	 * Comprueba si una conferencia tiene todos los campos correctos.
	 * 
	 * @param conference
	 * Conferencia que tiene los campos que queremos comprobar.
	 * 
	 * @return
	 * Devuelve un error en caso de que uno de los campos no sean correctos y null si todo
	 * es correcto.
	 */
	public static UnprocessableEntity check(Conference conference) {
		UnprocessableEntity error = null;
		if (conference.getName() == null || conference.getName().equals("")) {
			error = new UnprocessableEntity();
			error.required_attribute = "name";
		}
		return error;
	}

	/**
	 * Comprueba si una sesion tiene todos los campos correctos.
	 * 
	 * @param session
	 * Sesion que tiene los campos que queremos comprobar.
	 * 
	 * @return
	 * Devuelve un error en caso de que uno de los campos no sean correctos y null si todo
	 * es correcto.
	 */
	public static UnprocessableEntity check(
			isabel.component.conference.data.Session session) {
		UnprocessableEntity error = null;
		boolean ok = true;
		String tag = "";
		if (session.getName() == null || session.getName().equals("")) {
			tag = "name";
			ok = true;
		} 
		if (!ok) {
			error = new UnprocessableEntity();
			error.required_attribute = tag;
		}
		return error;
	}

	/**
	 * Elimina una sesion de la base de datos.
	 * 
	 * @param confSession
	 * Sesion que queremos eliminar.
	 */
	public static void removeSession(isabel.component.conference.data.Session confSession) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		isabel.component.conference.data.Session sessionToRemove = (isabel.component.conference.data.Session) session
				.get(isabel.component.conference.data.Session.class, confSession.getId());
		//Conference conf = (Conference) session.get(Conference.class,
		//		conference.getId());
		//conf.getSessions().remove(sessionToRemove);
		session.delete(sessionToRemove);
		session.getTransaction().commit();
	}
	
	/**
	 * Elimina todas las sesiones de una conferencia.
	 * 
	 * @param conference
	 * Conferencia a la que pertenecen las sesiones que queremos eliminar.
	 */
	public static void removeSessions(Conference conference) {
		for (isabel.component.conference.data.Session confSession : conference.getSession()) {
			removeSession(confSession);
		}
	}

	
	

}
