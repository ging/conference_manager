package isabel.component.conference.tests;

import java.util.Date;

public class TimeTestManager {
	
	public static Date now;
	
	public static void init() {
		long time = (new Date()).getTime();
    	time = time-time%1000;
    	
    	now = new Date(time);
	}
	
	public static Date getTime(int index) {
		return new Date(now.getTime() + index * 5*60*1000); 
	}
}
