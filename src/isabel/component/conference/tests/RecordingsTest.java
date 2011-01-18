package isabel.component.conference.tests;

import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine.SubType;
import isabel.component.conference.scheduler.jobs.ConferenceJob;
import isabel.component.conference.scheduler.jobs.SessionJob;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

import com.jayway.awaitility.Awaitility;

public class RecordingsTest extends TestCase {
	
	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory.getLogger(RecordingsTest.class);
	
	/** Received data */
	private boolean received;

	/**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    protected void setUp() {
    	Utils.addIsabelMachine("isabel1", SubType.vmIsabel);
    	Utils.addIsabelMachine("isabel2", SubType.vmIsabel);
    	Utils.addIsabelMachine("vnc1", SubType.vmVNC);
    	TimeTestManager.GAP = 1000;
    	TimeTestManager.init();
    	ConferenceJob.setManager(new MockManager());
    	ConferenceJob.setVNCManager(new MockVNCManager());
    	SessionJob.setController(new MockController());
    	TestConfigurator.initializeAll();
    }
    
    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() {
    	TestConfigurator.initializeAll();
    }
    
    public void testSessionStartTrigger() throws Exception {
    	SessionJob.setController(new MockController() {

			@Override
			public boolean startRecording(String streamURL, String streamName,
					String conference, String session) {
				log.info("Start recording");
				received = true;
				return true;
			}
    		
    	});
    	
    	Conference conf = Utils.createConference("confOne", "meeting", true, true, false, false, -20, 200);
    	conf = Utils.createCurrentConference(conf);
    	Utils.checkCreateSession(conf.getId(), 3, 7, true);
    	received = false;
    	
    	Awaitility.await().atMost(TimeTestManager.GAP * 7, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return received;
			}
		});
        assertTrue(received);
    }
    
    public void testSessionStopTrigger() throws Exception {
    	SessionJob.setController(new MockController() {

			@Override
			public boolean stopRecording(String streamURL, String streamName,
					String conference, String session) {
				log.info("Stop recording");
				received = true;
				return true;
			}
    		
    	});
    	
    	Conference conf = Utils.createConference("confOne", "meeting", true, true, false, false, -20, 200);
    	conf = Utils.createCurrentConference(conf);
    	Utils.checkCreateSession(conf.getId(), 3, 7, true);
    	received = false;
    	
    	Awaitility.await().atMost(TimeTestManager.GAP * 8, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return received;
			}
		});
        assertTrue(received);
    }
    
    public void testSessionPublishedTrigger() throws Exception {
    	SessionJob.setController(new MockController() {

			@Override
			public boolean publishRecording(String streamURL,
					String streamName, String conference, String session) {
				log.info("Start recording");
				received = true;
				return false;
			}

    	});
    	
    	Conference conf = Utils.createConference("confOne", "meeting", true, true, false, false, -20, 200);
    	conf = Utils.createCurrentConference(conf);
    	Utils.checkCreateSession(conf.getId(), 3, 7, true);
    	received = false;
    	
    	Awaitility.await().atMost(TimeTestManager.GAP * 8, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return received;
			}
		});
        assertTrue(received);
    }
    
}