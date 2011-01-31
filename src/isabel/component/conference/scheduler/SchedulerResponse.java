package isabel.component.conference.scheduler;

public class SchedulerResponse {
	public boolean ok;
	public String errorMessage;
	
	private SchedulerResponse(boolean ok, String errorMessage) {
		this.ok = ok;
		this.errorMessage = errorMessage;
	}
	
	public static SchedulerResponse ok() {
		return new SchedulerResponse(true, null);
	}
	
	public static SchedulerResponse error(String message) {
		return new SchedulerResponse(false, message);
	}
}
