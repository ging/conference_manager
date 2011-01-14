package isabel.component.conference.vst;

import java.io.*;


public class FileSelect implements PropertyInput{

	boolean onlyDirs;
	private String text;
	
	public FileSelect(String installDir,boolean onlyDirs){
		this.onlyDirs=onlyDirs;
	}
	
	public String getText(){
		return text;
	}
	public void setText(String text){
		this.text = text;
	}
	
	public boolean setContent(String cont){
		text = cont;
		return true;
	}
	
	public String getContent(){
		return getText();
	}
	
	public ErrorMessage control(){
		ErrorMessage em=new ErrorMessage();
		if(onlyDirs){
			if((new File(getContent()).isDirectory())){
				return null;
			} else {
				em.addError("The directory \""+ getContent() +"\" doesn't exist!");
				return (em);
			}
		}else{
			if((new File(getContent()).isFile())){
				return null;
			} else {
				em.addError("The file \""+ getContent() +"\" doesn't exist!");
				return em;
			}			
		}
	}

}
