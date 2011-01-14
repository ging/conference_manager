package isabel.component.conference.data;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement ( name = "event-status" )
@XmlAccessorType(XmlAccessType.FIELD)
public class ConferenceStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8719975788671136684L;

	@XmlElement(name = "recording-session", required = false)
	public Long recording;
	
}
