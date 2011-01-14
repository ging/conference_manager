package isabel.component.conference.vst;

public class StringField implements PropertyInput{
	
	String content;
	
	public boolean setContent(String cont){
		content = cont;
		return true;
	}
	public String getContent(){
		return content;
	}
	public ErrorMessage control(){
		return null;
	}
}
