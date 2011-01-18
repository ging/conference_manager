package isabel.component.conference.scheduler.jobs;

import isabel.component.conference.CloudManager;
import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.VMWareManager;
import isabel.component.conference.AWS.AmazonManager;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.Conference.FLASH_CODEC;
import isabel.component.conference.data.IsabelMachine.IsabelNodeType;
import isabel.component.conference.data.IsabelMachine.Type;
import isabel.component.conference.dst.DSTManager;
import isabel.component.conference.ist.ISTManager;
import isabel.component.conference.ist.SiteType;
import isabel.component.conference.monitoring.MonitorManager;
import isabel.component.conference.util.ConfigurationParser;
import isabel.component.conference.vst.VSTManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javassist.NotFoundException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inicializa todas las maquinas que necesita la conferencia.
 * 
 * @author jcervino
 *
 */
public class StartConferenceJob extends ConferenceJob {

	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory
			.getLogger(StartConferenceJob.class);

	/**
	 * Inicializa una maquina master.
	 * 
	 * @param machine
	 */
	public void initializeMaster(IsabelMachine machine) {
		log.info("Conference: " + conference.getName()
				+ "; New master initialized: " + machine.getHostname());
		master = machine;
		machine.setIsabelNodeType(IsabelNodeType.MASTER);
		IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
		if (manager == null)
			manager =  new ISTManager();

		try {
			manager.launchMaster(machine.getHostname(),
					conference.getSessionType(), SiteType.MASTER, ConfigurationParser.isabelSessionBW,
					conference.getId() + "", "mcu", null);
		} catch (IOException e) {
			log.error("Error launching Isabel Master: " + e.getMessage());
		}
	}
	
	/**
	 * Inicializa una maquina Gateway Web.
	 * 
	 * @param machine
	 */
	public void initializeMasterRecording(IsabelMachine machine, IsabelMachine vnc, String isabelBW, String streamingBW, boolean H264) {
		log.info("Conference: " + conference.getName()
				+ "; New Master/Recording initialized: " + machine.getHostname()
				+ "; sending to " + ConfigurationParser.streamURL
				+ conference.getId());

		machine.setIsabelNodeType(IsabelNodeType.MASTER_RECORDING);
		IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
		
		if (manager == null)
			manager =  new ISTManager();
		
		try {
			manager.launchMasterGatewayFlash(machine.getHostname(), conference.getSessionType(), 
					"mcuRecording", conference.getId() + "", isabelBW,
					ConfigurationParser.streamURL + conference.getId(), streamingBW, 
					H264, ConfigurationParser.metadataURL, vnc.getHostname(), ConfigurationParser.vncPassword, 
					conference.getChatURL(), null);
		} catch (IOException e) {
			log.error("Error launching Isabel Master: " + e.getMessage());
		}
	}
	
	/**
	 * Inicializa una maquina Gateway Web.
	 * 
	 * @param machine
	 */
	public void initializeMasterRecordingWithHTTPLiveStreaming(IsabelMachine machine, IsabelMachine vnc, String isabelBW, String streamingBW, 
			boolean H264, String httpLiveStreamingBW) {
		log.info("Conference: " + conference.getName()
				+ "; New Master/Recording initialized: " + machine.getHostname()
				+ "; sending to " + ConfigurationParser.streamURL
				+ conference.getId());

		machine.setIsabelNodeType(IsabelNodeType.MASTER_RECORDING_HTTPLIVESTREAMING);
		IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
		
		if (manager == null)
			manager =  new ISTManager();
		
		try {
			manager.launchMasterGatewayFlashHTTPLiveStreaming(machine.getHostname(), conference.getSessionType(), 
					"mcuRecording", conference.getId() + "", isabelBW,
					ConfigurationParser.streamURL + conference.getId(), streamingBW, 
					H264, ConfigurationParser.metadataURL, vnc.getHostname(), ConfigurationParser.vncPassword, 
					ConfigurationParser.igwURL, conference.getId()+"", httpLiveStreamingBW, 
					conference.getChatURL(), null);
		} catch (IOException e) {
			log.error("Error launching Isabel Master: " + e.getMessage());
		}
	}

	/**
	 * Inicializa una maquina Gateway Web.
	 * 
	 * @param machine
	 */
	public void initializeGatewayWeb(IsabelMachine machine, IsabelMachine vnc) {
		log.info("Conference: " + conference.getName()
				+ "; New Web gateway initialized: " + machine.getHostname()
				+ "; sending to " + ConfigurationParser.webURL + conference.getId());

		machine.setIsabelNodeType(IsabelNodeType.GW_WEB);
		IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
		
		if (manager == null)
			manager =  new ISTManager();
		
		try {
			manager.launchGatewayFlash(machine.getHostname(), conference
					.getSessionType(), "gwWeb", conference.getId()+"", master.getHostname(),
					ConfigurationParser.webURL + conference.getId(), ConfigurationParser.webVideoBW, 
					false, null, vnc.getHostname(), ConfigurationParser.vncPassword, null);
		} catch (IOException e) {
			log.error("Error launching Isabel Master: " + e.getMessage());
		}
	}

	/**
	 * Inicializa una maquina Gateway Web.
	 * 
	 * @param machine
	 */
	public void initializeGatewaySIP(IsabelMachine machine) {
		log.info("Conference: " + conference.getName()
				+ "; New SIP gateway initialized: " + machine.getHostname());
		
		machine.setIsabelNodeType(IsabelNodeType.GW_SIP);
		IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
		
		if (manager == null)
			manager =  new ISTManager();
		
		try {
			String host = machine.getHostname();
			String siteName = "gwSIP";
			String masterip = master.getHostname();

			String registerPort = ConfigurationParser.sipRegisterPort;
			String clientName = "session" + conference.getId();
			String realm = ConfigurationParser.sipRealm;
			String clientNickname = ConfigurationParser.sipClientNickname;
			String clientAddress = machine.getHostname();
			String registerAddress = ConfigurationParser.sipRegisterAddress;
			String videoBW = ConfigurationParser.sipVideoBW;
			String password = ConfigurationParser.sipPassword;

			manager.launchGatewaySIP(host, siteName, conference.getId() + "", masterip, registerAddress,
					registerPort, clientAddress, clientName, clientNickname,
					realm, password, videoBW, null);
		} catch (IOException e) {
			log.error("Error launching Isabel Master: " + e.getMessage());
		}
	}

	/**
	 * Inicializa una maquina Spy.
	 * 
	 * @param machine
	 */
	public void initializeSpy(IsabelMachine machine) {
		log.info("Conference: " + conference.getName()
				+ "; New spy initialized: " + machine.getHostname());
		
		machine.setIsabelNodeType(IsabelNodeType.SPY);
		IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
		
		if (manager == null)
			manager =  new ISTManager();
		
		try {
			manager.launchGatewayFlash(machine.getHostname(), conference
					.getSessionType(), "spy", conference.getId()+"", master.getHostname(),
					ConfigurationParser.streamURL, ConfigurationParser.webVideoBW, 
					false, null, "vncServer", "vncPassword", null);
		} catch (IOException e) {
			log.error("Error launching Isabel Master: " + e.getMessage());
		}
	}

	/**
	 * Gestiona la configuracion del VNC.
	 * 
	 * @param vnc
	 */
	public void initializeVNC(IsabelMachine vnc) {
		try {
			if (vstManager == null) vstManager = new VSTManager();
			log.info("Conference: " + conference.getName()
					+ "; New VNC initialized: " + vnc.getHostname());
			vstManager.changeSharedFolder(vnc, conference);
		} catch (Exception e) {
			log.error("Error changing Samba configuration file: " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Asigna las maquinas que va a necesitar la conferencia.
	 * 
	 * @return
	 */
	private boolean allocateConference() {
		// Suponemos que hay m�quinas disponibles porque se comprob� en su momento. Si no no estar�amos
		// en este punto.
		Map<Type, Integer> neededMachines = IsabelMachineRegistry.getNeededIsabelMachines(conference);
		Map<Type, List<IsabelMachine>> machines = IsabelMachineRegistry.getAvailableIsabelMachinesByType();
		
		try {
			Map<Type, List<IsabelMachine>> machinesForConference = IsabelMachineRegistry.getIsabelMachines(machines, neededMachines);
			
			if (machinesForConference == null) {
				return false;
			}
			
			for (Type type : machinesForConference.keySet()) {
				List<IsabelMachine> machinesOfType = machinesForConference.get(type);
				
				for (IsabelMachine newMachine : machinesOfType) {
					ConferenceRegistry.addIsabelMachineToConference(conference, newMachine);
				}
			}
			
			return true;
		} catch (NotFoundException e) {
			return false;
		}
	}

	/**
	 * Ejecuta la tarea.
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		Long id = (Long) context.getJobDetail().getJobDataMap().get(
				"conference");
		startConference(ConferenceRegistry.get(id));
	}
	
	/**
	 * Inicia las maquinas de la conferencia.
	 * 
	 * @param conference
	 * @throws JobExecutionException
	 */
	public void startConference(Conference conference) {
		synchronized (StartConferenceJob.class) {
			
			this.conference = conference;

			boolean allocated;
			// Asociamos las maquinas a la Conferencia.
			allocated = allocateConference();
	
			if (allocated)
			{
				conference = ConferenceRegistry.get(conference.getId());
				
				log.info("Conference " + conference.getName() + " starts at "
						+ new Date() + " in " + conference.getCloud() + " with " 
						+ conference.getIsabelMachines().size()	+ " Isabel machines.");
				
				// Preparamos las maquinas elegidas en el Cloud en el que se encuentren.
				CloudManager manager = null;
				switch (conference.getCloud()) {
				case VMWare:
					manager = new VMWareManager();
					break;
				case Amazon:
					manager = new AmazonManager();
					break;
				}
				manager.prepareConference(conference);
				
				conference = ConferenceRegistry.get(conference.getId());
		
				// Obtenemos las maquinas.
				List<IsabelMachine> spies = new ArrayList<IsabelMachine>();
				List<IsabelMachine> vncs = new ArrayList<IsabelMachine>();
				List<IsabelMachine> isabeles = new ArrayList<IsabelMachine>();
				for (IsabelMachine machine : conference.getIsabelMachines()) {
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
		
				log.info("There are " + isabeles.size() + " isabeles and " + vncs.size() + " vncs");
				
				// Iniciamos los VNC.
				IsabelMachine vncMachine = null;
				for (IsabelMachine vnc : vncs) {
					initializeVNC(vnc);
					vncMachine = vnc;
				}
		
				// Iniciamos el master/Recording.
				if (isabeles.size() > 0) {
					master = isabeles.remove(0);
					
					String isabelBW = ConfigurationParser.isabelSessionBW;
					String recordBW = ConfigurationParser.recordVideoBW;
					boolean H264 = (conference.getRecordingCodec().equals(FLASH_CODEC.H264));
					
					if (conference.getIsabelBW() != null) {
						isabelBW = conference.getIsabelBW();
					}
					if (conference.getRecordingBW() != null) {
						recordBW = conference.getRecordingBW();
					}
					
					if (conference.getEnableHTTPLiveStreaming()) {
						String httpLiveStreamingBW = ConfigurationParser.igwVideoBW;
						
						if (conference.getHTTPLiveStreamingBW() != null) {
							httpLiveStreamingBW = conference.getHTTPLiveStreamingBW();
						}
						initializeMasterRecordingWithHTTPLiveStreaming(master, vncMachine,isabelBW, recordBW, H264, httpLiveStreamingBW);
					} else {
						initializeMasterRecording(master, vncMachine,isabelBW, recordBW, H264);
					}
					if (conference.getEnableIsabel()) {
						// Lanzamos el DNS Scripting Toolkit para asociar el DNS.
						DSTManager dst = new DSTManager();
						try {
							dst.addDns(conference.getId()+"", master.getHostname(), null);
						} catch (IOException e) {
							log.error("Error adding Dynamic DNS : " + conference.getId() + "; " + master.getHostname());
						}
					}
				} else {
					log.error("There was no Isabel Master");
					return;
				}
				
				// Esperar un tiempo a que haya iniciado la sesi�n.
				if (!ConfigurationParser.debug) {
					try {
						Thread.sleep(ConfigurationParser.marginMasterGateway);
					} catch (InterruptedException e) {
					}
				}
			
				// Iniciamos el GW de participacion/streaming.
				if (isabeles.size() > 0) {
					initializeGatewayWeb(isabeles.remove(0), vncMachine);
				}
				
				// Iniciamos el GW SIP si lo hay.
				if (conference.getEnableSIP()) {
					initializeGatewaySIP(isabeles.remove(0));
				}
				
				// Iniciamos los Spy.
				for (IsabelMachine spy : spies) {
					initializeSpy(spy);
				}
				
				// Comenzamos la monitorizacion.
				MonitorManager.getInstance().startConferenceMonitoring(conference);
	
			}
		}
	}

}
