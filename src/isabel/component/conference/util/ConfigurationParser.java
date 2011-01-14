////////////////////////////////////////////////////////////////////////////////
//
//	Universidad Politecnica de Madrid
//  DEPARTAMENTO DE INGENIERIA DE SISTEMAS TELEMATICOS
//	Grupo de Internet de Nueva Generacion
//
//  Aun esta por decidir la licencia
//
////////////////////////////////////////////////////////////////////////////////

package isabel.component.conference.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * Parseador del archivo de configuracion de Marte
 * 
 * @author prodriguez
 * @author jcervino
 *
 */
public class ConfigurationParser {
	
	/**
	 * Nombre del host donde est� ejecut�ndose el ConferenceManager.
	 */
	public static String hostname = "";
	
	/**
	 * Puerto por el que va a escuchar el API Rest.
	 */
	public static int port = 0;
	
	/**
	 * URL que apunta a la aplicacion del grabador
	 */
	public static String recordURL = "";
	public static String recordSWF = "";
	public static String recordRTMP = "";
	public static String metadataURL = "";
	public static String metadataPath = "";
	public static String editorSWF = "";
	
	/**
	 * URL que apunta a la aplicacion de Streaming.
	 */
	public static String streamURL = "";
	public static String streamSWF = "";
	
	/**
	 * 
	 */
	public static String webURL = "";
	public static String webSWF = "";

	/**
	 * 
	 */
	public static String sambaFile = "";
	public static String sambaRoot = "";
	public static String sambaDefault = "";
	
	/**
	 * 
	 */
	public static String sipRegisterAddress = "";
	public static String sipRegisterPort = "";
	public static String sipRealm = "";
	public static String sipClientNickname = "";
	public static String sipVideoBW = "";
	public static String sipPassword = "";
	
	/**
	 * 
	 */
	public static int marginConferenceSession = 60000;
	public static int marginMasterGateway = 30000;

	/**
	 * 
	 */
	public static String isabelSessionBW = "1M";
	public static String webVideoBW = "400000";
	public static String recordVideoBW = "400000";
	public static String igwVideoBW = "300";
	
	/**
	 * 
	 */
	public static String vncPassword = "";

	/**
	 * Configuracion de Amazon.
	 */
	public static String amazonURL = "https://eu-west-1.ec2.amazonaws.com";
	public static String amazonServiceKey = "";
	public static String amazonServiceSecret = "";
	public static String amazonIsabelAMI = "ami-a9507add";
	public static String amazonVNCAMI = "";
	
	public static String igwURL = "http://hostname/live";
	public static String igwOutputDir = "/var/www/live";
	
	public static boolean debug = false;
	
	/**
	 * Crea el lector de configuraci�n.
	 * @param filename
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public ConfigurationParser(String filename) {
		
		Digester digester = new Digester();
		digester.push(this);
		
		// A�adimos el m�todo que inicia una aplicacion
		digester.addCallMethod("configuration/http", "addHTTP", 2);
		digester.addCallParam("configuration/http", 0, "hostname");
		digester.addCallParam("configuration/http", 1, "port");
		
		digester.addCallMethod("configuration/record", "addRecord", 6);
		digester.addCallParam("configuration/record", 0, "url");
		digester.addCallParam("configuration/record", 1, "swf");
		digester.addCallParam("configuration/record", 2, "metadata");
		digester.addCallParam("configuration/record", 3, "rtmp");
		digester.addCallParam("configuration/record", 4, "editorSWF");
		digester.addCallParam("configuration/record", 5, "metadataPath");
		
		digester.addCallMethod("configuration/streaming", "addStreaming", 2);
		digester.addCallParam("configuration/streaming", 0, "url");
		digester.addCallParam("configuration/streaming", 1, "swf");
		
		digester.addCallMethod("configuration/web", "addWeb", 2);
		digester.addCallParam("configuration/web", 0, "url");
		digester.addCallParam("configuration/web", 1, "swf");
		
		digester.addCallMethod("configuration/samba", "addSamba", 3);
		digester.addCallParam("configuration/samba", 0, "file");
		digester.addCallParam("configuration/samba", 1, "root");
		digester.addCallParam("configuration/samba", 2, "defaultFolder");
		
		digester.addCallMethod("configuration/sip", "addSIP", 6);
		digester.addCallParam("configuration/sip", 0, "registerAddress");
		digester.addCallParam("configuration/sip", 1, "registerPort");
		digester.addCallParam("configuration/sip", 2, "nickName");
		digester.addCallParam("configuration/sip", 3, "realm");
		digester.addCallParam("configuration/sip", 4, "password");
		digester.addCallParam("configuration/sip", 5, "videoBW");
		
		digester.addCallMethod("configuration/margin", "addMargins", 2);
		digester.addCallParam("configuration/margin", 0, "conferenceSession");
		digester.addCallParam("configuration/margin", 1, "masterGateway");
		
		digester.addCallMethod("configuration/bandwidth", "addBandwidth", 4);
		digester.addCallParam("configuration/bandwidth", 0, "isabelSession");
		digester.addCallParam("configuration/bandwidth", 1, "webVideo");
		digester.addCallParam("configuration/bandwidth", 2, "recordVideo");
		digester.addCallParam("configuration/bandwidth", 3, "igwVideo");
		
		digester.addCallMethod("configuration/vnc", "addVNCConfiguration", 1);
		digester.addCallParam("configuration/vnc", 0, "password");
		
		digester.addCallMethod("configuration/aws", "addAWSConfiguration", 5);
		digester.addCallParam("configuration/aws", 0, "isabelAmi");
		digester.addCallParam("configuration/aws", 1, "vncAmi");
		digester.addCallParam("configuration/aws", 2, "serviceKey");
		digester.addCallParam("configuration/aws", 3, "serviceSecret");
		digester.addCallParam("configuration/aws", 4, "url");
		
		digester.addCallMethod("configuration/igw", "addIGWConfiguration", 2);
		digester.addCallParam("configuration/igw", 0, "url");
		digester.addCallParam("configuration/igw", 1, "outputDir");
		
		digester.addCallMethod("configuration/general", "addGeneralConfiguration", 1);
		digester.addCallParam("configuration/general", 0, "debug");
		
		
		File f = new File(filename);
		
		try {
			digester.parse(f);
		} catch (IOException e) {
		} catch (SAXException e) {
		}		
	}
	
	/**
	 * Lee la configuracion del servidor REST.
	 * 
	 * @param hostname
	 * @param port
	 */
	public void addHTTP(String hostname, String port) {
		ConfigurationParser.hostname = hostname;
		ConfigurationParser.port = new Integer(port);
	}
	
	/**
	 * Lee la configuracion del grabador.
	 * @param url
	 */
	public void addRecord(String url, String swf, String metadata, String rtmp, String editorSWF, String metadataPath) {
		ConfigurationParser.recordURL = url;
		ConfigurationParser.recordSWF = swf;
		ConfigurationParser.metadataURL = metadata;
		ConfigurationParser.recordRTMP = rtmp;
		ConfigurationParser.editorSWF = editorSWF;
		ConfigurationParser.metadataPath = metadataPath;
	}
	
	/**
	 * Lee la configuracion del Streaming.
	 * @param url
	 */
	public void addStreaming(String url, String swf) {
		ConfigurationParser.streamURL = url;
		ConfigurationParser.streamSWF = swf;
	}
	
	/**
	 * Lee la configuracion de la participacion Web.
	 * @param url
	 */
	public void addWeb(String url, String swf) {
		ConfigurationParser.webURL = url;
		ConfigurationParser.webSWF = swf;
	}
	
	/**
	 * Lee la configuracion de la Samba.
	 * @param url
	 */
	public void addSamba(String file, String root, String defaultFolder) {
		ConfigurationParser.sambaFile = file;
		ConfigurationParser.sambaRoot = root;
		ConfigurationParser.sambaDefault = defaultFolder;
	}
	
	/**
	 * Lee la configuracion de la SIP.
	 * @param url
	 */
	public void addSIP(String registerAddress, String registerPort, String nickName, String realm, String password, String videoBW) {
		ConfigurationParser.sipRegisterAddress = registerAddress;
		ConfigurationParser.sipRegisterPort = registerPort;
		ConfigurationParser.sipClientNickname = nickName;
		ConfigurationParser.sipRealm = realm;
		ConfigurationParser.sipPassword = password;
		ConfigurationParser.sipVideoBW= videoBW;
		
	}
	
	/**
	 * Lee la configuracion de la SIP.
	 * @param url
	 */
	public void addMargins(String conferenceSession, String masterGateway) {
		ConfigurationParser.marginConferenceSession = Integer.parseInt(conferenceSession);
		ConfigurationParser.marginMasterGateway = Integer.parseInt(masterGateway);
	}
	
	/**
	 * Lee la configuracion de la SIP.
	 * @param url
	 */
	public void addBandwidth(String isabelSession, String webVideo, String recordVideo, String igwVideo) {
		ConfigurationParser.isabelSessionBW = isabelSession;
		ConfigurationParser.webVideoBW = webVideo;
		ConfigurationParser.recordVideoBW = recordVideo;
		ConfigurationParser.igwVideoBW = igwVideo;
	}
	
	/**
	 * Lee la configuracion de la SIP.
	 * @param url
	 */
	public void addVNCConfiguration(String password) {
		ConfigurationParser.vncPassword = password;
	}
	
	public void addAWSConfiguration(String isabelAmi, String vncAmi, String serviceKey, String serviceSecret, String url) {
		ConfigurationParser.amazonIsabelAMI = isabelAmi;
		ConfigurationParser.amazonURL = url;
		ConfigurationParser.amazonServiceKey = serviceKey;
		ConfigurationParser.amazonServiceSecret = serviceSecret;
		ConfigurationParser.amazonVNCAMI = vncAmi;
	}
	
	public void addIGWConfiguration(String url, String outputDir) {
		ConfigurationParser.igwURL = url;
		ConfigurationParser.igwOutputDir = outputDir;
	}
	
	public void addGeneralConfiguration(String debug) {
		if (debug.trim().equalsIgnoreCase("true")) {
			ConfigurationParser.debug = true;	
		} else {
			ConfigurationParser.debug = false;
		}
		 
	}
	
	/**
	 * Inicia la lectura del archivo de configuracion.
	 * @param filename
	 */
	public static void parse(String filename) throws Exception {
		new ConfigurationParser(filename);
	}
}
