package isabel.component.conference.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "available")
@XmlAccessorType(XmlAccessType.FIELD)
public class Available {

	@XmlElement(name = "start")
	private Date start;
	
	@XmlElement(name = "end")
	private Date end;
	
	public Available(Date start, Date end) {
		this.start = start;
		this.end = end;
	}
	
	public Available() {
		
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}
	
	
}
