package isabel.component.conference.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConflictEntity {
	
	@XmlElement(name="from") 
	public Date from;
	
	@XmlElement(name="to") 
	public Date to;
}
