package isabel.component.conference.monitoring;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.Session;
import isabel.component.conference.util.ConfigurationParser;
import isabel.component.conference.util.Mailer;
import isabel.component.conference.util.MonitorConfigurationParser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Clase principal de la gestion de la monitorizacion. Orientada a su uso por parte
 * del Conference Manager.
 * 
 * @author irenatr@dit.upm.es
 * @author prodriguez@dit.upm.es
 * @author jcervino@dit.upm.es
 *
 */
public class MonitorManager implements ActionListener {
	
	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(MonitorManager.class);
	
	/** Stats object */
	protected static Logger stats = LoggerFactory.getLogger("stats");
	
	private ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(5);
	
	private static MonitorManager monitor;
	
	private AgentRegistry reg;
	
	private HashMap<String, Agent> agents = new HashMap<String, Agent>();

	private String html = "";
	
	private MonitorManager() {
		reg = new AgentRegistry();
		reg.addActionListener(this);
		startMonitoring();
		pool.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				// Escribimos en el archivo de stats.
				String error = getErrorsHTML();
				if (error != null && !error.equals("")) 
					stats.info(error);
				
			}
			
		}, 0, MonitorConfigurationParser.logsPeriod, TimeUnit.MILLISECONDS);
	}
	
	public static MonitorManager getInstance() {
		if (monitor == null) {
			monitor = new MonitorManager();
		}
		return monitor;
	}
	
	private void startMonitoring() {
		if (MonitorConfigurationParser.debug) return;
		
		// Starting monitor to check the record machine.
		try {
			URL url = new URL(ConfigurationParser.recordURL);
			String recordHost = url.getHost();
			createAgent("Node_Venus", "Node", recordHost, "");
			
			// Crear agente del Venus.
			createAgent("Service_Venus", "Venus", recordHost, "");
		} catch (MalformedURLException e) {
			log.error("Error decoding recording URL " + e.getMessage());
		}
		
		// Starting monitor to check the web machine.
		try {
			URL url = new URL(ConfigurationParser.webSWF);
			String webHost = url.getHost();
			createAgent("Node_Red5", "Node", webHost, "");
			
			// Crear agente de Red5.
			createAgent("Service_Red5", "Red5", webHost, "");
		} catch (MalformedURLException e) {
			log.error("Error decoding recording URL " + e.getMessage());
		}
		
		// Starting monitor to check the igw machine.
		try {
			URL url = new URL(ConfigurationParser.igwURL);
			String igwHost = url.getHost();
			createAgent("Node_IGW", "Node", igwHost, "");
			
			// Crear agente del IGW.
			createAgent("Service_IGW", "IGW", igwHost, "");
		} catch (MalformedURLException e) {
			log.error("Error decoding recording URL " + e.getMessage());
		}

		if (!ConfigurationParser.debug) {
			List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
			for (IsabelMachine machine : machines) {
				startMachineMonitoring(machine);
			}
		
			List<Conference> preResult = ConferenceRegistry.getConferences();
			
			for (Conference conference: preResult) {
				List<IsabelMachine> machinesInConference = conference.getIsabelMachines();
				if (machinesInConference.size() > 0) {
					// Es una conferencia que está ocurriendo. La monitorizamos.
					startConferenceMonitoring(conference);
					for (Session session:conference.getSession()) {
						if (session.getStartDate().before(new Date()) && (new Date()).before(session.getStopDate())) {
							startSessionMonitoring(session);
						}
					}
				}
			}
		}
	}
	
	public void startConferenceMonitoring(Conference conference) {
		if (MonitorConfigurationParser.debug) return;
		try {
			URL url = null; 
			
			List<IsabelMachine> machinesInConference = conference.getIsabelMachines();
			IsabelMachine master = null;
			for (IsabelMachine machine:machinesInConference) {
				String command = " -conference " + conference.getId().toString();
				if (machine.getIsabelNodeType()==null)continue;
				switch(machine.getIsabelNodeType()) {
				case MASTER_RECORDING_HTTPLIVESTREAMING:
					url = new URL(ConfigurationParser.igwURL);
					String httpLiveHost = url.getHost();
					command+= " -httpLiveStreaming " + httpLiveHost;
				case MASTER_RECORDING:
					url = new URL(ConfigurationParser.recordURL);
					String recordHost = url.getHost();
					createAgent("Flow_Venus_"+conference.getId(), "FlowVenus", recordHost, " -conference " + conference.getId() + " -host " + machine.getHostname() );
					command+= " -recording " + recordHost;
				case MASTER:
					command+= " -type MASTER";
					master = machine;
					break;
				}
				createAgent("Isabel_" + conference.getId() + "_" + machine.getHostname(), "Isabel", machine.getHostname(), command);
			}
			for (IsabelMachine machine:machinesInConference) {
				if (machine.getIsabelNodeType()==null) continue;
				String command = " -conference " + conference.getId().toString();
				command += " -master " + master.getHostname();
				switch(machine.getIsabelNodeType()) {
				case GW_WEB:
					url = new URL(ConfigurationParser.webSWF);
					String webHost = url.getHost();
					createAgent("Flow_Red5_"+conference.getId(), "FlowRed5", webHost, " " + conference.getId());
					command+= " -type GW_WEB -web " + webHost;
					break;
				case GW_SIP:
					String sipHost = ConfigurationParser.sipRegisterAddress;
					command+= " -type GW_SIP -sip " + sipHost;
					break;
				case SPY:
					command+= " -type SPY";
					break;
				case MCU:
					break;
				}
				createAgent("Isabel_"+conference.getId() + "_" + machine.getHostname(), "Isabel", machine.getHostname(), command);
			}
			
		} catch (MalformedURLException e) {
			log.error("Error decoding recording URL " + e.getMessage());
		}
	}
	
	public void stopConferenceMonitoring(Conference conference) {
		deleteAgent("Flow_Venus_"+conference.getId());
		deleteAgent("Flow_Red5_" + conference.getId());
		
		List<IsabelMachine> machinesInConference = conference.getIsabelMachines();
		
		for (IsabelMachine machine:machinesInConference) {
			deleteAgent("Isabel_" + conference.getId()+"_"+machine.getHostname());
		}
	}
	
	public void startSessionMonitoring(Session session) {
		try {
			URL url = new URL(ConfigurationParser.recordURL);
			String recordHost = url.getHost();
			createAgent("Session_"+session.getId(), "Session", recordHost, " " + session.getConference().getId() + " " + session.getId());
		} catch (MalformedURLException e) {
			log.error("Error decoding recording URL " + e.getMessage());
		}
	}
	
	public void stopSessionMonitoring(Session session) {
		deleteAgent("Session_"+session.getId());
	}
	
	public void startMachineMonitoring(IsabelMachine machine) {
		// Monitorizamos cada máquina.
		createAgent("Node_" + machine.getHostname(), "Node", machine.getHostname(), "");
	}
	
	public void stopMachineMonitoring(IsabelMachine machine) {
		deleteAgent("Node_"+machine.getHostname());
	}
	
	public void createAgent(String id, String type, String host, String command) {
		Agent agent = new Agent(reg.getAvailableAgentTypes().get(type), host, command);
		agents.put(id, agent);
		reg.addAgent(agent);
	}
	
	public Agent deleteAgent(String id) {
		Agent agent = null;
		if (agents.containsKey(id)) {
			agent = agents.remove(id);
			reg.removeAgent(agent);
		}	
		return agent;
	}
	
	public HashMap<String, AgentCode> getStatus() {
		HashMap<String, AgentCode> status = new HashMap<String, AgentCode>();
		HashMap<Agent, Integer> regStatus = reg.getStatus();
		for (String id : agents.keySet()) {
			Agent agent = agents.get(id);
			int code = regStatus.get(agent);
			AgentCode agentCode = agent.getType().getAgentCode(code);
			status.put(id, agentCode);
		}
		return status;
	}
	
	public String getStatusHTMLWithoutScript() {
		StringBuilder html = new StringBuilder();
		
		html.append("<table border='1' class='sortable'>");
		html.append("<tr><th>Agent Name</th><th>Status</th></tr>");
		try {
				
			for (String agent : getStatus().keySet()) {
				String message = "Unknown";
				int codeNum = 100;
				AgentCode code = getStatus().get(agent);
				if (code != null) {
					message = code.getMessage();
					codeNum = code.getCode();
				}
				if (codeNum >= 400) {
					html.append("<tr bgcolor=#FF0000><td><font color=#FFFFFF>"+ agent + "</font></td><td><font color=#FFFFFF>" + message+"</font></td></tr>");
				}else {
					html.append("<tr><td>"+ agent + "</td><td>" + message+"</td></tr>");
				}
			}
			
		}catch(Exception esc) {
		}
		html.append("</table>");
		return html.toString();
	}
	
	public String getStatusHTML() {
		StringBuilder html = new StringBuilder();
		html.append("<script src='http://www.kryogenix.org/code/browser/sorttable/sorttable.js'></script>");
		html.append(getStatusHTMLWithoutScript());
		return html.toString();
	}
	
	public String getErrorsHTML() {
		StringBuilder html = new StringBuilder();
		
		try {
				
			for (String agent : getStatus().keySet()) {
				String message = "Unknown";
				int codeNum = 100;
				AgentCode code = getStatus().get(agent);
				if (code != null) {
					message = code.getMessage();
					codeNum = code.getCode();
				}
				if (codeNum >= 400) {
					html.append("<p>" + new Date() + agent + ": " + code + " - " + message + "</p>");
				}
			}
			
		}catch(Exception esc) {
		}
		return html.toString();
	}
	
	public String getHistoricalHTML() {
		// Comprobamos que existe el archivo.
		String html="";
		File statFile = new File("logs/stats.log");
		if (statFile.exists()) {
			try {
				InputStream is =new FileInputStream(statFile);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				byte[] data = new byte[1024];
				int i = 0;
				
				while ((i = is.read(data,0,1024)) != -1) {
					baos.write(data,0,i);
				}
				baos.flush();
				html = new String(baos.toByteArray());
				
			} catch (FileNotFoundException fne) {
				log.error("Stats file does not exist");
				return null;
			} catch (IOException ioe) {
				log.error("Error reading stats file");
				return null;
			}
		} else {
			
			return null;
		}
		return html;
	}
	
	public Integer getAgentStatus(String id) {
		return reg.getAgentStatus(agents.get(id));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String html = getStatusHTML();
		if (!this.html.equals(html)) {
			this.html = html;
			Mailer.sendMail(MonitorConfigurationParser.emailTo, MonitorConfigurationParser.emailFrom, "Monitoring error", html, MonitorConfigurationParser.smtpServer);
			log.error("System error: " + html);	
		}
	}
}