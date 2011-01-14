package isabel.component.conference;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.IsabelMachine.Cloud;
import isabel.component.conference.data.IsabelMachine.Type;
import isabel.component.conference.persistence.HibernateUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.NotFoundException;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jcervino@dit.upm.es
 *
 */
public class IsabelMachineRegistry {

	/**
	 * Logs de la aplicacion
	 */
	protected static Logger log = LoggerFactory
			.getLogger(IsabelMachineRegistry.class);

	/**
	 * A�ade una maquina de Isabel a la base de datos.
	 * 
	 * @param machine
	 * Maquina de Isabel que queremos a�adir.
	 * 
	 */
	public static void addIsabelMachine(IsabelMachine machine) {
		Session hSession = HibernateUtil.getSessionFactory()
				.getCurrentSession();
		Transaction transaction = hSession.beginTransaction();
		hSession.save(machine);
		transaction.commit();
	}

	/**
	 * Elimina una maquina de Isabel de la base de datos.
	 * 
	 * @param machine
	 * Maquina de Isabel que se quiere eliminar.
	 */
	public static void removeIsabelMachine(IsabelMachine machine) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		IsabelMachine machineDB = (IsabelMachine) session.get(
				IsabelMachine.class, machine.getId());
		session.delete(machineDB);
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
	public static IsabelMachine updateIsabelMachine(Long machineID,
			IsabelMachine machine) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		IsabelMachine newMachine = (IsabelMachine) session.get(IsabelMachine.class,
				machineID);
		newMachine.setExternalID(machine.getExternalID());
		newMachine.setHostname(machine.getHostname());
		newMachine.setIsabelNodeType(machine.getIsabelNodeType());
		session.update(newMachine);
		session.getTransaction().commit();
		return newMachine;
	}

	/**
	 * Devuelve las maquinas de Isabel que estan en la base de datos.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<IsabelMachine> getIsabelMachines() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<IsabelMachine> preResult = session.createQuery(
				"from IsabelMachine").list();

		for (IsabelMachine isabel : preResult) {
			isabel.getConference();
		}
		session.getTransaction().commit();
		return preResult;
	}
	
	public static HashMap<Type, List<IsabelMachine>> getIsabelMachinesByType() {
		List<IsabelMachine> machines = getIsabelMachines();
		List<IsabelMachine> isabeles = new ArrayList<IsabelMachine>(); 
		List<IsabelMachine> vncs  = new ArrayList<IsabelMachine>();
		List<IsabelMachine> spies  = new ArrayList<IsabelMachine>();
		HashMap<Type, List<IsabelMachine>> result = new HashMap<Type, List<IsabelMachine>>();
		for (IsabelMachine machine : machines) {
			switch (machine.getType()) {
			case Isabel:
				isabeles.add(machine);
				break;
			case Spy:
				spies.add(machine);
				break;
			case VNC:
				vncs.add(machine);
				break;
			}
		}
		result.put(Type.Isabel, isabeles);
		result.put(Type.Spy, spies);
		result.put(Type.VNC, vncs);
		return result;
	}

	public static HashMap<Type, List<IsabelMachine>> getAvailableIsabelMachinesByType() {
		HashMap<Type, List<IsabelMachine>> result = new HashMap<Type, List<IsabelMachine>>();
		HashMap<Type, List<IsabelMachine>> temp = getIsabelMachinesByType();
		List<IsabelMachine> from;
		List<IsabelMachine> to;
		for (Type type : temp.keySet()) {
			from = temp.get(type);
			to = new ArrayList<IsabelMachine>();
			for (IsabelMachine machine : from) {
				if (machine.getConference() == null) {
					to.add(machine);
				}
			}
			result.put(type, to);
		}
		
		return result;
	}
	
	/**
	 * Consulta las m�quinas de Isabel que necesita la conferencia dada para poder
	 * funcionar correctamente.
	 * 
	 * @param conference
	 * Conferencia de la que queremos saber que tipos de maquinas Isabel necesitamos
	 * y el numero.
	 * 
	 * @return
	 * Devuelve un Map con el tipo de maquinas de Isabel que se necesitan y el numero
	 * de maquinas por cada tipo.
	 */
	public static Map<Type, Integer> getNeededIsabelMachines(
			Conference conference) {
		return getNeededIsabelMachines(conference.getEnableSIP());
	}
	
	/**
	 * 
	 * @param enableSIP
	 * @return
	 */
	public static Map<Type, Integer> getNeededIsabelMachines(boolean enableSIP) {
		int isabelMachines = enableSIP?3:2;
		int spyMachines = 0;
		int vncMachines = 1;

		HashMap<Type, Integer> needed = new HashMap<Type, Integer>();

		needed.put(Type.Isabel, 		isabelMachines);
		needed.put(Type.Spy, 			spyMachines);
		needed.put(Type.VNC, 			vncMachines);

		return needed;
	}
	
	public static boolean checkAvailability(Date startDateTime, Date stopDateTime) {
		return checkAvailability(startDateTime, stopDateTime, 3, null);
	}
	
	public static boolean checkAvailability(Date startDateTime, Date stopDateTime, Conference conference) {
		return checkAvailability(startDateTime, stopDateTime, conference.getEnableSIP() ? 3:2, conference.getId());
	}
	
	public static boolean checkAvailability(Date startDateTime, Date stopDateTime, int resources, Long id) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (!startDateTime.before(stopDateTime))
			return false;

		String startTime = format.format(startDateTime).toString();
		String stopTime = format.format(stopDateTime).toString();
		
		String addition = "";
		if (id != null)
			addition = "AND ct.CONFERENCE_ID != " + id.longValue() + " ";
		
		org.hibernate.classic.Session session = HibernateUtil
				.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<?> result = session
				.createSQLQuery(
						"SELECT MAX(total.ISABEL_RESOURCES) AS ISABEL_TOTAL, MAX(total.VNC_RESOURCES) AS VNC_TOTAL, MAX(total.SPY_RESOURCES) AS SPY_TOTAL " +
						"FROM ( " +
						"SELECT s.TIME AS TIME, SUM(ct.ISABEL_RESOURCES) AS ISABEL_RESOURCES, SUM(ct.VNC_RESOURCES) AS VNC_RESOURCES, SUM(ct.SPY_RESOURCES) AS SPY_RESOURCES, ct.CONFERENCE_ID " +
						"FROM (" +
						"SELECT CONFERENCE_ID, START_TIME, STOP_TIME, (case c.ENABLE_SIP when 1 then 3 else 2 end) AS ISABEL_RESOURCES, " +
						"1 AS VNC_RESOURCES, "+
						"0 AS SPY_RESOURCES FROM Conference c"+
						") ct, ( " +
						"SELECT START_TIME AS TIME FROM Conference " +
						"WHERE (START_TIME >= '"+startTime+"' AND START_TIME <= '"+stopTime+"') " +
						"UNION SELECT STOP_TIME AS TIME FROM Conference " +
						"WHERE (STOP_TIME >= '"+startTime+"' AND STOP_TIME <= '"+stopTime+"') " +
						"UNION SELECT '"+startTime+"' AS TIME UNION  SELECT '"+stopTime+"' AS TIME " +
						") s WHERE (ct.START_TIME <= s.TIME AND ct.STOP_TIME >= s.TIME " +
						addition +
						") " +
						"GROUP BY TIME) total;"
				)
				.list();
		
		session.getTransaction().commit();
		Map<Type, List<IsabelMachine>> machines = getIsabelMachinesByType();
		int total = 0;
		int used = 0;
		Object[] data = (Object[]) result.get(0);
		
		for (Type type : machines.keySet()) {
			used = 0;
			total = machines.get(type).size();
			switch(type) {
			case Isabel:
				if (data[0] != null)
					used += ((BigDecimal)data[0]).intValue();
				used += resources;
				break;
			case Spy:
				if (data[2] != null)
					used += ((BigDecimal)data[2]).intValue();
				used += 0;
				break;
			case VNC:
				if (data[1] != null)
					used += ((BigDecimal)data[1]).intValue();
				used += 1;
				break;
			}
			if (total < used) {
				log.error("There are not enough available machines");
				return false;
			}
		}
		
		return true;
	}
	
	public static Map<IsabelMachine.Type, List<IsabelMachine>> getIsabelMachines(
			Map<Type, List<IsabelMachine>> availableMachines,
			Map<Type, Integer> neededMachines)
			throws NotFoundException {
		
		Map<Type, Integer> needed = neededMachines;
		
		Cloud cloud = Cloud.VMWare;
		
		Map<Type, List<IsabelMachine>> result = getIsabelMachinesForCloud(availableMachines, needed, cloud);
		
		if (result == null) {
			cloud = Cloud.Amazon;
			result = getIsabelMachinesForCloud(availableMachines, needed, cloud);
		}
		
		if (result == null) {
			throw new NotFoundException(
			"There are not enough Isabel machines available.");
		}

		return result;

	}

	private static Map<IsabelMachine.Type, List<IsabelMachine>> getIsabelMachinesForCloud(
			Map<Type,List<IsabelMachine>> availableMachines,
			Map<Type, Integer> needed, 
			Cloud cloud) {
		
		log.info("Scheduling machines in " + cloud + " cloud");
		
		int isabelMachines = needed.get(Type.Isabel);
		int spyMachines = needed.get(Type.Spy);
		int vncMachines = needed.get(Type.VNC);
	
		List<IsabelMachine> isabeles = new ArrayList<IsabelMachine>();
		List<IsabelMachine> spies = new ArrayList<IsabelMachine>();
		List<IsabelMachine> vncs = new ArrayList<IsabelMachine>();
	
		for (IsabelMachine machine : availableMachines.get(Type.Isabel)) {
			if (isabelMachines > 0 && machine.getCloud().equals(cloud)) {
				isabeles.add(machine);
				isabelMachines--;
			}
		}
		for (IsabelMachine machine : availableMachines.get(Type.Spy)) {
				if (spyMachines > 0) {
					spies.add(machine);
					spyMachines--;
				}
		}
		for (IsabelMachine machine : availableMachines.get(Type.VNC)) {
				if (vncMachines > 0 && machine.getCloud().equals(cloud)) {
					vncs.add(machine);
					vncMachines--;
				}
		}

	
		// Si falta alguna maquina es que no hay maquinas suficientes.
		if (isabelMachines > 0 && vncMachines > 0 ) {
			
			log.info("Error Scheduling for " + cloud +  ":  It needs " + isabelMachines
					+ " more isabeles and " + vncMachines
					+ " more VNCs.");
			
			return null;
			
		}
	
		HashMap<Type, List<IsabelMachine>> result = new HashMap<Type, List<IsabelMachine>>();
	
		result.put(Type.Isabel, isabeles);
		result.put(Type.Spy, spies);
		result.put(Type.VNC, vncs);
	
		return result;
	}


}
