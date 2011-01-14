package isabel.component.conference.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class NotFoundEntity {
	
	@XmlElement(name="event-not-found") 
	public String event_not_found;
	
	@XmlElement(name="session-not-found") 
	public String session_not_found;
}
