package isabel.component.conference;

import isabel.component.conference.data.Conference;

public interface CloudManager {
	public void prepareConference(Conference conference);
	public void cleanConference(Conference conference);
}
