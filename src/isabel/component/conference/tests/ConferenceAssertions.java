package isabel.component.conference.tests;

import java.util.Date;
import java.util.List;

import org.quartz.SchedulerException;

import junit.framework.TestCase;
import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.SessionStatus;
import isabel.component.conference.data.Session.Status;
import isabel.component.conference.rest.ConferenceResource;
import isabel.component.conference.rest.ConferencesResource;
import isabel.component.conference.rest.SessionResource;
import isabel.component.conference.rest.SessionStatusResource;
import isabel.component.conference.rest.SessionsResource;
import isabel.component.conference.scheduler.IsabelScheduler;

public class ConferenceAssertions extends TestCase {
	
	public static Conference isInDatabase(String message, Long conferenceID) {
		Conference oldConference = getConference(conferenceID);
    	assertTrue(message,oldConference!=null);
    	return oldConference;
	}
	
	public static void isNotInDatabase(String message, Long conferenceID) {
		Conference oldConference = getConference(conferenceID);
    	assertTrue(message,oldConference==null);
	}
	
	public static Conference isCreate(String message, Conference conference) {
		ResourceTestManager resManager = new ResourceTestManager();
		ConferencesResource conferencesResource = resManager.getConferencesResource();
    	Object conf = conferencesResource.createConference(conference);
    	assertTrue(message, conferencesResource.getResponse().getStatus().getCode() == 200);
    	assertTrue(message, conf instanceof Conference);
    	Conference oldConference = (Conference)conf;
    	return isInDatabase(message, oldConference.getId());
	}
	
	public static void isNotCreate(String message, Conference conference) {
		ResourceTestManager resManager = new ResourceTestManager();
		ConferencesResource conferencesResource = resManager.getConferencesResource();
    	conferencesResource.createConference(conference);
    	assertFalse(message, conferencesResource.getResponse().getStatus().getCode() == 200);
    	if (conference.getId() != null) {
    		isNotInDatabase(message, conference.getId());	
    	}
	}
	
	public static Session isSessionAdded(String message, Long conferenceID, Session session) {
		
		Conference oldConference = isInDatabase("Conference not in database", conferenceID);
		int initSize = oldConference.getSession().size();
		ResourceTestManager resManager = new ResourceTestManager();
		SessionsResource sessionsResource = resManager.getSessionsResource(oldConference.getId());
    	Object sess = sessionsResource.createSession(session);
    	assertTrue(message, sessionsResource.getResponse().getStatus().getCode() == 200);
    	assertTrue(message, sess instanceof Session);
    	Session sesRes = (Session)sess;
    	oldConference = getConference(conferenceID);
    	List<Session> sessions = oldConference.getSession();
    	assertTrue(message, sessions.size() == initSize+1);
    	
    	sesRes = getSession(conferenceID, sesRes.getId());
    	assertTrue(message, session.equals(sesRes));
    	isSessionDateEqualDataBase("Dates are wrong", conferenceID, sesRes.getId(), session);
    	return sesRes;
	}
	
	public static void isNotSessionAdded(String message, Long conferenceID, Session session) {
		Conference oldConference = isInDatabase("Conference not in database", conferenceID);
		int initSize = oldConference.getSession().size();
		ResourceTestManager resManager = new ResourceTestManager();
		SessionsResource sessionsResource = resManager.getSessionsResource(oldConference.getId());
    	sessionsResource.createSession(session);
    	assertFalse(message, sessionsResource.getResponse().getStatus().getCode() == 200);
    	oldConference = ConferenceRegistry.get(oldConference.getId());
    	List<Session> sessions = oldConference.getSession();
    	assertFalse(message, sessions.size() != initSize);
	}
	
	public static void isRemove(String message, Long conferenceID) {
		ResourceTestManager resManager = new ResourceTestManager();
		ConferenceResource conferenceResource = resManager.getConferenceResource(conferenceID);
    	conferenceResource.remove();
    	assertTrue(message, conferenceResource.getResponse().getStatus().getCode() == 200);
   		isNotInDatabase(message, conferenceID);	
	}
	
	public static Conference isChanged(String message, Conference newConference, Long conferenceID) {
		ResourceTestManager resManager = new ResourceTestManager();
		ConferenceResource conferenceResource = resManager.getConferenceResource(conferenceID);
    	conferenceResource.changeConference(newConference);
    	assertTrue(message, conferenceResource.getStatus().getCode() == 200);
    	Conference result = ConferenceRegistry.get(conferenceID);
    	return result;
	}
	
	public static void isNotChanged(String message, Conference newConference, Long conferenceID) {
		ResourceTestManager resManager = new ResourceTestManager();
		ConferenceResource conferenceResource = resManager.getConferenceResource(conferenceID);
    	conferenceResource.changeConference(newConference);
    	assertFalse(message, conferenceResource.getStatus().getCode() == 200);
	}
	
	public static void isConferenceStartDateEqualDatabase(String message, Long id, Conference newConference) {
		try {
			Conference oldConference = ConferenceRegistry.get(id);
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstart", "isabel").getStartTime();
			assertTrue(message, newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
			assertTrue(message, newConference.getResourcesStartTime().equals(startJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de inicio de evento", false);
		}
	}
	
	public static void isConferenceStartDateNotEqualDatabase(String message, Long id, Conference newConference) {
		try {
			Conference oldConference = ConferenceRegistry.get(id);
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstart", "isabel").getStartTime();
			assertFalse(message, newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
			assertFalse(message, newConference.getResourcesStartTime().equals(startJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de inicio de evento", false);
		}
	}
	
	public static void isConferenceStopDateEqualDataBase(String message, Long id, Conference newConference) {
		try {
			Conference oldConference = ConferenceRegistry.get(id);
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstop", "isabel").getStartTime();
			assertTrue(message, newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
			assertTrue(message, newConference.getResourcesStopTime().equals(stopJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de fin de evento", false);
		}
	}
	
	public static void isConferenceStopDateNotEqualDataBase(String message, Long id, Conference newConference) {
		try {
			Conference oldConference = ConferenceRegistry.get(id);
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstop", "isabel").getStartTime();
			assertFalse(message, newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
			assertFalse(message, newConference.getResourcesStopTime().equals(stopJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de fin de evento", false);
		}
	}
	
	public static void isConferenceDateEqualDataBase(String message, Conference newConference) {
		try {
			Conference oldConference = ConferenceRegistry.get(newConference.getId());
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstart", "isabel").getStartTime();
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstop", "isabel").getStartTime();
			assertTrue(message, newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
			assertTrue(message, newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
			assertTrue(message, newConference.getResourcesStartTime().equals(startJobDate));
			assertTrue(message, newConference.getResourcesStopTime().equals(stopJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de fin de evento", false);
		}
	}
	
	public static void isConfereceDateNotEqualDataBase(String message, Conference newConference) {
		try {
			Conference oldConference = ConferenceRegistry.get(newConference.getId());
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstart", "isabel").getStartTime();
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldConference.getId() + "confstop", "isabel").getStartTime();
			assertFalse(message, newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
			assertFalse(message, newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
			assertFalse(message, newConference.getResourcesStartTime().equals(startJobDate));
			assertFalse(message, newConference.getResourcesStopTime().equals(stopJobDate));
    	} catch (SchedulerException e) {
    		assertTrue("No se pudo obtener la hora de fin de evento", false);
		}
	}
	
	public static void isSessionStartDateEqualDataBase(String message, Conference conference, Session newSession) {
		Session oldSession = ConferenceRegistry.get(conference.getId()).getSession().get(0);
		assertTrue(message, newSession.getStartDate().equals(oldSession.getStartDate()));
    	try {
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstart", "isabel").getStartTime();
			assertTrue(message, newSession.getStartDate().equals(startJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de lanzamiento de evento", false);
		}
	}
	
	public static void isSessionStartDateNotEqualDataBase(String message, Long conferenceID, Long sessionID, Session newSession) {
		Session oldSession = getSession(conferenceID, sessionID);
		assertFalse(message, newSession.getStartDate().equals(oldSession.getStartDate()));
    	try {
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstart", "isabel").getStartTime();
			assertFalse(message, newSession.getStartDate().equals(startJobDate));
    	} catch (SchedulerException e) {
    		assertTrue("No se pudo obtener la hora de lanzamiento de evento", false);
		}
	}
	
	public static void isSessionStopDateEqualDataBase(String message, Long conferenceID, Long sessionID, Session newSession) {
		Session oldSession = getSession(conferenceID, sessionID);
    	assertTrue(message, newSession.getStopDate().equals(oldSession.getStopDate()));
    	try {
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstop", "isabel").getStartTime();
			assertTrue(message, newSession.getStopDate().equals(stopJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de lanzamiento de evento", false);
		}
	}
	
	public static void isSessionStopDateNotEqualDataBase(String message, Long conferenceID, Long sessionID, Session newSession) {
		Session oldSession = getSession(conferenceID, sessionID);
		assertFalse(message, newSession.getStopDate().equals(oldSession.getStopDate()));
    	try {
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstop", "isabel").getStartTime();
			assertFalse(message, newSession.getStopDate().equals(stopJobDate));
    	} catch (SchedulerException e) {
    		assertTrue("No se pudo obtener la hora de lanzamiento de evento", false);
		}
	}
	
	public static void isSessionDateEqualDataBase(String message, Long conferenceID, Long sessionID, Session newSession) {
		Session oldSession = getSession(conferenceID, sessionID);
		assertTrue(message + " - " + newSession.getStartDate() + " - " + oldSession.getStartDate(), newSession.getStartDate().equals(oldSession.getStartDate()));
    	assertTrue(message, newSession.getStopDate().equals(oldSession.getStopDate()));
    	try {
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstart", "isabel").getStartTime();
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstop", "isabel").getStartTime();
			assertTrue(message, newSession.getStartDate().equals(startJobDate));
			assertTrue(message, newSession.getStopDate().equals(stopJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de lanzamiento de evento", false);
		}
	}
	
	public static void isSessionDateNotEqualDataBase(String message, Long conferenceID, Long sessionID, Session newSession) {
		Session oldSession = getSession(conferenceID, sessionID);
		assertFalse(message, newSession.getStartDate().equals(oldSession.getStartDate()));
		assertFalse(message, newSession.getStopDate().equals(oldSession.getStopDate()));
    	try {
			Date startJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstart", "isabel").getStartTime();
			Date stopJobDate = IsabelScheduler.getInstance().getJobScheduler().getScheduler().getTrigger(oldSession.getId() + "sesstop", "isabel").getStartTime();
			assertFalse(message, newSession.getStartDate().equals(startJobDate));
			assertFalse(message, newSession.getStopDate().equals(stopJobDate));
    	} catch (SchedulerException e) {
			assertTrue("No se pudo obtener la hora de lanzamiento de evento", false);
		}
	}

	public static void isSessionChanged(String message, Long conferenceID, Long sessionID, Session newSession) {
		ResourceTestManager resManager = new ResourceTestManager();
		SessionResource sessionResource = resManager.getSessionResource(conferenceID, sessionID);
		Object sess = sessionResource.changeSession(newSession);
		assertTrue(message, sessionResource.getResponse().getStatus().getCode() == 200);
    	assertTrue(message, sess instanceof Session);
	}
	
	public static void isSessionNotChanged(String message, Long conferenceID, Long sessionID, Session newSession) {
		ResourceTestManager resManager = new ResourceTestManager();
		SessionResource sessionResource = resManager.getSessionResource(conferenceID, sessionID);
		Object sess = sessionResource.changeSession(newSession);
		assertFalse(message, sessionResource.getResponse().getStatus().getCode() == 200);
		assertFalse(message, sess instanceof Session);
	}
	
	public static void isManualSessionStatus(Long conferenceID, Long sessionID, Status status, boolean yesOrNot) {
		ResourceTestManager resManager = new ResourceTestManager();
		SessionStatusResource sessionStatusResource = resManager.getSessionStatusResource(conferenceID, sessionID);
    	SessionStatus oldStatus = sessionStatusResource.getSessionStatus();
    	if (yesOrNot) {
    		assertTrue("Status was not " + status, oldStatus.getStatus().equals(status));
    	} else {
    		assertTrue("Status was " + status, oldStatus.getStatus().equals(status));
    	}
	}
	
	public static void isManualSessionChanged(Long conferenceID, Long sessionID, Status origin, Status target, boolean yesOrNot) {
		ResourceTestManager resManager = new ResourceTestManager();
		SessionStatusResource sessionStatusResource = resManager.getSessionStatusResource(conferenceID, sessionID);
    	SessionStatus newStatus = new SessionStatus();
    	newStatus.setStatus(target);
    	sessionStatusResource.changeSessionStatus(newStatus);
    	sessionStatusResource = resManager.getSessionStatusResource(conferenceID, sessionID);
    	SessionStatus oldStatus = sessionStatusResource.getSessionStatus();
    	if (yesOrNot) {
    		assertTrue("Could not change status from " + origin + " to " + target, oldStatus.getStatus().equals(target));
    	} else {
    		assertFalse("Status was changed from " + origin + " to " + target, oldStatus.getStatus().equals(target));
    	}
	}
	
	public static Session getSession(Long conferenceID, Long sessionID) {
		Conference conference = getConference(conferenceID); 
		List<Session> sessions = conference.getSession();
		for (Session session : sessions) {
			if (session.getId().equals(sessionID)) {
				return session;
			}
		}
		return null;
	}
	
	public static Conference getConference(Long conferenceID) {
		return ConferenceRegistry.get(conferenceID);
	}
	
}
