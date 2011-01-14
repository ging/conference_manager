package isabel.component.conference.tests;

import java.util.HashMap;
import java.util.Map;

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
import junit.framework.TestCase;

public class ConferencesTest extends TestCase {

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    protected void setUp() {
    	addIsabelMachine("isabel1", SubType.vmIsabel);
    	addIsabelMachine("isabel2", SubType.vmIsabel);
    	addIsabelMachine("vnc1", SubType.vmVNC);
    	TimeTestManager.init();
    }
    
    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() {
    	TestConfigurator.initializeAll();
    }
    
    /**
     * Tests the creation of conferences. In the future and in the past.
     */
    public void testCreateConference() {
    	checkCreateConference(10, 12, true);
    	checkCreateConference(-1, 1, false);
    	checkCreateConference(-11, -10, false);
    }
    
    /**
     * Tests if we can remove Conferences.
     */
    public void testRemoveConference() {
    	Conference oldConference = checkCreateConference(1, 2, true);
    	ConferenceAssertions.isRemove("Could not delete conference", oldConference.getId());
    }
    
    /**
     * Tests if we can change the dates of past conferences.
     */
    public void testChangePastConference() {
    	Conference oldConference = checkCreateConference(1, 2, true);
    	
    	checkChangeConference(oldConference.getId(), -2, 2, false);
    	checkChangeConference(oldConference.getId(), -2, -1, false);
    }
    
    /**
     * Tests if we can add session to a conference.
     */
    public void testAddSessionToConference() {
    	Conference oldConference = checkCreateConference(1, 6, true);
    	checkCreateSession(oldConference.getId(), 2, 3, true);
    	checkCreateSession(oldConference.getId(), 4, 7, false);
    	checkCreateSession(oldConference.getId(), 4, 6, true);
    	checkCreateSession(oldConference.getId(), 2, 6, true);
    	checkCreateSession(oldConference.getId(), 1, 4, true);
    }
    
    /**
     * Tests if we can change session dates in a conference
     */
    public void testChangeSessionInConference() {
    	Conference conference = checkCreateConference(1, 6, true);
    	Session session = checkCreateSession(conference.getId(), 2, 3, true);
    	checkChangeSessionAndDates(conference.getId(), session.getId(), 2, 4, true);
    	checkChangeSessionAndDates(conference.getId(), session.getId(),  3, 4, true);
    	checkChangeSessionAndDates(conference.getId(), session.getId(),  2, 4, true);
    	checkChangeSessionAndDates(conference.getId(), session.getId(),  2, 3, true);
    	checkChangeSessionAndDates(conference.getId(), session.getId(),  4, 5, true);
    	checkChangeSessionAndDates(conference.getId(), session.getId(),  2, 3, true);
    }
    
    /**
     * Tests if we can change the conference dates.
     */
    public void testChangeVoidConference() {
    	Conference oldConference = checkCreateConference(1, 2, true);
    	checkChangeConference(oldConference.getId(), 1, 3, true);
    	checkChangeConference(oldConference.getId(), 2, 3, true);
    	checkChangeConference(oldConference.getId(), 1, 3, true);
    	checkChangeConference(oldConference.getId(), 1, 2, true);
    	checkChangeConference(oldConference.getId(), 4, 5, true);
    	checkChangeConference(oldConference.getId(), 1, 3, true);
    	checkChangeConference(oldConference.getId(), -1, 3, false);
    	checkChangeConference(oldConference.getId(), 3, 1, false);
    	checkChangeConference(oldConference.getId(), 3, 3, false);
    }
    
    /**
     * Tests if the system correctly detects overlaps.
     */
    public void testAddTwoConferences() {
    	isOverlap(1,2,3,4,false);
    	isOverlap(11,13,12,14,true);
    	isOverlap(22,24,21,23,true);
    	isOverlap(31,34,32,33,true);
    	isOverlap(42,43,41,44,true);
    	isOverlapExtending(51,53,54,56,55,56,false);
    	isOverlapExtending(61,63,64,66,62,66,true);
    	isOverlapExtending(74,76,71,73,71,75,true);
    	isOverlapExtending(83,86,81,82,84,85,true);
    	isOverlapExtending(94,95,91,92,93,96,true);
    }
    
    /**
     * Tests if the system correctly change session dates when we
     * change conference dates.
     */
    public void testChangeConferenceTimesWithSessions() {
    	Conference oldConference = checkCreateConference(1, 4, true);
    	Session session = checkCreateSession(oldConference.getId(), 2, 3, true);
    	HashMap<Long, int[]> sessions = new HashMap<Long, int[]>();
    	int date[] = {2,3};
    	sessions.put(session.getId(), date);
    	
    	//checkChanged(Conferencia que tenga ID, inicioConferencia, paradaConferencia, loQueDebeSerInicioSesion, loQueDebeSerParadaSesion);
    	checkChangeConferenceWithSession(oldConference.getId(), 1, 6, sessions, true);
    	
    	checkChangeConferenceWithSession(oldConference.getId(), 1, 4, sessions, true);
    	date[0]=3;date[1]=4;
    	checkChangeConferenceWithSession(oldConference.getId(), 2, 6, sessions, true);
    	date[0]=2;date[1]=3;
    	checkChangeConferenceWithSession(oldConference.getId(), 1, 6, sessions, true);
    	checkChangeConferenceWithSession(oldConference.getId(), 1, 2, sessions, false);
    	checkChangeConferenceWithSession(oldConference.getId(), 5, 6, sessions, false);
    	checkChangeConferenceWithSession(oldConference.getId(), 4, 5, sessions, false);
    	checkChangeConferenceWithSession(oldConference.getId(), 1, 6, sessions, true);
    	
    }
    
    /**
     * Tests if we can change dates of a conference that is running
     */
    public void testChangeCurrentConference() {
    	Conference oldConference = createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = createCurrentConference(oldConference);
   		
    	checkChangeConferenceAndStop(oldConference.getId(), -2, 6, true, true);
    	checkChangeConferenceAndStop(oldConference.getId(), -2, 3, true, true);
    	checkChangeConferenceAndStop(oldConference.getId(), -2, -1, false, false);
    	checkChangeConferenceAndStop(oldConference.getId(), -1, 3, false, true);
    	checkChangeConferenceAndStop(oldConference.getId(), -2, 6, true, true);
    }
    
    /**
     * Tests if we can change dates of a session that is running
     */
    public void testChangeCurrentAutomaticSessionInConference() {
    	Conference oldConference = createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = createCurrentConference(oldConference);
    	
    	Session oldSession = createSession("sesUno", false, true, oldConference.getId(), -1, 2);
    	oldSession = createCurrentSession(oldConference, oldSession);
    	
    	checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -1, 3, true, true);
    	checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -1, 2, true, true);
    	checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -2, 2, false, true);
    	checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -2, 3, false, false);
    	checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -1, 1, true, true);
    	
    }
    
    /**
     * Tests if SessionStatus functions correctly.
     */
    public void testChangeCurrentManualSessionInConference() {
    	Conference oldConference = createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = createCurrentConference(oldConference);
    	
    	Session oldSession = createSession("sesUno", false, false, oldConference.getId(), -1, 2);
    	oldSession = createCurrentSession(oldConference, oldSession);
    	
    	ConferenceAssertions.isManualSessionStatus(oldConference.getId(), oldSession.getId(), Status.INIT, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.INIT, Status.RECORDED, false);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.INIT, Status.PUBLISHED, false);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.INIT, Status.RECORDING, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDING, Status.INIT, false);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDING, Status.PUBLISHED, false);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDING, Status.RECORDED, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDED, Status.INIT, false);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDED, Status.RECORDING, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDING, Status.RECORDED, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDED, Status.PUBLISHED, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.PUBLISHED, Status.INIT, false);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.PUBLISHED, Status.RECORDING, false);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.PUBLISHED, Status.RECORDED, false);
    }
    
    public void testChangePublishedSession() {
    	Conference oldConference = createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = createCurrentConference(oldConference);
    	
    	Session oldSession = createSession("sesUno", false, false, oldConference.getId(), -1, 2);
    	oldSession = createCurrentSession(oldConference, oldSession);
    	
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.INIT, Status.RECORDING, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDING, Status.RECORDED, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDED, Status.PUBLISHED, true);
    	checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), false, -1, 1, true, true);
    	
    	ConferenceAssertions.isManualSessionStatus(oldConference.getId(), oldSession.getId(), Status.PUBLISHED, true);
    }
    
    /* Clases de ayuda para los tests */
    
    private Session createSession(String name, boolean enableStreaming, boolean automaticRecording, 
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
    
    private Conference createConference(String name, String type, boolean enableIsabel, boolean enableWeb, 
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
    
    private IsabelMachine addIsabelMachine(String hostname, SubType type) {
    	IsabelMachine machine = new IsabelMachine();
		machine.setHostname(hostname);
		machine.setSubType(type);
		IsabelMachineRegistry.addIsabelMachine(machine);
		return machine;
    }
    
    private Conference createCurrentConference(Conference conference) {
    	try {
	    	// Lo guardamos en la base de datos.
	    	ConferenceRegistry.add(conference);
	    	conference = ConferenceRegistry.get(conference.getId());
	    	
	    	// Arrancamos todo
	    	StartConferenceJob confStart = new StartConferenceJob();
	   		confStart.startConference(conference);
	   		
	   		// Programamos el final.
	   		IsabelScheduler.getInstance().getJobScheduler().scheduleStopConferenceJob(conference);
	   		
	   		
    	} catch(Exception e) {
   			assertTrue("Error creating current Conference",false);
   		}
   		return conference;
    }
    
    private Session createCurrentSession(Conference conference, Session session) {
    	
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
    
    private Conference checkCreateConference(int start, int stop, boolean yesOrNot) {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, start, stop);
    	if (yesOrNot) {
    		return ConferenceAssertions.isCreate("Conference could not be created", conference);
    	} else {
    		ConferenceAssertions.isNotCreate("Conference could not be created", conference);
    	}
    	return null;
    }
    
    private Session checkCreateSession(Long conferenceID, int start, int stop, boolean yesOrNot) {
    	Session session = createSession("uno", true, true, conferenceID, start, stop);
    	if (yesOrNot) {
    		return ConferenceAssertions.isSessionAdded("Session could not be added", conferenceID, session);
    	} else {
    		ConferenceAssertions.isNotSessionAdded("Session was added", conferenceID, session);
    		return null;
    	}
    }
    
    private void isOverlap(int start1, int stop1, int start2, int stop2, boolean checkOverlap) {
    	checkCreateConference(start1, stop1, true);
    	checkCreateConference(start2, stop2, !checkOverlap);
    }
    
    public void isOverlapExtending(int start1, int stop1, int start2, int stop2, int start3, int stop3, boolean checkOverlap) {
    	checkCreateConference(start1, stop1, true);
    	Conference newConference = checkCreateConference(start2, stop2, true);
    	
    	checkChangeConference(newConference.getId(), start3, stop3, !checkOverlap);
    }
    
    private void checkChangeConferenceWithSession(Long conferenceID, int startc1, int stopc1, Map<Long, int[]> sessions, boolean yesOrNot) {
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
    
    private void checkChangeSessionAndDates(Long conferenceID, Long sessionID, int start, int stop, boolean yesOrNot) {
    	checkChangeSessionAndDates(conferenceID, sessionID, true, start, stop, yesOrNot);
    }
    
    private void checkChangeSessionAndDates(Long conferenceID, Long sessionID, boolean automaticRecording, int start, int stop, boolean yesOrNot) {
    	Session newSession = createSession("uno", true, automaticRecording, conferenceID, start, stop);
    	if (yesOrNot) {
    		ConferenceAssertions.isSessionChanged("Session could not be added", conferenceID, sessionID, newSession);
    		ConferenceAssertions.isSessionDateEqualDataBase("Dates are bad", conferenceID, sessionID, newSession);
    	} else {
    		ConferenceAssertions.isSessionNotChanged("Session could not be added", conferenceID, sessionID, newSession);
    		ConferenceAssertions.isSessionDateNotEqualDataBase("Dates are bad", conferenceID, sessionID, newSession);
    	}
    }
    
    private void checkChangeSessionAndStop(Long conferenceID, Long sessionID, int start, int stop, boolean yesOrNotChange, boolean yesOrNotStop) {
    	checkChangeSessionAndStop(conferenceID, sessionID, true, start, stop, yesOrNotChange, yesOrNotStop);
    	
    }
    
    private void checkChangeSessionAndStop(Long conferenceID, Long sessionID, boolean automaticRecording, int start, int stop, boolean yesOrNotChange, boolean yesOrNotStop) {
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
    
    private void checkChangeConferenceAndStop(Long conferenceID, int start, int stop, boolean yesOrNotChange, boolean yesOrNotStop) {
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
    
    private void checkChangeConference(Long conferenceID, int start, int stop, boolean yesOrNot) {
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, start, stop);
    	if (yesOrNot) {
    		ConferenceAssertions.isChanged("Conference has been returned to past", newConference, conferenceID);
    	} else {
    		ConferenceAssertions.isNotChanged("Conference has been returned to past", newConference, conferenceID);
    	}
    }
    
}
