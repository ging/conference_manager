package isabel.component.conference.tests;

import isabel.component.conference.rest.ConferenceResource;
import isabel.component.conference.rest.ConferencesResource;
import isabel.component.conference.rest.SessionResource;
import isabel.component.conference.rest.SessionStatusResource;
import isabel.component.conference.rest.SessionsResource;

import java.util.HashMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.resource.ServerResource;

public class ResourceTestManager {
	
	private long conference=0;
	private long session=0;
	
	public ConferencesResource getConferencesResource() {
		return (ConferencesResource)initServerResource(new ConferencesResource());
	}
	
	public ConferenceResource getConferenceResource(long conference) {
		this.conference = conference;
		return (ConferenceResource)initServerResource(new ConferenceResource());
	}
	
	public SessionsResource getSessionsResource(long conference) {
		this.conference = conference;
		return (SessionsResource)initServerResource(new SessionsResource());
	}
	
	public SessionResource getSessionResource(long conference, long session) {
		this.conference = conference;
		this.session = session;
		return (SessionResource)initServerResource(new SessionResource());
	}
	
	public SessionStatusResource getSessionStatusResource(long conference, long session) {
		this.conference = conference;
		this.session = session;
		return (SessionStatusResource)initServerResource(new SessionStatusResource());
	}
	
	private ServerResource initServerResource(ServerResource res) {
    	Request req2 = new Request();
    	HashMap<String, Object> attr = new HashMap<String, Object>();
    	attr.put("conferenceID", conference+"");
    	attr.put("sessionID", session+"");
    	req2.setAttributes(attr);
    	req2.setResourceRef(new Reference("http://hostname.org/path"));
    	Response resp2 = new Response(req2);
    	res.init(new Context(), req2, resp2);
    	return res;
	}
}
