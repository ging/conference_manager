package isabel.component.conference.monitoring;
import isabel.component.conference.util.MonitorConfigurationParser;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Clase de registro de agentes, es la encargada de programar la 
 * monitorizacion de cada agente basandose en el tipo al que pertenece y tambien lo
 * ejecuta. Incluye y borra agentes en este registro.
 * 
 * @author irenatr@dit.upm.es
 * @author prodriguez@dit.upm.es
 * @author jcervino@dit.upm.es
 *
 */
public class AgentRegistry {
	
	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(AgentRegistry.class);
	
	private ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(5);

	private List<ActionListener> listeners = new ArrayList<ActionListener>();

	private HashMap<Agent, Integer> tableStatus = new HashMap<Agent, Integer>();
	
	private HashMap<Agent, TimerTask> timers = new HashMap<Agent, TimerTask>();
	
	public AgentRegistry() {
		try {
			log.debug("Starting monitor configuration parser....");
			MonitorConfigurationParser.parse("config/monitor.xml");
			log.debug("Monitor configuration parsed. There are " + MonitorConfigurationParser.agentTypes.size() + " agent types defined.");
		} catch (Exception e) {
			log.error("Error reading configuration file");
		}
	}
	
	public void addAgent(final Agent agent){
		int delay = 5000; // delay for 5 sec. 
		int period = agent.getType().getPeriod(); // repeat every sec. 
		tableStatus.put(agent, 100);
		pool.scheduleAtFixedRate(new Runnable() {
			
			public  void  run() { 
				Runtime rt = Runtime.getRuntime();

				try {
					if (!MonitorConfigurationParser.debug) {
						Process p = rt.exec(MonitorConfigurationParser.scriptsPath + agent.getType().getScript() + " " + agent.getIp() + " " + agent.getCommand());

						InputStreamReader isr = new InputStreamReader(p.getInputStream());
						BufferedReader reader = new BufferedReader(isr);

						String line;
						while ((line= reader.readLine()) == null);

						checkStatus(agent, line);

						reader.close();
					} else {
						checkStatus(agent, "200");
					}

				} 

				catch (Exception e) {
					log.error("Error executing agent " + agent + "; message: " + e.getLocalizedMessage());
				}
			}	
		}, delay, period, TimeUnit.MILLISECONDS);
	}

	public void removeAgent(Agent agent){
		if (timers.containsKey(agent)) {
			timers.remove(agent).cancel();
			tableStatus.remove(agent);
		}
	}

	public Integer getAgentStatus(Agent agent) {
		return tableStatus.get(agent.toString());
	}
	
	public HashMap<Agent, Integer> getStatus() {
		return tableStatus;
	}

	private synchronized void checkStatus(Agent agent, String line) {

		Integer code = 1;
		
		try {
			code = Integer.parseInt(line);
		} catch (NumberFormatException e) {
			
		}
		
		tableStatus.put(agent, code);
		
		if (code >= 400) {
			for (ActionListener listener : listeners) {
				listener.actionPerformed(new AgentEvent(tableStatus));
			}
		}
	}
	
	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public ActionListener removeActionListener(ActionListener listener) {
		return listeners.remove(listeners.indexOf(listener));
	}
	
	public HashMap<String, AgentType> getAvailableAgentTypes() {
		return MonitorConfigurationParser.agentTypes;
	}

}
