package isabel.component.conference.scheduler.jobs;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;

public interface VNCManager {
	public void changeSharedFolder(IsabelMachine vnc, Conference conference);
	public void setDefaultFolder(IsabelMachine vnc);
}
