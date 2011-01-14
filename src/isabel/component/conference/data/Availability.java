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
@XmlRootElement(name = "availability")
public class Availability implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5591470141455010360L;
	
	/**
	 * Lista de las conferencias de la aplicacion.
	 */
	@XmlElement(name="available")
	public Collection<Available> available = new HashSet<Available>();
	
	/**
	 * Inicia una lista de conferencias.
	 */
	public Availability() {
		
	}
	
	/**
	 * Inicia una lista de conferencias a partir de una lista ya creada anteriormente.
	 * @param list
	 */
	public Availability(List<Available> list) {
		for (Available ava : list) {
			available.add(ava);
		}
	}
	
	public void addAvailable(Available newAvailable) {
		available.add(newAvailable);
	}
}
