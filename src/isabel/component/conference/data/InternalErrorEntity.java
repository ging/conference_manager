package isabel.component.conference.data;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement( name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class InternalErrorEntity {
	
	@XmlElement(name="message") 
	public String message;
	
	@XmlTransient
	private Exception exception;
	
	@XmlTransient
	public Exception getOriginException() {
		return exception;
	}
	
	public String getMessage() {
		return message;
	}
	
	@XmlTransient
	public InternalErrorEntity setException(Exception e) {
		exception = e;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		String content = baos.toString();
		InternalErrorEntity error = new InternalErrorEntity();
		error.message = content;
		return error;
	}

}
