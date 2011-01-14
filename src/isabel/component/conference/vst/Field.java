package isabel.component.conference.vst;

public class Field{

	public String type;
	public String label;
	public boolean optional;
	public PropertyInput input;

	public Field(String type, String label, boolean optional){
		this.type=type;
		this.label=label;
		this.optional=optional;
	}
}
