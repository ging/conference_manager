package isabel.component.conference.vst;

import java.util.*;

public class Share{
	private String Name="";
	private String template="unknown";
	private int startLine;
	private int endLine;
	private Vector<Expression> expressions=new Vector<Expression>();
	
	
	public Share(String Name,String template){
		this.Name=Name;
		this.template=template;
	}

	public String getName(){
		return Name;
	}

	public String getTemplate(){
		return this.template;
	}
	
	public void setStartLine(int startLine){
		this.startLine=startLine;
	}

	public int getStartLine(){
		return startLine;
	}
	
	public void setEndLine(int endLine){
		this.endLine=endLine;
	}

	public int getEndLine(){
		return endLine;
	}
	public String toString(){
		return getName();
	}
	public void addExpression(Expression expr){
		expressions.addElement(expr);
	}
	public String getValueOf(String varName){
		Enumeration<Expression> enumm = expressions.elements();
		Expression expr;
		while(enumm.hasMoreElements()){
			expr=(Expression)enumm.nextElement();
			if(expr.getName().toLowerCase().compareTo(varName.toLowerCase())==0){
				return expr.getValue();
			}
		}
		return "";
	}

	public int getLineNumOf(String varName){
		Enumeration<Expression> enumm=expressions.elements();
		Expression expr;
		while(enumm.hasMoreElements()){
			expr=(Expression)enumm.nextElement();
			if(expr.getName().toLowerCase().compareTo(varName.toLowerCase())==0){
				return expr.getLineNum();
			}
		}
		return -1;
	}


	public String getComment(){
		return getValueOf("comment");
	}
	public String getPath(){
		return getValueOf("path");
	}
	public boolean isPrintable(){
		if(getValueOf("printable").toLowerCase().compareTo("yes")==0){
			return true;
		}else{
			return false;
		}
	}
	
	/* getInfo() returns a string of name and
	 * expressions as it would set in a samba conf file
	 */
	public String getInfo(){
		String ret="";
		ret+="#TPL:"+template+"\n";
		ret+=getData();
		return ret;
	}
	
	public String getData(){
		String s="";
		Enumeration<Expression> enumm=expressions.elements();
		s+="[" + getName() +"]\n";
		while(enumm.hasMoreElements()){
			s+="\t" + enumm.nextElement() +"\n";
		}
		return s;
	}
	
	
	public Enumeration<Expression> getExpressionsEnum(){
		return expressions.elements();
	}


}
