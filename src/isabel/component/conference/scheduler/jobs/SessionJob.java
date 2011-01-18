package isabel.component.conference.scheduler.jobs;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.Session;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SessionJob implements Job {
	protected Session session;
	protected Conference conference;
	
	protected static Controller controller;
	
	public SessionJob() {
		super();
	}
	
	public SessionJob(Conference conference, Session session) {
		super();
		this.session = session;
		this.conference = conference;
	}
	
	public Session getSession() {
		return session;
	}
	
	public Conference getConference() {
		return conference;
	}
	
	public static void setController(Controller controller) {
		SessionJob.controller = controller;
	}
	
	public static Controller getController() {
		return controller;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
	}
}
