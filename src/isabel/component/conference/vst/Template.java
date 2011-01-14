package isabel.component.conference.vst;

public class Template{
	private String fileName;

	public Template(String fileName){
		this.fileName=fileName;
	}
	public String getFileName(){
		return fileName;
	}
	public String getName(){
		String name=fileName;
		int li=fileName.lastIndexOf('/');
		if (li<0) {li=0;} else {li++;}
		name=name.substring(li,name.length()-4);
		name=name.replace('_',' ');
		return name;
	}
	public String toString(){
		return getName();
	}
	
}