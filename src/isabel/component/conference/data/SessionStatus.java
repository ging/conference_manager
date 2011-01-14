package isabel.component.conference.data;

import isabel.component.conference.data.Session.Status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement ( name = "session-status" )
@XmlAccessorType(XmlAccessType.FIELD)
public class SessionStatus {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8522199479703125262L;

	@XmlElement(name = "status")
	private String status = "Init";
	
	public Status getStatus() {
		return Status.getStatus(status);
	}
	
	public void setStatus(Status status) {
		this.status = status.toString();
	}

}
