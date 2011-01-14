package isabel.component.conference.vst;

import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

public class IO{
	private String filename;

	public IO(String filename){
		this.filename=filename;
	}

	public String[] load() throws Exception{
		String[] ret;
		ArrayList<String> retArray=new ArrayList<String>();
		String line="";
		Exception fnfex=null;
		
		try{
			BufferedReader r=new BufferedReader(new FileReader(this.filename));
			while(line!=null){
				line=r.readLine();
				if (line!=null){
					retArray.add(line);
				}
			}
			r.close();
		} catch(Exception e){
			fnfex=e;
		}
		if (fnfex!=null) throw fnfex;
		
		ret=new String[retArray.size()];
		for (int i=0; i<retArray.size();i++){
			ret[i]=(String)retArray.get(i);
		}
		return ret;
	}
	
	public boolean save(String[] lines){
		boolean ret=true;
		try {
			FileWriter fw=new FileWriter(filename);
			for (int i=0; i<lines.length; i++){
				fw.write(lines[i]+"\n");
			}
			fw.close();
		}catch(Exception exc){	
			JOptionPane.showMessageDialog(new JPanel(),exc.getMessage()+"\nPerhaps you have to restart jsmbconf as root to save this file!","Error!",JOptionPane.ERROR_MESSAGE);
			ret=false;
		}
		return ret;
	}
	



}
