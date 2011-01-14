package isabel.component.conference.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ForbiddenEntity {
	
	@XmlElement(name="message") 
	public String message;
	
}
