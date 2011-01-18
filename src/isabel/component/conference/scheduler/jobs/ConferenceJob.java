package isabel.component.conference.scheduler.jobs;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ConferenceJob  implements Job {
	
	/**
	 * Conferencia sobre la que actuaremos.
	 */
	protected Conference conference;

	/**
	 * Maquina de master
	 */
	protected IsabelMachine master;
	
	/**
	 * Isabel machines Manager.
	 */
	protected static Manager manager;
	
	/**
	 * VNC machines Manager.
	 */
	protected static VNCManager vstManager;

	public static VNCManager getVNCManager() {
		return vstManager;
	}

	public static void setVNCManager(VNCManager vstManager) {
		ConferenceJob.vstManager = vstManager;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		
	}

	public Conference getConference() {
		return conference;
	}

	public void setConference(Conference conference) {
		this.conference = conference;
	}

	public IsabelMachine getMaster() {
		return master;
	}

	public void setMaster(IsabelMachine master) {
		this.master = master;
	}

	public static Manager getManager() {
		return manager;
	}

	public static void setManager(Manager manager) {
		ConferenceJob.manager = manager;
	}

}
