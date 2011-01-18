package isabel.component.conference.tests;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.IsabelMachine.SubType;
import isabel.component.conference.data.Session.Status;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.jobs.StartConferenceJob;
import isabel.component.conference.scheduler.jobs.StartSessionJob;

import java.util.Map;

public class Utils {
	public static Session createSession(String name, boolean enableStreaming, boolean automaticRecording, 
    		Long conferenceID, int start, int stop) {
    	Conference conference = ConferenceAssertions.getConference(conferenceID);
    	Session session = new Session();
    	session.setName(name);
    	session.setStartTime(TimeTestManager.getTime(start).getTime()-conference.getStartTime().getTime());
    	session.setStopTime(TimeTestManager.getTime(stop).getTime()-conference.getStartTime().getTime());
    	session.setEnableStreaming(enableStreaming);
    	session.setAutomaticRecord(automaticRecording);
    	return session;
    }
    
	public static Conference createConference(String name, String type, boolean enableIsabel, boolean enableWeb, 
    		boolean enableHTTPLiveStreaming, boolean enableSIP, int start, int stop) {
    	Conference conference1 = new Conference(name);
		conference1.setType(type);
		conference1.setEnableIsabel(enableIsabel);
		conference1.setEnableWeb(enableWeb);
		conference1.setEnableHTTPLiveStreaming(enableHTTPLiveStreaming);
		conference1.setEnableSIP(enableSIP);
		conference1.setStartTime(TimeTestManager.getTime(start));
		conference1.setStopTime(TimeTestManager.getTime(stop));
		return conference1;
    }
    
	public static IsabelMachine addIsabelMachine(String hostname, SubType type) {
    	IsabelMachine machine = new IsabelMachine();
		machine.setHostname(hostname);
		machine.setSubType(type);
		IsabelMachineRegistry.addIsabelMachine(machine);
		return machine;
    }
    
	public static Conference createCurrentConference(Conference conference) {
	    	// Lo guardamos en la base de datos.
	    	ConferenceRegistry.add(conference);
	    	conference = ConferenceRegistry.get(conference.getId());
	    	
	    	// Arrancamos todo
	    	StartConferenceJob confStart = new StartConferenceJob();
	   		confStart.startConference(conference);
	   		
	   		// Programamos el final.
	   		IsabelScheduler.getInstance().getJobScheduler().scheduleStopConferenceJob(conference);
	   		
	   		
   		return conference;
    }
    
	public static Session createCurrentSession(Conference conference, Session session) {
    	
    	// Lo guardamos en la base de datos.
    	ConferenceRegistry.addSessionToConference(conference, session);
    	conference = ConferenceRegistry.get(conference.getId());
    	session = conference.getSession().get(0);
    	
    	// Arrancamos todo
    	StartSessionJob sesStart = new StartSessionJob(conference,session);
    	if (session.isAutomaticRecord() || session.getStatus().equals(Status.RECORDING)) {
    		sesStart.startSession();
    	}
    	
    	// Programamos el final.
    	IsabelScheduler.getInstance().getJobScheduler().scheduleSessionJobs(session);
    	
    	return session;
    }
    
	public static Conference checkCreateConference(int start, int stop, boolean yesOrNot) {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, start, stop);
    	if (yesOrNot) {
    		return ConferenceAssertions.isCreate("Conference could not be created", conference);
    	} else {
    		ConferenceAssertions.isNotCreate("Conference could not be created", conference);
    	}
    	return null;
    }
    
	public static Session checkCreateSession(Long conferenceID, int start, int stop, boolean yesOrNot) {
    	Session session = createSession("uno", true, true, conferenceID, start, stop);
    	if (yesOrNot) {
    		return ConferenceAssertions.isSessionAdded("Session could not be added", conferenceID, session);
    	} else {
    		ConferenceAssertions.isNotSessionAdded("Session was added", conferenceID, session);
    		return null;
    	}
    }
    
	public static void isOverlap(int start1, int stop1, int start2, int stop2, boolean checkOverlap) {
    	checkCreateConference(start1, stop1, true);
    	checkCreateConference(start2, stop2, !checkOverlap);
    }
    
	public static void isOverlapExtending(int start1, int stop1, int start2, int stop2, int start3, int stop3, boolean checkOverlap) {
    	checkCreateConference(start1, stop1, true);
    	Conference newConference = checkCreateConference(start2, stop2, true);
    	
    	checkChangeConference(newConference.getId(), start3, stop3, !checkOverlap);
    }
    
	public static void checkChangeConferenceWithSession(Long conferenceID, int startc1, int stopc1, Map<Long, int[]> sessions, boolean yesOrNot) {
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, startc1, stopc1);
    	if (yesOrNot) {
    		ConferenceAssertions.isChanged("Could not change conference", newConference, conferenceID);
    	} else {
    		ConferenceAssertions.isNotChanged("Could not change conference", newConference, conferenceID);
    	}
    	for (Long sessionID : sessions.keySet()) {
    		Session newSession = createSession("uno", true, true, conferenceID, sessions.get(sessionID)[0], sessions.get(sessionID)[1]);
        	newSession.setConference(ConferenceAssertions.getConference(conferenceID));
        	ConferenceAssertions.isSessionDateEqualDataBase("Session has not changed dates", conferenceID, sessionID, newSession);
    	}
    }
    
	public static void checkChangeSessionAndDates(Long conferenceID, Long sessionID, int start, int stop, boolean yesOrNot) {
    	checkChangeSessionAndDates(conferenceID, sessionID, true, start, stop, yesOrNot);
    }
    
	public static void checkChangeSessionAndDates(Long conferenceID, Long sessionID, boolean automaticRecording, int start, int stop, boolean yesOrNot) {
    	Session newSession = createSession("uno", true, automaticRecording, conferenceID, start, stop);
    	if (yesOrNot) {
    		ConferenceAssertions.isSessionChanged("Session could not be added", conferenceID, sessionID, newSession);
    		ConferenceAssertions.isSessionDateEqualDataBase("Dates are bad", conferenceID, sessionID, newSession);
    	} else {
    		ConferenceAssertions.isSessionNotChanged("Session could not be added", conferenceID, sessionID, newSession);
    		ConferenceAssertions.isSessionDateNotEqualDataBase("Dates are bad", conferenceID, sessionID, newSession);
    	}
    }
    
	public static void checkChangeSessionAndStop(Long conferenceID, Long sessionID, int start, int stop, boolean yesOrNotChange, boolean yesOrNotStop) {
    	checkChangeSessionAndStop(conferenceID, sessionID, true, start, stop, yesOrNotChange, yesOrNotStop);
    	
    }
    
	public static void checkChangeSessionAndStop(Long conferenceID, Long sessionID, boolean automaticRecording, int start, int stop, boolean yesOrNotChange, boolean yesOrNotStop) {
    	Session newSession = createSession("sesUno", true, automaticRecording, conferenceID, start, stop);
    	if (yesOrNotChange) {
    		ConferenceAssertions.isSessionChanged("Session could not be added", conferenceID, sessionID, newSession);
    	} else {
    		ConferenceAssertions.isSessionNotChanged("Session was added", conferenceID, sessionID, newSession);
    	}
    	if (yesOrNotStop) {
    		ConferenceAssertions.isSessionStopDateEqualDataBase("Stop data was not changed", conferenceID, sessionID, newSession);
    	} else {
    		ConferenceAssertions.isSessionStopDateNotEqualDataBase("Stop data could not be changed", conferenceID, sessionID, newSession);
    	}
    	
    }
    
	public static void checkChangeConferenceAndStop(Long conferenceID, int start, int stop, boolean yesOrNotChange, boolean yesOrNotStop) {
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, start, stop);
    	if (yesOrNotChange) {
    		ConferenceAssertions.isChanged("Conference has been returned to past", newConference, conferenceID);
    	} else {
    		ConferenceAssertions.isNotChanged("Conference has been returned to past", newConference, conferenceID);
    	}
    	if (yesOrNotStop) {
    		ConferenceAssertions.isConferenceStopDateEqualDataBase("Stop conference date has been changed", conferenceID, newConference);
    	} else {
    		ConferenceAssertions.isConferenceStopDateNotEqualDataBase("Stop conference date has been changed", conferenceID, newConference);
    	}
    }
    
	public static void checkChangeConference(Long conferenceID, int start, int stop, boolean yesOrNot) {
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, start, stop);
    	if (yesOrNot) {
    		ConferenceAssertions.isChanged("Conference has been returned to past", newConference, conferenceID);
    	} else {
    		ConferenceAssertions.isNotChanged("Conference has been returned to past", newConference, conferenceID);
    	}
    }
}
