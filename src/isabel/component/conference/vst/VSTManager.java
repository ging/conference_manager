package isabel.component.conference.vst;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.scheduler.jobs.VNCManager;
import isabel.component.conference.util.ConfigurationParser;

public class VSTManager implements VNCManager {
	public void changeSharedFolder(IsabelMachine vnc, Conference conference) {
		if (!ConfigurationParser.debug) {
			ShareProp share = new ShareProp(vnc.getHostname(), ConfigurationParser.sambaRoot + conference.getPath(), "Directory for " + vnc.getHostname());
			SmbWizard.getInstance().createOrUpdate(vnc.getHostname(), share);
		}
	}

	public void setDefaultFolder(IsabelMachine vnc) {
		if (!ConfigurationParser.debug) {
			ShareProp share = new ShareProp(vnc.getHostname(), ConfigurationParser.sambaDefault, "Directory for " + vnc.getHostname());
			SmbWizard.getInstance().createOrUpdate(vnc.getHostname(), share);
		}
	}
}
