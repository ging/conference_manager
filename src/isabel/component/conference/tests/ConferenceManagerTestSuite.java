package isabel.component.conference.tests;

import isabel.component.conference.persistence.HibernateUtil;
import isabel.component.conference.util.ConfigurationParser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class ConferenceManagerTestSuite {
	public static Test suite() {
    	Enumeration<String> loggers = LogManager.getLogManager().getLoggerNames();
		while (loggers.hasMoreElements()) {
			java.util.logging.Logger log = LogManager.getLogManager().getLogger(loggers.nextElement());
			for (Handler handler : log.getHandlers()) {
				if (handler instanceof ConsoleHandler) {
					log.removeHandler(handler);
				}
			}
		}
		
		try {
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			StatusPrinter.setPrintStream(new PrintStream("logs/stdout.log"));
			StatusPrinter.print(lc);
		} catch (FileNotFoundException e1) {
		}
		
		//Configuramos CM
		try {
			System.out.println("Leyendo configuracion");
			ConfigurationParser.parse("config/config.xml");
			ConfigurationParser.marginMasterGateway = 10;
			ConfigurationParser.marginConferenceSession = 10;
		} catch (Exception e) {
		}
		
		// Configuramos Hibernate
		System.out.println("Arrancando hibernate");
		HibernateUtil.debug = true;
		HibernateUtil.getSessionFactory().getCurrentSession();
		System.out.println("Arrancado todo");

        TestSuite suite = new TestSuite();
    	
        suite.addTestSuite(IsabelMachinesTest.class);
        suite.addTestSuite(IsabelSchedulerTest.class);
        suite.addTestSuite(ConferencesTest.class);
        suite.addTestSuite(RecordingsTest.class);
        
        TestConfigurator.initializeAll();

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
    	
        junit.textui.TestRunner.run(suite());
    }
}
