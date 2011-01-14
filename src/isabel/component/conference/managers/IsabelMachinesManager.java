package isabel.component.conference.managers;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.IsabelMachine.IsabelNodeType;
import isabel.component.conference.persistence.HibernateUtil;
import isabel.component.conference.util.ConfigurationParser;
import isabel.component.conference.vst.Share;
import isabel.component.conference.vst.SmbWizard;
import isabel.component.conference.vst.VSTManager;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.hibernate.classic.Session;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Gestor de maquinas Isabel que se puede ejecutar por linea de comando
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class IsabelMachinesManager {

	/**
	 * Metodo de arranque del gestor de maquinas Isabel.
	 * 
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		// Borrando Loggers indeseados.
		Enumeration<String> loggers = LogManager.getLogManager().getLoggerNames();
		while (loggers.hasMoreElements()) {
			java.util.logging.Logger log = LogManager.getLogManager().getLogger(loggers.nextElement());
			for (Handler handler : log.getHandlers()) {
				if (handler instanceof ConsoleHandler) {
					log.removeHandler(handler);
				}
			}
		}
		
		try {
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			StatusPrinter.setPrintStream(new PrintStream("logs/stdout.log"));
			StatusPrinter.print(lc);
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
			List<IsabelMachine> preResult = session.createQuery(
					"from IsabelMachine").list();

			for (IsabelMachine isabel : preResult) {

				System.out.println("ISABEL MACHINE. Hostname: "
						+ isabel.getHostname() + "; tipo: "
						+ getIsabelType(isabel.getSubType()) + "; tipo de nodo: " + getIsabelNodeType(isabel.getIsabelNodeType()) + "; Id: "
						+ isabel.getId());
				Conference conference = isabel.getConference();
				if (conference != null) {
					System.out.println("     Has conference: "
							+ conference.getName() + "; starts: "
							+ conference.getResourcesStartTime() + "; stops: "
							+ conference.getResourcesStopTime() + "; Id: "
							+ conference.getId());
				}
			}
			session.getTransaction().commit();

		} else if (op.equalsIgnoreCase("add")) {
			String element = "", hostname = null;
			IsabelMachine.SubType type = null;
			for (String arg : args) {

				if (arg.equalsIgnoreCase("add"))
					continue;

				if (arg.startsWith("-")) {
					element = arg.substring(1);

					continue;
				} else if (element.equals("")) {
					imprimirAyudaAdd();
					return;
				}

				if (element.equals("hostname")) {
					hostname = arg;
				} else if (element.equals("type")) {
					if (arg.equalsIgnoreCase(			"amiIsabel")) {
						type = IsabelMachine.SubType.amiIsabel;
					} else if (arg.equalsIgnoreCase(	"vmIsabel")) {
						type = IsabelMachine.SubType.vmIsabel;
					} else if (arg.equalsIgnoreCase(	"spy")) {
						type = IsabelMachine.SubType.Spy;
					} else if (arg.equalsIgnoreCase(	"amiVNC")) {
						type = IsabelMachine.SubType.amiVNC;
					} else if (arg.equalsIgnoreCase(	"vmVNC")) {
						type = IsabelMachine.SubType.vmVNC;
					}
				}
				element = "";
			}
			if (	type == null ||
					((	type.equals(IsabelMachine.SubType.vmIsabel) || 
						type.equals(IsabelMachine.SubType.vmVNC) || 
						type.equals(IsabelMachine.SubType.Spy)) 
						&& hostname == null)) {
				imprimirAyudaAdd();
				return;
			}
			IsabelMachine isabel = new IsabelMachine();
			isabel.setHostname(hostname);
			isabel.setSubType(type);

			Session hSession = HibernateUtil.getSessionFactory()
					.getCurrentSession();
			hSession.beginTransaction();
			hSession.save(isabel);
			hSession.getTransaction().commit();

			if (isabel.getType().equals(IsabelMachine.Type.VNC)) {
				configureVNC(isabel);
			}
			System.out.println("Restart Conference Manager in order to apply changes.");
		} else if (op.equalsIgnoreCase("remove")) {
			if (args.length != 2) {
				imprimirAyudaRemove();
				return;
			}

			String id = args[1];
			Session session = HibernateUtil.getSessionFactory()
					.getCurrentSession();
			session.beginTransaction();
			IsabelMachine machine = (IsabelMachine) session.get(
					IsabelMachine.class, new Long(id));
			session.delete(machine);
			session.getTransaction().commit();
			if (machine.getType().equals(IsabelMachine.Type.VNC)) {
				// Lo borramos del archivo de configuracion.
				System.out.println("Borrando " + machine.getHostname() + " de "
						+ ConfigurationParser.sambaFile);
				Share share = SmbWizard.getInstance().getShare(
						machine.getHostname());
				System.out.println(share == null);
				if (share != null) {
					System.out.println("Borrando share");
					SmbWizard.getInstance().deleteShare(share.getName());
					SmbWizard.getInstance().save();
				}
			}
			System.out.println("Restart Conference Manager in order to apply changes.");
		} else {
			imprimirAyudas();
		}

	}

	/**
	 * Imprime ayuda para todos los comandos
	 */
	public static void imprimirAyudas() {
		imprimirAyudaList();
		imprimirAyudaAdd();
		imprimirAyudaRemove();
	}

	/**
	 * Imprime la ayuda para el comando list.
	 */
	public static void imprimirAyudaList() {
		System.out.println("Usage: java IsabelMachinesManager list");
	}

	/**
	 * Imprime la ayuda para el comando add.
	 */
	public static void imprimirAyudaAdd() {
		System.out
				.println("Usage: java IsabelMachinesManager add -hostname machineHostname -type [vmIsabel, spy, vmVnc, amiIsabel, amiVnc]");
	}

	/**
	 * Imprime la ayuda para el comando remove.
	 */
	public static void imprimirAyudaRemove() {
		System.out
				.println("Usage: java IsabelMachinesManager remove machineID");
	}

	/**
	 * Configura una maquina VNC en el smb.conf
	 * 
	 * @param isabel
	 */
	public static void configureVNC(IsabelMachine isabel) {
		VSTManager vst = new VSTManager();
		vst.setDefaultFolder(isabel);
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
	
	private static String getIsabelNodeType(IsabelNodeType isabelNodeType) {
		if (isabelNodeType == null) return "";
		switch(isabelNodeType) {
		case MASTER:
			return "master";
		case GW_SIP:
			return "sip";
		case GW_WEB:
			return "gwflash";
		case MASTER_RECORDING:
			return "master-recording";
		case MASTER_RECORDING_HTTPLIVESTREAMING:
			return "master-recording-httplivestreaming";
		case MCU:
			return "mcu";
		case SPY:
			return "spy";
		}
		return "";
	}
}
