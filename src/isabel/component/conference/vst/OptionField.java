package isabel.component.conference.vst;

public class OptionField implements PropertyInput{
	
	private boolean selected;
	
	public OptionField(){
		
	}
	
	public boolean setContent(String cont){
		if (cont.equals("Yes")){
			selected = true;
			return true;
		}
		
		if (cont.equals("No")){
			selected = false;
			return true;
		}
		
		return false;
	}
	
	
	public String getContent(){
		if(selected){
			return "Yes";
		}else{
			return "No";
		}
	}

	@Override
	public ErrorMessage control() {
		return null;
	}
	
}
