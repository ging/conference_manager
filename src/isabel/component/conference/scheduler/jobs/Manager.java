package isabel.component.conference.scheduler.jobs;

import isabel.component.conference.ist.SessionType;
import isabel.component.conference.ist.SiteType;
import isabel.lib.tasks.Task;
import isabel.lib.tasks.TaskListener;

import java.io.IOException;
import java.util.Map;

public interface Manager {

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
			TaskListener listener) throws IOException;
	
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
			String vncPassword, TaskListener listener) throws IOException;
	
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
			String chatURL, TaskListener listener) throws IOException;
	
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
			String chatURL, TaskListener listener) throws IOException;
	
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
			String videoBW, TaskListener listener) throws IOException;
	
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
			String siteName, TaskListener listener) throws IOException;
	
	/**
	 * Mata isabel en un host remoto
	 * 
	 * @param host
	 *            Host donde se mata a isabel
	 * @throws IOException
	 *             En caso de problemas al lanzar o conectar con el sitio
	 */
	public void killIsabel(String host) throws IOException;
}
