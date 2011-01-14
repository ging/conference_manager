package isabel.component.conference.monitoring;

import java.util.HashMap;

/**
 * 
 * Representacion de la configuracion leida acerca de la monitorizacion.
 * 
 * @author irenatr@dit.upm.es
 * @author prodriguez@dit.upm.es
 * @author jcervino@dit.upm.es
 *
 */
public class AgentConfigurations {
	private HashMap<String, AgentType> agents = new HashMap<String, AgentType>();
	
	private String scriptsStartDelay = "5000";
	private String scriptsPath = "/home/";
	private int logsPeriod = 300000;
	private boolean debug = false;
	private String emailTo = "jcague@gmail.com";
	public static String smtpServer = "localhost";
	public static String emailFrom = "admin@globalplaza.org";
	
	public int getLogsPeriod() {
		return logsPeriod;
	}

	public void setLogsPeriod(int logsPeriod) {
		this.logsPeriod = logsPeriod;
	}
	
	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		AgentConfigurations.smtpServer = smtpServer;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		AgentConfigurations.emailFrom = emailFrom;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public AgentConfigurations() {
	}
	
	public void addAgent(AgentType agentType) {
		agents.put(agentType.getName(), agentType);
	}
	
	public HashMap<String, AgentType> getAgentTypes() {
		 return agents;
	}
	
	public int size() {
		return agents.size();
	}
	
	public String getScriptsStartDelay() {
		return scriptsStartDelay;
	}

	public void setScriptsStartDelay(String scriptsStartDelay) {
		this.scriptsStartDelay = scriptsStartDelay;
	}

	public String getScriptsPath() {
		return scriptsPath;
	}

	public void setScriptsPath(String scriptsPath) {
		this.scriptsPath = scriptsPath;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
}
