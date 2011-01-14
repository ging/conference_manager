package isabel.component.conference.vst;

public class Expression{
	public String name;
	public String value;
	public int lineNum;
	
	public Expression(String name, String value, int lineNum){
		this.name=name;
		this.value=value;
		this.lineNum=lineNum;
	}
	public String getName(){
		return name;
	}
	public String getValue(){
		return value;
	}

	public int getLineNum(){
		return lineNum;
	}
	
	public String toString(){
		return name + " = " + value;
	}
}
