package isabel.component.conference.monitoring;

import java.util.HashMap;

/**
 * 
 * Representacion de un tipo de agente de monitorizacion que da informacion acerca del nombre
 * del tipo, el script que se ejecutara, cada cuanto tiempo y los codigos que puede devolver 
 * un agente de ese tipo al ejecutarse.
 * 
 * @author irenatr@dit.upm.es
 * @author prodriguez@dit.upm.es
 * @author jcervino@dit.upm.es
 *
 */
public class AgentType {

	private String name;
	private String script;
	private int period;
	private HashMap<Integer, AgentCode> codes = new HashMap<Integer, AgentCode>();
	
	public AgentType (String name, String script, int period){
		this.name = name;
		this.script = script;
		this.period = period;
	}
	
	public AgentType() {
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	
	public void addCode(AgentCode code) {
		codes.put(code.getCode(), code);
	}
	
	public AgentCode getAgentCode(int code) {
		return codes.get(code);
	}

}
