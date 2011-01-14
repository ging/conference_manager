package isabel.component.conference.util;
////////////////////////////////////////////////////////////////////////////////
//
//	Universidad Politecnica de Madrid
//  DEPARTAMENTO DE INGENIERIA DE SISTEMAS TELEMATICOS
//	Grupo de Internet de Nueva Generacion
//
//  Aun esta por decidir la licencia
//
////////////////////////////////////////////////////////////////////////////////

import isabel.component.conference.monitoring.AgentCode;
import isabel.component.conference.monitoring.AgentType;
import isabel.component.conference.monitoring.AgentConfigurations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * Parseador del archivo de configuracion de Marte
 * 
 * @author prodriguez
 * @author jcervino
 *
 */
public class MonitorConfigurationParser {
	
	/**
	 * 
	 */
	public static String scriptsStartDelay = "5000";
	public static String scriptsPath = "/home/";
	public static int logsPeriod = 300000;
	public static boolean debug = false;
	public static HashMap<String, AgentType> agentTypes = new HashMap<String, AgentType>();
	public static String emailTo = "jcervino@dit.upm.es";
	public static String smtpServer;
	public static String emailFrom;
	
	/**
	 * Crea el lector de configuraci�n.
	 * @param filename
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public MonitorConfigurationParser(String filename) {
		
		Digester digester = new Digester();
		digester.setValidating(false);
		
		digester.addObjectCreate("configuration", AgentConfigurations.class);
		digester.addSetProperties("configuration/general", "name", "name");
		digester.addSetProperties("configuration/general", "script", "script");
		digester.addSetProperties("configuration/general", "period", "period");
		digester.addSetProperties("configuration/general", "logsPeriod", "logsPeriod");
		digester.addSetProperties("configuration/general", "emailTo", "emailTo");
		digester.addSetProperties("configuration/general", "emailFrom", "emailFrom");
		digester.addSetProperties("configuration/general", "smtpServer", "smtpServer");
		
		digester.addObjectCreate("configuration/agentType", AgentType.class);
		digester.addSetProperties("configuration/agentType", "name", "name");
		digester.addSetProperties("configuration/agentType", "script", "script");
		digester.addSetProperties("configuration/agentType", "period", "period");
		
		digester.addObjectCreate("configuration/agentType/code", AgentCode.class);
		digester.addSetProperties("configuration/agentType/code", "code", "code");
		digester.addSetProperties("configuration/agentType/code", "message", "message");
		
		digester.addSetNext("configuration/agentType/code", "addCode");
		digester.addSetNext("configuration/agentType", "addAgent");
		
		
		File f = new File(filename);
		
		try {
			AgentConfigurations types = (AgentConfigurations)digester.parse(f);
			debug = types.isDebug();
			scriptsPath = types.getScriptsPath();
			scriptsStartDelay = types.getScriptsStartDelay();
			agentTypes = types.getAgentTypes();
			emailTo = types.getEmailTo();
			emailFrom = types.getEmailFrom();
			smtpServer = types.getSmtpServer();
		} catch (IOException e) {
		} catch (SAXException e) {
		}		
	}
	
	/**
	 * Lee la configuracion general.
	 * @param url
	 */
	public void addGeneralConfiguration(String scriptsStartDelay, String scriptsPath, String debug) {
		MonitorConfigurationParser.scriptsStartDelay = scriptsStartDelay;
		MonitorConfigurationParser.scriptsPath = scriptsPath;
		if (debug.trim().equalsIgnoreCase("true")) {
			MonitorConfigurationParser.debug = true;	
		} else {
			MonitorConfigurationParser.debug = false;
		}
	}
	
	/**
	 * Inicia la lectura del archivo de configuracion.
	 * @param filename
	 */
	public static void parse(String filename) throws Exception {
		new MonitorConfigurationParser(filename);
	}
}
