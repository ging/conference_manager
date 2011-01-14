package isabel.component.conference.monitoring;
import java.awt.event.ActionEvent;

/**
 * 
 * Evento lanzado por un agente cuando hay un error.
 * 
 * @author irenatr@dit.upm.es
 * @author prodriguez@dit.upm.es
 * @author jcervino@dit.upm.es
 *
 */
public class AgentEvent extends ActionEvent{

	public AgentEvent(Object source) {
		super(source, 1, "");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3921033949562489145L;
}
