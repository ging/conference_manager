package isabel.component.conference.vst;

import java.util.*;
import java.awt.*;
import javax.swing.*;

public class ErrorMessage{


	private Vector<String> ErrorMessages;
	private Vector<String> WarnMessages;
	private String oldWarnTitle="";
	private String oldErrorTitle="";
	

	public ErrorMessage(){
		ErrorMessages=new Vector<String>();
		WarnMessages=new Vector<String>();
	}

	public void add(String title,ErrorMessage message){
		if (message==null) return;
		if (message.getErrorVector().size()>0){
			setErrorTitle(title);
			ErrorMessages.addAll(message.getErrorVector());
		}
		if (message.getWarnVector().size()>0){
			setWarnTitle(title);
			WarnMessages.addAll(message.getWarnVector());
		}
	}

	public Vector<String> getWarnVector(){
		return WarnMessages;
	}

	public Vector<String> getErrorVector(){
		return ErrorMessages;
	}

	public void setWarnTitle(String title){
		if (!oldWarnTitle.equals(title)){
			WarnMessages.add("*T*"+title);
			oldWarnTitle=title;
		}
	}
	
	public void addWarning(String message){
		WarnMessages.add(message);
	}
		
	public void setErrorTitle(String title){
		if (!oldErrorTitle.equals(title)){
			ErrorMessages.add("*T*"+title);
			oldErrorTitle=title;
		}
	}
	
	public void addError(String message){
		ErrorMessages.add(message);
	}
	
	private String getErrors(){
		Enumeration<String> en=ErrorMessages.elements();
		String errormessage="";
		while (en.hasMoreElements()){
			String s=(String)en.nextElement();
			if (s.substring(0,3).equals("*T*")){
				errormessage+="<u>"+s.substring(3,s.length())+"</u><BR>";
			} else {
				errormessage+="<font color=red>* "+s+"</font><BR>";
			}
		}
		return errormessage;
	}


	private String getWarnings(){
		Enumeration<String> en=WarnMessages.elements();
		String warnmessage="";
		while (en.hasMoreElements()){
			String s=(String)en.nextElement();
			if (s.substring(0,3).equals("*T*")){
				warnmessage+="<u>"+s.substring(3,s.length())+"</u><BR>";
			} else {
				warnmessage+="<font color=red>* "+s+"</font><BR>";
			}
		}
		return warnmessage;
	}

	
	public void showErrors(Component owner,String mainTitle){
		JOptionPane.showMessageDialog(owner,"<html><h3>"+mainTitle+"</h3>"+getErrors()+"</html>","Error",JOptionPane.ERROR_MESSAGE);
	}

	public int showErrorsWarnings(Component owner,String mainTitle){
		String message="<h3>"+mainTitle+"</h3>"+getErrors();
		if (WarnMessages.size()>0){
			message+="<h3>Warnings</h3>"+getWarnings();
		}
		
		if (ErrorMessages.size()>0){
			JOptionPane.showMessageDialog(owner,"<html>"+message+"</html>","Error",JOptionPane.ERROR_MESSAGE);
			return 1;
		} else {
			message+="<p>Do you want to ignore this warnings?</p>";
			return JOptionPane.showConfirmDialog(owner,"<html>"+message+"</html>","Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
		}
	}

	public boolean hasMessages(){
		return ((WarnMessages.size()>0)||(ErrorMessages.size()>0));
	}




}
