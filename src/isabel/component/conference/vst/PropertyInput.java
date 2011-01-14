package isabel.component.conference.vst;

public interface PropertyInput{
	public abstract String getContent();
	public abstract boolean setContent(String cont);
	//returns null when all is correct,
	//returns the eroor message else.
	public abstract ErrorMessage control();
}
