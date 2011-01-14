package isabel.component.conference.monitoring;

/**
 * 
 * Agente que almacena informacion acerca del tipo de monitorizacion, ip de la maquina y 
 * comando a ejecutar para la monitorizacion.
 * 
 * @author irenatr@dit.upm.es
 * @author prodriguez@dit.upm.es
 * @author jcervino@dit.upm.es
 *
 */
public class Agent {

	private AgentType type;

	private String ip;
	
	private String command;

	public AgentType getType() {
		return type;
	}

	public void setType(AgentType type) {
		this.type = type;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Agent (AgentType type, String ip, String command) {
		if (type != null) {
			this.type = type;
			this.ip = ip;
			this.command = command;
		} else {
			throw new NullPointerException();
		}
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String toString() {
		return type.getName() + ", " + ip;
	}

}
