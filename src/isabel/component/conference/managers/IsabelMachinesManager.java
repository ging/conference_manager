package isabel.component.conference.managers;

import isabel.component.conference.IsabelMachineRegistry;
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
 * 
 * Isabel machines manager that can be used by command line.
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class IsabelMachinesManager {

	public static void main(String[] args) {

		// Removing undesired loggers.
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
			showList();

		} else if (op.equalsIgnoreCase("add")) {
			addMachine(args);
		} else if (op.equalsIgnoreCase("remove")) {
			if (args.length != 2) {
				imprimirAyudaRemove();
				return;
			}

			removeMachine(args);
		} else {
			imprimirAyudas();
		}

	}
	
	private static void showList() {
		
		List<IsabelMachine> preResult = IsabelMachineRegistry.getIsabelMachines();

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
	}
	
	private static void addMachine(String[] args) {
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
		
		IsabelMachineRegistry.addIsabelMachine(isabel);

		if (isabel.getType().equals(IsabelMachine.Type.VNC)) {
			configureVNC(isabel);
		}
		
		System.out.println("Restart Conference Manager in order to apply changes.");
	}
	
	private static void removeMachine(String[] args) {
		String id = args[1];
		Session session = HibernateUtil.getSessionFactory()
				.getCurrentSession();
		session.beginTransaction();
		IsabelMachine machine = (IsabelMachine) session.get(
				IsabelMachine.class, new Long(id));
		session.delete(machine);
		session.getTransaction().commit();
		if (machine.getType().equals(IsabelMachine.Type.VNC)) {
			// Delete it from the samba config file.
			System.out.println("Deleting " + machine.getHostname() + " from "
					+ ConfigurationParser.sambaFile);
			Share share = SmbWizard.getInstance().getShare(
					machine.getHostname());
			System.out.println(share == null);
			if (share != null) {
				System.out.println("Share deleted");
				SmbWizard.getInstance().deleteShare(share.getName());
				SmbWizard.getInstance().save();
			}
		}
		System.out.println("Restart Conference Manager in order to apply changes.");
	}

	/**
	 * Prints help for all commands
	 */
	public static void imprimirAyudas() {
		imprimirAyudaList();
		imprimirAyudaAdd();
		imprimirAyudaRemove();
	}

	/**
	 * Prints help for List command
	 */
	public static void imprimirAyudaList() {
		System.out.println("Usage: java IsabelMachinesManager list");
	}

	/**
	 * Prints help for Add command
	 */
	public static void imprimirAyudaAdd() {
		System.out
				.println("Usage: java IsabelMachinesManager add -hostname machineHostname -type [vmIsabel, spy, vmVnc, amiIsabel, amiVnc]");
	}

	/**
	 * Prints help for Remove command
	 */
	public static void imprimirAyudaRemove() {
		System.out
				.println("Usage: java IsabelMachinesManager remove machineID");
	}

	/**
	 * Config a VNC machine in the samba config file
	 * 
	 * @param isabel
	 * VNC Machine
	 */
	public static void configureVNC(IsabelMachine isabel) {
		VSTManager vst = new VSTManager();
		vst.setDefaultFolder(isabel);
	}

	/**
	 * Returns the type of the machine.
	 * 
	 * @param type
	 * The machine type specified by SubType class.
	 * 
	 * @return
	 * Machine type: "spy", "vmVnc", "vmIsabel", "amiVNC" or "amiIsabel".
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
