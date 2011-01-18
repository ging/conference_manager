package isabel.component.conference.scheduler.jobs;

public interface Controller {
	public boolean startRecording(String streamURL, String streamName, String conference, String session);
	public boolean stopRecording(String streamURL, String streamName, String conference, String session);
	public boolean publishRecording(String streamURL, String streamName, String conference, String session);
	public boolean isRecording (String conference, String session);
}
