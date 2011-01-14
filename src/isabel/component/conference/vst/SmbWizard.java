package isabel.component.conference.vst;

import isabel.component.conference.util.ConfigurationParser;

import java.util.*;

//import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import org.mortbay.log.Log;

public class SmbWizard {
	public String filePath="";
	public String exPath="/etc/init.d/samba";
	public SmbConfParser parser=null;
	private static SmbWizard wizard;
	
	private SmbWizard(){
		//Parser initialisieren
		this.filePath = ConfigurationParser.sambaFile;
		parser=new SmbConfParser(filePath);
	}
	
	public static SmbWizard getInstance() {
		if (wizard == null) {
			wizard = new SmbWizard();
		}
		return wizard;
	}

	public void reloadShareListData(){
		parser=new SmbConfParser(filePath);
	}

	public void exit(){
		System.exit(0);
	}

	public void save(){
		IO io=new IO(filePath);
		if (io.save(parser.getAllLines())){
			String os= System.getProperty("os.name");
			if (os != null && !os.toLowerCase().contains("win")) {
				restartSamba();
			}
		}
	}

	public void addShare(ShareProp shareprop) {
		parser.appendLines(shareprop.output());
	}

	public int deleteShare(String shareName){
		int ret = 1;
		if (parser.Shares.containsKey(shareName))
			ret=parser.deleteShare(shareName);
		return ret;
	}

	public void changeShare(Share share, ShareProp shareprop) {
		deleteShare(share.getName());
		addShare(shareprop);
	}
	
	public void restartSamba(){
		Process p=null;
		try{
			p=Runtime.getRuntime().exec(exPath + " restart");
			p.waitFor();
		}catch(Exception e){
			Log.warn(e.getMessage());
		}
		if (p==null){
			Log.warn("Restarting wasn't successful!");
		} else {
			if (p.exitValue()!=0){
				Log.warn("restart samba: Error!: Error Code: "+p.exitValue());
			} else {
				Log.info("Restarting was successful!");
			}
		}
	}
	
	public void reloadSamba(){
		Process p=null;
		try{
			p=Runtime.getRuntime().exec(exPath + " reload");
			p.waitFor();
		}catch(Exception e){
			Log.warn(e.getMessage());
		}
		if (p==null){
			Log.warn("Restarting wasn't successful!");
		} else {
			if (p.exitValue()!=0){
				Log.warn("restart samba: Error!: Error Code: "+p.exitValue());
			} else {
				Log.info("Restarting was successful!");
			}
		}
	}
		
	public Set<String> getShareNames(){
		return parser.getShareNames();
	}
	public Collection<Share> getShares(){
		return parser.getShares();
	}
	
	public Share getShare(String shareName) {
		return parser.getShare(shareName);
	}
	
	public void createOrUpdate(String shareName, ShareProp shareProp) {
		if (parser.Shares.containsKey(shareName)) {
			Share share = getShare(shareName);
			changeShare(share, shareProp);
		} else {
			addShare(shareProp);
		}
		save();
	}
	
	public static void main(String[] args) {
		try {
			ConfigurationParser.parse("config/config.xml");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		String shareName = "VNC-2";
		String path = "miPath3";
		String comments = "Comentario";
		
		ShareProp shareProp = new ShareProp(shareName, path, comments);
		wizard.createOrUpdate(shareName, shareProp);
		for (Share share:wizard.getShares()) {
			System.out.println(share);
		}
	}
	
}
