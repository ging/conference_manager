package isabel.component.conference.tests;

import isabel.component.conference.scheduler.jobs.Controller;

public class MockController implements Controller {

	@Override
	public boolean isRecording(String conference, String session) {
		return false;
	}

	@Override
	public boolean publishRecording(String streamURL,
			String streamName, String conference, String session) {
		return false;
	}

	@Override
	public boolean startRecording(String streamURL, String streamName,
			String conference, String session) {
		return false;
	}

	@Override
	public boolean stopRecording(String streamURL, String streamName,
			String conference, String session) {
		return false;
	}
	
}