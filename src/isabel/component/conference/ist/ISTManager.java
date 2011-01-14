package isabel.component.conference.ist;

import isabel.component.conference.util.ConfigurationParser;
import isabel.lib.tasks.Task;
import isabel.lib.tasks.TaskListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IST Wrapper
 * 
 * @author Fernando Escribano
 */
public class ISTManager {

	/**
	 * Logs de la aplicacion
	 */
	protected static Logger log = LoggerFactory
			.getLogger(ISTManager.class);
	
	/**
	 * Usuario de isabel con arroba
	 */
	private static final String IST_USER = "isabel";

	/**
	 * Lanza isabel en un host remoto
	 * 
	 * @param host
	 *            Host en el que se va a arrancar isabel
	 * @param sessionType
	 *            Actividad (teleconference, telemeeting, etc..)
	 * @param siteType
	 *            Rol del sitio (Master, Flashgateway, etc..)
	 * @param quality
	 *            Ancho de banda de la sesion (512k, 1M, 2M, etc..)
	 * @param sessionName
	 *            Nombre de la sesion
	 * @param siteName
	 *            Nombre del sitio
	 * @param masterip
	 *            IP donde se va a conectar. null si el sitio es master.
	 * @param params
	 *            Map de parametros de la sesion
	 * @param listener
	 *            TaskListener al que se avisa cuando se muere la tarea
	 * @return Objeto Task que representa el proceso.
	 * @throws IOException
	 *             En caso de problemas al lanzar o conectar con el sitio
	 */
	public Task launchIsabel(String host, SessionType sessionType,
			SiteType siteType, String quality, String sessionName,
			String siteName, String masterip, Map<String, String> params,
			TaskListener listener) throws IOException {

		String master = "";

		if (masterip != null) {
			master += masterip;
		}

		String ist_cmd = sessionName + " " + sessionType.getName() + " "
				+ quality + " " + siteType + " " + siteName + " " + master
				+ " " + getParametersString(params);

		String scriptName = "ist/remoteIsabel";

		String[] cmd;
		
		if (params.containsKey("IGW_BASE_URL")) {
			String[] temp = { scriptName, IST_USER, host, "START", ist_cmd, params.get("IGW_BASE_URL") };
			cmd = temp;
			log.debug("Running IST launch: " + scriptName + " " + IST_USER + " " + host + " START \"" + ist_cmd + "\" " + params.get("IGW_BASE_URL"));
		}  else {
			String[] temp = { scriptName, IST_USER, host, "START", ist_cmd };
			cmd = temp;
			log.debug("Running IST launch: " + scriptName + " " + IST_USER + " " + host + " START \"" + ist_cmd + "\"");
		}
		
		if (!ConfigurationParser.debug) {
			Task t = new Task(cmd, false);
			if (listener != null)
				t.addTaskListener(listener);
			t.start();
			return t;
		} else {
			return null;
		}

	}

	/**
	 * @param host
	 * @param siteName
	 * @param masterip
	 * @param flashURL
	 * @param bitrate
	 * @param listener
	 * @param vncServer
	 * @param vncPassword
	 * @return
	 * @throws IOException
	 */
	public Task launchGatewayFlash(String host, SessionType type, String siteName,
			String sessionName, String masterip, String flashURL, String bitrate, boolean h264, 
			String metadataURL, String vncServer,
			String vncPassword, TaskListener listener) throws IOException {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put("FLASH_BITRATE", bitrate);
		params.put("FLASH_SERVER_URL", flashURL);
		params.put("FLASH_VNC_SERVER", vncServer);
		params.put("FLASH_VNC_PASSWORD", vncPassword);
		if (metadataURL != null) {
			params.put("FLASH_METADATA_URL", metadataURL);
		}
		
		if (h264) {
			params.put("FLASH_USE_H.264", "yes");
		}

		return launchIsabel(host, type, SiteType.FLASH, "2M",
				sessionName, siteName, masterip, params, listener);
	}
	
	/**
	 * @param host
	 * @param siteName
	 * @param masterip
	 * @param flashURL
	 * @param bitrate
	 * @param listener
	 * @param vncServer
	 * @param vncPassword
	 * @return
	 * @throws IOException
	 */
	public Task launchMasterGatewayFlash(String host, SessionType type, String siteName,
			String sessionName, String quality, String flashURL, String bitrate, boolean h264, 
			String metadataURL, String vncServer,
			String vncPassword, 
			String chatURL, TaskListener listener) throws IOException {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put("FLASH_BITRATE", bitrate);
		params.put("FLASH_SERVER_URL", flashURL);
		params.put("FLASH_VNC_SERVER", vncServer);
		params.put("FLASH_VNC_PASSWORD", vncPassword);
		
		if (metadataURL != null && !metadataURL.equals("")) {
			params.put("FLASH_METADATA_URL", metadataURL);
		}
		
		if (h264) {
			params.put("FLASH_USE_H264", "yes");
			params.put("FLASH_USE_MP3", "yes");
		}
		
		params.put("ISABEL_VCC_CHAT_URL", chatURL);
		
		return launchIsabel(host, type, SiteType.MASTER_FLASH, quality,
				sessionName, siteName, null, params, listener);
	}
	
	/**
	 * @param host
	 * @param siteName
	 * @param masterip
	 * @param flashURL
	 * @param bitrate
	 * @param listener
	 * @param vncServer
	 * @param vncPassword
	 * @return
	 * @throws IOException
	 */
	public Task launchMasterGatewayFlashHTTPLiveStreaming(String host, SessionType type, String siteName,
			String sessionName, String quality, String flashURL, String bitrate, boolean h264, 
			String metadataURL, String vncServer,
			String vncPassword, String igwBaseURL, 
			String igwPrefix, String igwVBitrate, 
			String chatURL, TaskListener listener) throws IOException {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put("FLASH_BITRATE", bitrate);
		params.put("FLASH_SERVER_URL", flashURL);
		params.put("FLASH_VNC_SERVER", vncServer);
		params.put("FLASH_VNC_PASSWORD", vncPassword);
		
		if (metadataURL != null && !metadataURL.equals("")) {
			params.put("FLASH_METADATA_URL", metadataURL);
		}
		
		if (h264) {
			params.put("FLASH_USE_H264", "yes");
			params.put("FLASH_USE_MP3", "yes");
		}
		
		params.put("IGW_ENABLE", "yes");
		params.put("IGW_BASE_URL", igwBaseURL);
		params.put("IGW_OUTPUT_DIR", ConfigurationParser.igwOutputDir);
		params.put("IGW_PREFIX", igwPrefix);
		params.put("IGW_VBITRATE", igwVBitrate);
		
		params.put("ISABEL_VCC_CHAT_URL", chatURL);
		
		return launchIsabel(host, type, SiteType.MASTER_FLASH, quality,
				sessionName, siteName, null, params, listener);
	}

	/**
	 * @param host
	 * @param siteName
	 * @param masterip
	 * @param listener
	 * @return
	 * @throws IOException
	 */
	public Task launchGatewaySIP(String host, String siteName, 
			String sessionName, String masterip,
			String registerAddress, String registerPort, String clientAddress,
			String clientName, String clientNickname, String realm, String password, 
			String videoBW, TaskListener listener) throws IOException {
		HashMap<String, String> params = new HashMap<String, String>();
		
		params.put("SIP_REGISTER_ADDRESS", registerAddress);
		params.put("SIP_REGISTER_PORT", registerPort);
		params.put("SIP_CLIENT_ADDRESS", clientAddress);
		params.put("SIP_CLIENT_NAME", clientName);
		params.put("SIP_CLIENT_NICKNAME", clientNickname);
		params.put("SIP_REALM", realm);
		params.put("SIP_CLIENT_PASSWORD", password);
		params.put("SIP_VIDEO_BW", videoBW);
		
		return launchIsabel(host, SessionType.TELECLASS, SiteType.SIP, "2M",
				sessionName, siteName, masterip, params, listener);
	}

	/**
	 * @param host
	 * @param sessionType
	 * @param siteType
	 * @param quality
	 * @param sessionName
	 * @param siteName
	 * @param listener
	 * @return
	 * @throws IOException
	 */
	public Task launchMaster(String host, SessionType sessionType,
			SiteType siteType, String quality, String sessionName,
			String siteName, TaskListener listener) throws IOException {
		return launchIsabel(host, sessionType, siteType, quality, sessionName,
				siteName, null, null, listener);
	}

	/**
	 * Mata isabel en un host remoto
	 * 
	 * @param host
	 *            Host donde se mata a isabel
	 * @throws IOException
	 *             En caso de problemas al lanzar o conectar con el sitio
	 */
	public void killIsabel(String host) throws IOException {

		String scriptName = "ist/remoteIsabel";

		String[] cmd = { scriptName, IST_USER, host, "STOP" };

		log.debug("Running IST kill: " + scriptName + " " + IST_USER + " " + host + " STOP");

		if (!ConfigurationParser.debug) {
			Task t = new Task(cmd, false);
			t.start();
		}
	}

	private String getParametersString(Map<String, String> params) {
		StringBuilder result = new StringBuilder(" ");
		if (params != null) {
			for (String s : params.keySet())
				result.append(s).append('=').append(params.get(s)).append(' ');
		}
		return result.toString();
	}

}
