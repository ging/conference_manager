package isabel.component.conference.tests;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.IsabelMachine.SubType;
import isabel.component.conference.data.Session.Status;
import isabel.component.conference.scheduler.jobs.ConferenceJob;
import isabel.component.conference.scheduler.jobs.SessionJob;

import java.util.HashMap;

import junit.framework.TestCase;

public class ConferencesTest extends TestCase {

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    protected void setUp() {
    	Utils.addIsabelMachine("isabel1", SubType.vmIsabel);
    	Utils.addIsabelMachine("isabel2", SubType.vmIsabel);
    	Utils.addIsabelMachine("vnc1", SubType.vmVNC);
    	TimeTestManager.init();
    	ConferenceJob.setManager(new MockManager());
    	ConferenceJob.setVNCManager(new MockVNCManager());
    	SessionJob.setController(new MockController());
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
    	Utils.checkCreateConference(10, 12, true);
    	Utils.checkCreateConference(-1, 1, false);
    	Utils.checkCreateConference(-11, -10, false);
    }
    
    /**
     * Tests if we can remove Conferences.
     */
    public void testRemoveConference() {
    	Conference oldConference = Utils.checkCreateConference(1, 2, true);
    	ConferenceAssertions.isRemove("Could not delete conference", oldConference.getId());
    }
    
    /**
     * Tests if we can change the dates of past conferences.
     */
    public void testChangePastConference() {
    	Conference oldConference = Utils.checkCreateConference(1, 2, true);
    	
    	Utils.checkChangeConference(oldConference.getId(), -2, 2, false);
    	Utils.checkChangeConference(oldConference.getId(), -2, -1, false);
    }
    
    /**
     * Tests if we can add session to a conference.
     */
    public void testAddSessionToConference() {
    	Conference oldConference = Utils.checkCreateConference(1, 6, true);
    	Utils.checkCreateSession(oldConference.getId(), 2, 3, true);
    	Utils.checkCreateSession(oldConference.getId(), 4, 7, false);
    	Utils.checkCreateSession(oldConference.getId(), 4, 6, true);
    	Utils.checkCreateSession(oldConference.getId(), 2, 6, true);
    	Utils.checkCreateSession(oldConference.getId(), 1, 4, true);
    }
    
    /**
     * Tests if we can change session dates in a conference
     */
    public void testChangeSessionInConference() {
    	Conference conference = Utils.checkCreateConference(1, 6, true);
    	Session session = Utils.checkCreateSession(conference.getId(), 2, 3, true);
    	Utils.checkChangeSessionAndDates(conference.getId(), session.getId(), 2, 4, true);
    	Utils.checkChangeSessionAndDates(conference.getId(), session.getId(),  3, 4, true);
    	Utils.checkChangeSessionAndDates(conference.getId(), session.getId(),  2, 4, true);
    	Utils.checkChangeSessionAndDates(conference.getId(), session.getId(),  2, 3, true);
    	Utils.checkChangeSessionAndDates(conference.getId(), session.getId(),  4, 5, true);
    	Utils.checkChangeSessionAndDates(conference.getId(), session.getId(),  2, 3, true);
    }
    
    /**
     * Tests if we can change the conference dates.
     */
    public void testChangeVoidConference() {
    	Conference oldConference = Utils.checkCreateConference(1, 2, true);
    	Utils.checkChangeConference(oldConference.getId(), 1, 3, true);
    	Utils.checkChangeConference(oldConference.getId(), 2, 3, true);
    	Utils.checkChangeConference(oldConference.getId(), 1, 3, true);
    	Utils.checkChangeConference(oldConference.getId(), 1, 2, true);
    	Utils.checkChangeConference(oldConference.getId(), 4, 5, true);
    	Utils.checkChangeConference(oldConference.getId(), 1, 3, true);
    	Utils.checkChangeConference(oldConference.getId(), -1, 3, false);
    	Utils.checkChangeConference(oldConference.getId(), 3, 1, false);
    	Utils.checkChangeConference(oldConference.getId(), 3, 3, false);
    }
    
    /**
     * Tests if the system correctly detects overlaps.
     */
    public void testAddTwoConferences() {
    	Utils.isOverlap(1,2,3,4,false);
    	Utils.isOverlap(11,13,12,14,true);
    	Utils.isOverlap(22,24,21,23,true);
    	Utils.isOverlap(31,34,32,33,true);
    	Utils.isOverlap(42,43,41,44,true);
    	Utils.isOverlapExtending(51,53,54,56,55,56,false);
    	Utils.isOverlapExtending(61,63,64,66,62,66,true);
    	Utils.isOverlapExtending(74,76,71,73,71,75,true);
    	Utils.isOverlapExtending(83,86,81,82,84,85,true);
    	Utils.isOverlapExtending(94,95,91,92,93,96,true);
    }
    
    /**
     * Tests if the system correctly change session dates when we
     * change conference dates.
     */
    public void testChangeConferenceTimesWithSessions() {
    	Conference oldConference = Utils.checkCreateConference(1, 4, true);
    	Session session = Utils.checkCreateSession(oldConference.getId(), 2, 3, true);
    	HashMap<Long, int[]> sessions = new HashMap<Long, int[]>();
    	int date[] = {2,3};
    	sessions.put(session.getId(), date);
    	
    	//checkChanged(Conferencia que tenga ID, inicioConferencia, paradaConferencia, loQueDebeSerInicioSesion, loQueDebeSerParadaSesion);
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 1, 6, sessions, true);
    	
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 1, 4, sessions, true);
    	date[0]=3;date[1]=4;
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 2, 6, sessions, true);
    	date[0]=2;date[1]=3;
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 1, 6, sessions, true);
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 1, 2, sessions, false);
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 5, 6, sessions, false);
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 4, 5, sessions, false);
    	Utils.checkChangeConferenceWithSession(oldConference.getId(), 1, 6, sessions, true);
    	
    }
    
    /**
     * Tests if we can change dates of a conference that is running
     */
    public void testChangeCurrentConference() {
    	Conference oldConference = Utils.createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = Utils.createCurrentConference(oldConference);
   		
   		Utils.checkChangeConferenceAndStop(oldConference.getId(), -2, 6, true, true);
   		Utils.checkChangeConferenceAndStop(oldConference.getId(), -2, 3, true, true);
   		Utils.checkChangeConferenceAndStop(oldConference.getId(), -2, -1, false, false);
   		Utils.checkChangeConferenceAndStop(oldConference.getId(), -1, 3, false, true);
   		Utils.checkChangeConferenceAndStop(oldConference.getId(), -2, 6, true, true);
    }
    
    /**
     * Tests if we can change dates of a session that is running
     */
    public void testChangeCurrentAutomaticSessionInConference() {
    	Conference oldConference = Utils.createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = Utils.createCurrentConference(oldConference);
    	
    	Session oldSession = Utils.createSession("sesUno", false, true, oldConference.getId(), -1, 2);
    	oldSession = Utils.createCurrentSession(oldConference, oldSession);
    	
    	Utils.checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -1, 3, true, true);
    	Utils.checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -1, 2, true, true);
    	Utils.checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -2, 2, false, true);
    	Utils.checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -2, 3, false, false);
    	Utils.checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), -1, 1, true, true);
    	
    }
    
    /**
     * Tests if SessionStatus functions correctly.
     */
    public void testChangeCurrentManualSessionInConference() {
    	Conference oldConference = Utils.createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = Utils.createCurrentConference(oldConference);
    	
    	Session oldSession = Utils.createSession("sesUno", false, false, oldConference.getId(), -1, 2);
    	oldSession = Utils.createCurrentSession(oldConference, oldSession);
    	
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
    	Conference oldConference = Utils.createConference("uno", "meeting", true, true, false, false, -2, 4);
   		oldConference = Utils.createCurrentConference(oldConference);
    	
    	Session oldSession = Utils.createSession("sesUno", false, false, oldConference.getId(), -1, 2);
    	oldSession = Utils.createCurrentSession(oldConference, oldSession);
    	
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.INIT, Status.RECORDING, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDING, Status.RECORDED, true);
    	ConferenceAssertions.isManualSessionChanged(oldConference.getId(), oldSession.getId(), Status.RECORDED, Status.PUBLISHED, true);
    	Utils.checkChangeSessionAndStop(oldConference.getId(), oldSession.getId(), false, -1, 1, true, true);
    	
    	ConferenceAssertions.isManualSessionStatus(oldConference.getId(), oldSession.getId(), Status.PUBLISHED, true);
    }
    
}
