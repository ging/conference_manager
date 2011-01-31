package isabel.component.conference.tests;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.IsabelMachine.SubType;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.SchedulerResponse;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class IsabelSchedulerTest extends TestCase {
	
	private IsabelScheduler scheduler; 

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    protected void setUp() {
    	TimeTestManager.init();
    	scheduler = IsabelScheduler.getInstance();
    	addIsabelMachine("isabel1", SubType.vmIsabel);
    	addIsabelMachine("isabel2", SubType.vmIsabel);
    	addIsabelMachine("vnc1", SubType.vmVNC);
    	long time = (new Date()).getTime();
    	time = time-time%1000;
    }
    
    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() {
    	TestConfigurator.initializeAll();
    }
    
    public void testAddConference() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	SchedulerResponse result = scheduler.scheduleConference(conference);
    	assertTrue("No se pueden crear conferencias", result.ok);
    	Conference confResult = ConferenceRegistry.get(conference.getId());
    	assertTrue("No se ha guardado en la base de datos", confResult != null);
    }
    
    public void testAddSessionToConference() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(4));
    	SchedulerResponse result = scheduler.scheduleConference(conference);
    	assertTrue("No se pueden crear conferencias", result.ok);
    	Conference confResult = ConferenceRegistry.get(conference.getId());
    	assertTrue("No se ha guardado en la base de datos", confResult != null);
    	
    	Session session = createSession("sesUno", true, true, TimeTestManager.getTime(1), TimeTestManager.getTime(2), TimeTestManager.getTime(3));
    	assertTrue("No se puede crear la sesion", scheduler.scheduleSession(conference, session).ok);
    	
    	confResult = ConferenceRegistry.get(conference.getId());
    	List<Session> sessions = confResult.getSession();
    	assertTrue("Hay mas sesiones de las que deberia", sessions.size() <= 1);
    	assertTrue("Hay menos sesiones de las que deberia", sessions.size() >= 1);
    	
    	Session sesRes = sessions.get(0);
    	assertTrue("La sesion no es la misma", session.equals(sesRes));
    }
    
    public void testChangeSessionInConference() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(5));
    	scheduler.scheduleConference(conference);
    	
    	Session newSession = createSession("sesUno", true, true, TimeTestManager.getTime(1), TimeTestManager.getTime(2), TimeTestManager.getTime(3));
    	
    	assertTrue("No se puede crear la sesion", scheduler.scheduleSession(conference, newSession).ok);
    	Session oldSession = ConferenceRegistry.get(conference.getId()).getSession().get(0);
    	assertTrue("Las fechas de inicio no son las mismas", newSession.getStartDate().equals(oldSession.getStartDate()));
    	assertTrue("Las fechas de fin no son las mismas", newSession.getStopDate().equals(oldSession.getStopDate()));
    	
    	newSession = createSession("sesUno", true, true, TimeTestManager.getTime(1), TimeTestManager.getTime(2), TimeTestManager.getTime(4));
    	newSession.setConference(conference);
    	assertTrue("No se puede retrasar el final de la sesion", scheduler.rescheduleSession(oldSession, newSession).ok);
    	oldSession = ConferenceRegistry.get(conference.getId()).getSession().get(0);
    	assertTrue("Las fechas de inicio no son las mismas", newSession.getStartDate().equals(oldSession.getStartDate()));
    	assertTrue("Las fechas de fin no son las mismas", newSession.getStopDate().equals(oldSession.getStopDate()));
    	
    	newSession = createSession("sesUno", true, true, TimeTestManager.getTime(1), TimeTestManager.getTime(3), TimeTestManager.getTime(4));
    	newSession.setConference(conference);
    	assertTrue("No se puede retrasar el inicio de la sesion", scheduler.rescheduleSession(oldSession, newSession).ok);
    	oldSession = ConferenceRegistry.get(conference.getId()).getSession().get(0);
    	assertTrue("Las fechas de inicio no son las mismas", newSession.getStartDate().equals(oldSession.getStartDate()));
    	assertTrue("Las fechas de fin no son las mismas", newSession.getStopDate().equals(oldSession.getStopDate()));
    	
    	newSession = createSession("sesUno", true, true, TimeTestManager.getTime(1), TimeTestManager.getTime(2), TimeTestManager.getTime(4));
    	newSession.setConference(conference);
    	assertTrue("No se puede adelantar el inicio de la sesion", scheduler.rescheduleSession(oldSession, newSession).ok);
    	oldSession = ConferenceRegistry.get(conference.getId()).getSession().get(0);
    	assertTrue("Las fechas de inicio no son las mismas", newSession.getStartDate().equals(oldSession.getStartDate()));
    	assertTrue("Las fechas de fin no son las mismas", newSession.getStopDate().equals(oldSession.getStopDate()));
    	
    	newSession = createSession("sesUno", true, true, TimeTestManager.getTime(1), TimeTestManager.getTime(2), TimeTestManager.getTime(3));
    	newSession.setConference(conference);
    	assertTrue("No se puede adelantar el final de la sesion", scheduler.rescheduleSession(oldSession, newSession).ok);
    	oldSession = ConferenceRegistry.get(conference.getId()).getSession().get(0);
    	assertTrue("Las fechas de inicio no son las mismas", newSession.getStartDate().equals(oldSession.getStartDate()));
    	assertTrue("Las fechas de fin no son las mismas", newSession.getStopDate().equals(oldSession.getStopDate()));
    	
    	newSession = createSession("sesUno", true, true, TimeTestManager.getTime(1), TimeTestManager.getTime(3), TimeTestManager.getTime(4));
    	newSession.setConference(conference);
    	assertTrue("No se puede retrasar toda la sesion", scheduler.rescheduleSession(oldSession, newSession).ok);
    	oldSession = ConferenceRegistry.get(conference.getId()).getSession().get(0);
    	assertTrue("Las fechas de inicio no son las mismas", newSession.getStartDate().equals(oldSession.getStartDate()));
    	assertTrue("Las fechas de fin no son las mismas", newSession.getStopDate().equals(oldSession.getStopDate()));
    	
    }
    
    public void testChangeVoidConference() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	assertTrue("No se puede crear la segunda conferencia", scheduler.scheduleConference(conference).ok);
    	Conference oldConference = ConferenceRegistry.get(conference.getId());
    	
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(3));
    	assertTrue("No se ha podido retrasar el final", scheduler.rescheduleConference(newConference, oldConference).ok);

    	oldConference = ConferenceRegistry.get(conference.getId());
    	assertTrue("Las fechas de inicio no son las mismas", newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
    	assertTrue("Las fechas de fin no son las mismas", newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
    	
    	newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(2), TimeTestManager.getTime(3));
    	assertTrue("No se ha podido retrasar el principio", scheduler.rescheduleConference(newConference, oldConference).ok);
    	
    	oldConference = ConferenceRegistry.get(conference.getId());
    	assertTrue("Las fechas de inicio no son las mismas", newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
    	assertTrue("Las fechas de fin no son las mismas", newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
    	
    	newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(3));
    	assertTrue("No se ha podido adelantar el principio", scheduler.rescheduleConference(newConference, oldConference).ok);
    	
    	oldConference = ConferenceRegistry.get(conference.getId());
    	assertTrue("Las fechas de inicio no son las mismas", newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
    	assertTrue("Las fechas de fin no son las mismas", newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
    	
    	newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	assertTrue("No se ha podido adelantar el final", scheduler.rescheduleConference(newConference, oldConference).ok);
    	
    	oldConference = ConferenceRegistry.get(conference.getId());
    	assertTrue("Las fechas de inicio no son las mismas", newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
    	assertTrue("Las fechas de fin no son las mismas", newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
    	
    	newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(3), TimeTestManager.getTime(4));
    	assertTrue("No se ha podido retrasar toda la conferencia", scheduler.rescheduleConference(newConference, oldConference).ok);
    	
    	oldConference = ConferenceRegistry.get(conference.getId());
    	assertTrue("Las fechas de inicio no son las mismas", newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
    	assertTrue("Las fechas de fin no son las mismas", newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
    	
    	newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	assertTrue("No se ha podido adelantar toda la conferencia", scheduler.rescheduleConference(newConference, oldConference).ok);
    	
    	oldConference = ConferenceRegistry.get(conference.getId());
    	assertTrue("Las fechas de inicio no son las mismas", newConference.getResourcesStartTime().equals(oldConference.getResourcesStartTime()));
    	assertTrue("Las fechas de fin no son las mismas", newConference.getResourcesStopTime().equals(oldConference.getResourcesStopTime()));
    }
    
    
    public void testRemoveConference() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	scheduler.scheduleConference(conference);
    	Long id = conference.getId();
    	conference = ConferenceRegistry.get(conference.getId());
    	ConferenceRegistry.remove(conference);
    	Conference confResult = ConferenceRegistry.get(id);
    	assertTrue("No se ha borrado de la base de datos", confResult == null);
    }
    
    public void testAddTwoConferences() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(3), TimeTestManager.getTime(4));
    	assertTrue("No se puede crear la segunda conferencia", scheduler.scheduleConference(conference).ok);
    }
    
    public void testCheckCreateOverlapAfter() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(3));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(2), TimeTestManager.getTime(4));
    	assertFalse("Ha permitido solapar conferencias", scheduler.scheduleConference(conference).ok);
    }
    
    public void testCheckCreateOverlapBefore() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(2), TimeTestManager.getTime(4));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(3));
    	assertFalse("Ha permitido solapar conferencias", scheduler.scheduleConference(conference).ok);
    }
    
    public void testCheckCreateOverlapIn() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(4));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(2), TimeTestManager.getTime(3));
    	assertFalse("Ha permitido solapar conferencias", scheduler.scheduleConference(conference).ok);
    }
    
    public void testCheckCreateOverlapOut() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(2), TimeTestManager.getTime(3));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(4));
    	assertFalse("Ha permitido solapar conferencias", scheduler.scheduleConference(conference).ok);
    }
    
    public void testCheckExtendOverlapAfter() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(3));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(4), TimeTestManager.getTime(6));
    	assertTrue("No se puede crear la segunda conferencia", scheduler.scheduleConference(conference).ok);
    	Conference oldConference = ConferenceRegistry.get(conference.getId());
    	
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(2), TimeTestManager.getTime(6));
    	assertFalse("Ha permitido solapar conferencias al extender", scheduler.rescheduleConference(newConference, oldConference).ok);
    }
    
    public void testCheckExtendOverlapBefore() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(4), TimeTestManager.getTime(6));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(3));
    	assertTrue("No se puede crear la segunda conferencia", scheduler.scheduleConference(conference).ok);
    	Conference oldConference = ConferenceRegistry.get(conference.getId());
    	
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(5));
    	assertFalse("Ha permitido solapar conferencias al extender", scheduler.rescheduleConference(newConference, oldConference).ok);
    }
    
    public void testCheckChangeOverlapIn() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(3), TimeTestManager.getTime(6));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	assertTrue("No se puede crear la segunda conferencia", scheduler.scheduleConference(conference).ok);
    	Conference oldConference = ConferenceRegistry.get(conference.getId());
    	
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(4), TimeTestManager.getTime(5));
    	assertFalse("Ha permitido solapar conferencias al extender", scheduler.rescheduleConference(newConference, oldConference).ok);
    }
    
    public void testCheckChangeOverlapOut() {
    	Conference conference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(3), TimeTestManager.getTime(5));
    	assertTrue("No se puede crear la primera conferencia", scheduler.scheduleConference(conference).ok);
    	
    	conference = createConference("dos", "meeting", true, true, false, false, TimeTestManager.getTime(1), TimeTestManager.getTime(2));
    	assertTrue("No se puede crear la segunda conferencia", scheduler.scheduleConference(conference).ok);
    	Conference oldConference = ConferenceRegistry.get(conference.getId());
    	
    	Conference newConference = createConference("uno", "meeting", true, true, false, false, TimeTestManager.getTime(2), TimeTestManager.getTime(6));
    	assertFalse("Ha permitido solapar conferencias al extender", scheduler.rescheduleConference(newConference, oldConference).ok);
    }
    
    private Session createSession(String name, boolean enableStreaming, boolean automaticRecording, 
    		Date confInit, Date sesStart, Date sesStop) {
    	Session session = new Session();
    	session.setName(name);
    	session.setStartTime(sesStart.getTime()-confInit.getTime());
    	session.setStopTime(sesStop.getTime()-confInit.getTime());
    	session.setEnableStreaming(enableStreaming);
    	session.setAutomaticRecord(automaticRecording);
    	return session;
    }
    
    private Conference createConference(String name, String type, boolean enableIsabel, boolean enableWeb, 
    		boolean enableHTTPLiveStreaming, boolean enableSIP, Date start, Date stop) {
    	Conference conference1 = new Conference(name);
		conference1.setType(type);
		conference1.setEnableIsabel(enableIsabel);
		conference1.setEnableWeb(enableWeb);
		conference1.setEnableHTTPLiveStreaming(enableHTTPLiveStreaming);
		conference1.setEnableSIP(enableSIP);
		conference1.setStartTime(start);
		conference1.setStopTime(stop);
		return conference1;
    }
    
    private IsabelMachine addIsabelMachine(String hostname, SubType type) {
    	IsabelMachine machine = new IsabelMachine();
		machine.setHostname(hostname);
		machine.setSubType(type);
		IsabelMachineRegistry.addIsabelMachine(machine);
		return machine;
    }
}
