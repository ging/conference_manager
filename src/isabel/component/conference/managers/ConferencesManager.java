package isabel.component.conference.managers;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.persistence.HibernateUtil;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.jobs.StopConferenceJob;
import isabel.component.conference.util.ConfigurationParser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import org.hibernate.classic.Session;
import org.quartz.JobExecutionException;

import ch.qos.logback.core.util.StatusPrinter;

public class ConferencesManager {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			StatusPrinter.setPrintStream(new PrintStream("logs/stdout.log"));
		} catch (FileNotFoundException e1) {
		}

		if (args.length < 1) {
			imprimirAyudas();
			return;
		}

		try {
			ConfigurationParser.parse("config/config.xml");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String op = args[0];
		if (op.equalsIgnoreCase("list")) {
			// Iniciamos la base de datos.

			Session session = HibernateUtil.getSessionFactory()
					.getCurrentSession();

			session.beginTransaction();
			List<Conference> preResult = session.createQuery(
					"from Conference").list();

			for (Conference conference: preResult) {

				System.out.println("Conference: "
						+ conference.getId() + "; nombre: " 
						+ conference.getName() + "; tipo: "
						+ conference.getType() + "; inicio: "
						+ conference.getStartTime() + "; fin: "
						+ conference.getStopTime()
				);
				
				List<IsabelMachine> machines = conference.getIsabelMachines();
				for (IsabelMachine machine:machines) {
					System.out.println("    Machine: " + machine.getHostname() + "; type: " + getIsabelType(machine.getSubType()));
				}
			}
			session.getTransaction().commit();

		} else if (op.equalsIgnoreCase("get")) {
			String element = "";
			long id = -1;
			for (String arg : args) {

				if (arg.equalsIgnoreCase("get"))
					continue;

				if (arg.startsWith("-")) {
					element = arg.substring(1);

					continue;
				} else if (element.equals("")) {
					imprimirAyudaGet();
					return;
				}

				if (element.equals("id")) {
					id = Long.parseLong((String)arg);
				}
				element = "";
			}
			if (
					id == -1) {
				imprimirAyudaGet();
				return;
			}

			Conference conference = ConferenceRegistry.get(id);

			if (conference == null) {
				System.out.println("Conference not found");
				return;
			}
			
			System.out.println("Conference: "
					+ conference.getId() + "; nombre: " 
					+ conference.getName() + "; tipo: "
					+ conference.getType() + "; inicio: "
					+ conference.getStartTime() + "; fin: "
					+ conference.getStopTime()
			);
			
			List<isabel.component.conference.data.Session> sessions = conference.getSession();
			
			for (isabel.component.conference.data.Session session : sessions) {
				System.out.println(" Session: " + session.getId() + "; start: " + session.getStartDate() + "; stop: " + session.getStopDate());
			}
			List<IsabelMachine> machines = conference.getIsabelMachines();
			for (IsabelMachine machine:machines) {
				System.out.println("    Machine: " + machine.getHostname() + "; type: " + getIsabelType(machine.getSubType()));
			}

		} else if (op.equalsIgnoreCase("restart")) {
			String element = "";
			long id = -1;
			for (String arg : args) {

				if (arg.equalsIgnoreCase("restart"))
					continue;

				if (arg.startsWith("-")) {
					element = arg.substring(1);

					continue;
				} else if (element.equals("")) {
					imprimirAyudaRestart();
					return;
				}

				if (element.equals("id")) {
					id = Long.parseLong((String)arg);
				}
				element = "";
			}
			if (
					id == -1) {
				imprimirAyudaRestart();
				return;
			}

			Conference conference = ConferenceRegistry.get(id);

			if (conference == null) {
				System.out.println("Conference not found");
				return;
			}
			
			// Paramos la conferencia.
			StopConferenceJob stopJob = new StopConferenceJob();
			try {
				stopJob.stopConference(conference);
			} catch (JobExecutionException e) {
				System.out.println("Error stopping conference");
			}	
			IsabelScheduler.getInstance().getJobScheduler().unscheduleStartConferenceJob(conference);
			IsabelScheduler.getInstance().getJobScheduler().scheduleStartConferenceJob(conference, new Date());
			System.exit(0);
		} else {
			imprimirAyudas();
		}
	}
	
	/**
	 * Imprime ayuda para todos los comandos
	 */
	public static void imprimirAyudas() {
		imprimirAyudaList();
		imprimirAyudaGet();
		imprimirAyudaRestart();
	}

	/**
	 * Imprime la ayuda para el comando list.
	 */
	public static void imprimirAyudaList() {
		System.out.println("Usage: java ConferencesManager list");
	}

	/**
	 * Imprime la ayuda para el comando add.
	 */
	public static void imprimirAyudaGet() {
		System.out
				.println("Usage: java ConferencesManager get -id conferenceID");
	}

	/**
	 * Imprime la ayuda para el comando remove.
	 */
	public static void imprimirAyudaRestart() {
		System.out
				.println("Usage: java ConferencesManager restart  -id conferenceID");
	}
	
	/**
	 * Devuelve un String con el tipo de maquina.
	 * 
	 * @param type
	 * Tipo de maquina.
	 * 
	 * @return
	 * Un string representando el tipo de maquina.
	 */
	public static String getIsabelType(IsabelMachine.SubType type) {
		switch (type) {
		case Spy:
			return "spy";
		case vmVNC:
			return "vmVnc";
		case vmIsabel:
			return "vmIsabel";
		case amiIsabel:
			return "amiIsabel";
		case amiVNC:
			return "amiVNC";
		}
		return "";
	}
}
