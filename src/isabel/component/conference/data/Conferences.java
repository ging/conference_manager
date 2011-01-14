package isabel.component.conference.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author jcervino@dit.upm.es
 *
 */
@XmlRootElement(name = "events")
public class Conferences implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5591470141455010360L;
	
	/**
	 * Lista de las conferencias de la aplicación.
	 */
	@XmlElement(name="event")
	public Collection<Conference> conference = new HashSet<Conference>();
	
	/**
	 * Inicia una lista de conferencias.
	 */
	public Conferences() {
		
	}
	
	/**
	 * Inicia una lista de conferencias a partir de una lista ya creada anteriormente.
	 * @param list
	 */
	public Conferences(List<Conference> list) {
		for (Conference conf : list) {
			conference.add(conf);
		}
	}
}
