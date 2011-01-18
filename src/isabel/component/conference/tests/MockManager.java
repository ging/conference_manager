package isabel.component.conference.tests;

import isabel.component.conference.ist.SessionType;
import isabel.component.conference.ist.SiteType;
import isabel.component.conference.scheduler.jobs.Manager;
import isabel.lib.tasks.Task;
import isabel.lib.tasks.TaskListener;

import java.io.IOException;
import java.util.Map;

public class MockManager implements Manager {

	@Override
	public void killIsabel(String host) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task launchGatewayFlash(String host, SessionType type,
			String siteName, String sessionName, String masterip,
			String flashURL, String bitrate, boolean h264, String metadataURL,
			String vncServer, String vncPassword, TaskListener listener)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task launchGatewaySIP(String host, String siteName,
			String sessionName, String masterip, String registerAddress,
			String registerPort, String clientAddress, String clientName,
			String clientNickname, String realm, String password,
			String videoBW, TaskListener listener) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task launchIsabel(String host, SessionType sessionType,
			SiteType siteType, String quality, String sessionName,
			String siteName, String masterip, Map<String, String> params,
			TaskListener listener) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task launchMaster(String host, SessionType sessionType,
			SiteType siteType, String quality, String sessionName,
			String siteName, TaskListener listener) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task launchMasterGatewayFlash(String host, SessionType type,
			String siteName, String sessionName, String quality,
			String flashURL, String bitrate, boolean h264, String metadataURL,
			String vncServer, String vncPassword, String chatURL,
			TaskListener listener) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task launchMasterGatewayFlashHTTPLiveStreaming(String host,
			SessionType type, String siteName, String sessionName,
			String quality, String flashURL, String bitrate, boolean h264,
			String metadataURL, String vncServer, String vncPassword,
			String igwBaseURL, String igwPrefix, String igwVBitrate,
			String chatURL, TaskListener listener) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
}