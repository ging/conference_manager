package isabel.component.conference.vst;

import java.util.*;

import org.mortbay.log.Log;

public class SmbConfParser{

	private String[] allLines;
	private IO io;
	public HashMap<String, Share> Shares;
	

	public SmbConfParser(String sambaconf){
		io=new IO(sambaconf);
		this.init();
	}
	
	public void init(){
		try{
			allLines=io.load();
		}catch(Exception e){
			System.out.println("File Not Found!");
			e.printStackTrace();
		}
		
		parse();
	}
	
	
	private void parse(){
	
		Share newShare=null;
		Shares=new HashMap<String, Share>();
		int errors=0;
		final int MAXERRORS=5;
		int startline=0;
		int endline=0;
		for (int i=0; i<allLines.length; i++){

		    String sharename="";
		    String templatefile="unknown";

			if(allLines[i].matches("\\s*\\[.*\\]\\s*")){
				sharename=allLines[i].substring(allLines[i].indexOf('[')+1,allLines[i].lastIndexOf(']'));
				if (sharename.equals("global")){
				
				} else {
					if ((errors<MAXERRORS)&&(getShare(sharename)!=null)){
						errors++;
						ErrorMessage em=new ErrorMessage();
						em.addError("Error in smb.conf! Existing at least 2 share with same name: "+sharename+" . This can cause unpredictable errors!");
						if (errors==MAXERRORS){
							em.setErrorTitle("Information");
							em.addError("There were "+errors+" Errors. Skipping following ones...");
						}
						Log.warn("Error while processing smb.conf");
					}
					
					if (newShare!=null){
						endline=i-1;
						for (int k=i-1; k>=0; k--){
							endline=k;
							if (!allLines[k].startsWith("#")){
								break;
							}
						}
						newShare.setEndLine(endline);
					}
					
					//find templatefilename and fingerprint if it exists
					startline=i;
					if (i-3>=0){
						if (allLines[i-1].startsWith("#FPR:")){
							allLines[i-1].substring(5);
							startline=i-1;
							
							if (allLines[i-2].startsWith("#TPL:")){
								templatefile=allLines[i-2].substring(5);
								startline=i-2;
								
								if (allLines[i-3].startsWith("#")){
									startline=i-3;
								
									if (allLines[i-4].startsWith("#")){
										startline=i-4;
									}
								}	
							}
						}
					}
					
					
					newShare=new Share(sharename,templatefile);
					newShare.setStartLine(startline);
					Shares.put(sharename, newShare);
				}
			}
			//Share mit Attributen fuellen
			//...
		}
		
		if (newShare!=null){
			newShare.setEndLine(allLines.length-1);
		}

		for(Share newShared : Shares.values()){
			for(int a=newShared.getStartLine();a<=newShared.getEndLine();a++){
				String line=removeComments(allLines[a]);
				if(line.indexOf('=')>=0){
					String expression_left=line.substring(0,line.indexOf('='));
					String expression_right=line.substring(line.indexOf('=')+1);
					expression_right=removeSpaceAtBeginAndEnd(expression_right);
					expression_left=removeSpaceAtBeginAndEnd(expression_left);
					newShared.addExpression(new Expression(expression_left, expression_right,a));
				}
			}
		}
	}
	
	public String removeComments(String s){
		if(s.indexOf('#')>=0){
			s=s.substring(0,s.indexOf('#'));
		}
		if(s.indexOf(';')>=0){
			s=s.substring(0,s.indexOf(';'));
		}
		return s;
	}
	public String removeSpaceAtBeginAndEnd(String s){
		for(int i=0; i<s.length();i++){
			if(s.charAt(i)!=' ' &&	s.charAt(i)!='\t'){
				s=s.substring(i);
				break;
			}
		}
		for(int i=s.length()-1; i>=0;i--){
			if(s.charAt(i)!=' ' &&	s.charAt(i)!='\t'){
				s=s.substring(0,i+1);
				return s;
			}
		}
		
		return "";
	}

	public Set<String> getShareNames(){
		return Shares.keySet();
	}
	
	public Collection<Share> getShares(){
		return Shares.values();
	}
	
	public String[] getAllLines(){
		return allLines;
	}

	public boolean deleteLine(int oldLine, String newLine){
		String[] aL=getAllLines();
		if (aL.length==0){ return false;}
		
		boolean ret=false;
		String[] nL;
		if (newLine==null){
			nL=new String[aL.length-1];
		} else {
			nL=new String[aL.length];
		}
		
		int d=0;	
		for (int i=0; i<aL.length; i++){

			if (oldLine==i){
				if (newLine==null){
					//nur l�schen
					ret=true;
				} else {
					//ersetzen
					nL[i]=newLine;
					d++;
					ret=true;
				}
			} else {
				nL[d]=aL[i];
				d++;
			}
		}
		if (ret) setAllLines(nL);
		return ret;
	}

	public void setAllLines(String[] allLines){
		this.allLines=allLines;
		parse();
	}

	public void appendLines(String[] lines){
		String[] newAllLines=new String[allLines.length+lines.length];
		for (int i=0; i<allLines.length; i++){
			newAllLines[i]=allLines[i];
		}
		for (int i=allLines.length; i<allLines.length+lines.length; i++){
			newAllLines[i]=lines[i-allLines.length];
		}
		allLines=newAllLines;
		parse();
	}


	public int deleteShare(String shareName){
		Share share=getShare(shareName);
		int delLines=share.getEndLine()-share.getStartLine();
		String[] newAllLines=new String[allLines.length-delLines-1];
		for (int i=0; i<allLines.length; i++){
			if (i<share.getStartLine()){
				newAllLines[i]=allLines[i];
			} 
			if (i>share.getEndLine()){
				newAllLines[i-delLines-1]=allLines[i];
			}
		}
		allLines=newAllLines;
		parse();
		return 0;
	}
	
	public boolean renameShare(String oldName, String newName, SmbWizard wizard){
		if (oldName.equals(newName)) return false;
		int lineNum=getShare(oldName).getStartLine();
		int endline=getShare(oldName).getEndLine();
		if (lineNum<0) return false;
		
		boolean g=false;
		
		while(lineNum<endline){
			if (allLines[lineNum].matches("\\s*\\[.*\\]\\s*")){
				g=(deleteLine(lineNum, "["+newName+"]"));
				return g;
			}
			lineNum++;
		}
		return g;
	}
	
	public Share getShare(String shareName){
		return Shares.get(shareName);
	}


}
