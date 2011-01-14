package isabel.component.conference;

import isabel.component.conference.monitoring.MonitorManager;
import isabel.component.conference.persistence.HibernateUtil;
import isabel.component.conference.rest.ConferenceRouter;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.util.ConfigurationParser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * 
 * Clase principal de la aplicaci�n. Inicia un servidor HTTP que escuchar� 
 * posibles peticiones REST
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class ConferenceManager  {
	
	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(ConferenceManager.class);

	/**
	 * Inicia la aplicaci�n del gestor de conferencias.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Borrando Loggers indeseados.
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
			
			// Leemos el archivo de configuraci�n
			try {
				log.debug("Starting configuration parser....");
				ConfigurationParser.parse("config/config.xml");
				log.info("Setting server at " + ConfigurationParser.hostname + ":" + ConfigurationParser.port);
				log.info("Setting recording url at " + ConfigurationParser.recordURL);
				log.info("Setting streaming url at " + ConfigurationParser.streamURL);
				log.info("Setting web participation url at " + ConfigurationParser.webURL);
				
			} catch (Exception e) {
				log.error("Error reading configuration file");
			}
			
			// Inicializamos el registro de agentes de monitorización.
			MonitorManager.getInstance();
			
			// Iniciamos la base de datos.
			//DatabaseManager.getInstance();
			HibernateUtil.getSessionFactory().getCurrentSession();
	        
			// Iniciamos el servidor HTTP.
			Component component = new Component();
			component.getLogService().setEnabled(false);
			component.setName(ConfigurationParser.hostname);

			component.getServers().add(Protocol.HTTP, ConfigurationParser.port);
			
			// Iniciamos el programador de tareas de arranque y parada.
			IsabelScheduler scheduler = IsabelScheduler.getInstance();
			scheduler.start();
			
			// Iniciamos el arranque del router.
			component.getDefaultHost().attach(new ConferenceRouter());
			component.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
