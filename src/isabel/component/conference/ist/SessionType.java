package isabel.component.conference.ist;

/**
 * Enumeration for Isabel session types
 * @author Fernando Escribano
 *
 */
public enum SessionType {
	
	TELEMEETING("meeting.act"),
	TELECLASS("class.act"),
	TELECONFERENCE("conference.act");
	
	private String name;
	
	private SessionType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
