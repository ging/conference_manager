package isabel.component.conference.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnprocessableEntity {
	
	@XmlElement(name="required-tag") 
	public String required_tag;
	
	@XmlElement(name="required-attribute")
	public String required_attribute;
	
	@XmlElement(name="wrong-tag")
	public String wrong_tag;
	
	@XmlElement(name="wrong-attribute")
	public String wrong_attribute;
	
	@XmlElement(name="unknown-tag")
	public String unknown_tag;
	
	@XmlElement(name="unknown-attribute")
	public String unknown_attribute;
	
}
