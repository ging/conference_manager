package isabel.component.conference.AWS;


public class AWSInstance {
	
	public String instanceID;
	public String ipAddress;
	public String amiID;
	
	/*public static void writeInstanceInfo(String amiID) {
		
	//String ipAddress = null;
	//String instanceID = null;
	
	String instanceID = AWSAPI.startHost(amiID);
	String ipAddress = AWSAPI.updateHostIP(amiID);
	amiID = AWSAPI.returnAMI(amiID);
	
	String instID = instanceID;
	
	AWSAPI.stopHost(instID);
	
	}*/

}
