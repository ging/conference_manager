package isabel.component.conference.rest;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enrutador de las llamadas REST a /conferences
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class ConferenceRouter extends Application {
	
	protected static Logger log = LoggerFactory
	.getLogger(ConferenceRouter.class);

	/**
	 * @see org.restlet.Application#createInboundRoot()
	 */
	@Override  
	public Restlet createInboundRoot() {

		Router router = new Router(getContext());
		
		router.setDefaultMatchingMode(Template.MODE_EQUALS);
		router.setDefaultMatchQuery(false);

		router.attach("/events", ConferencesResource.class);
		router.attach("/events/", ConferencesResource.class);
		router.attach("/events/{conferenceID}", ConferenceResource.class);
		router.attach("/events/{conferenceID}/player", PlayerResource.class);
		router.attach("/events/{conferenceID}/web", WebParticipationResource.class);
		router.attach("/events/{conferenceID}/webstat", WebStatsResource.class);
		router.attach("/events/{conferenceID}/webmap", WebMapResource.class);
		router.attach("/events/{conferenceID}/streaming", StreamingResource.class);
		router.attach("/events/{conferenceID}/start", StartConferenceResource.class);
		router.attach("/events/{conferenceID}/restart", RestartConferenceResource.class);
		router.attach("/events/{conferenceID}/stop", StopConferenceResource.class);
		router.attach("/events/{conferenceID}/sessions", SessionsResource.class);
		router.attach("/events/{conferenceID}/sessions/", SessionsResource.class);
		router.attach("/events/{conferenceID}/sessions/{sessionID}/start", StartSessionResource.class);
		router.attach("/events/{conferenceID}/sessions/{sessionID}/stop", StopSessionResource.class);
		router.attach("/events/{conferenceID}/sessions/{sessionID}", SessionResource.class);
		router.attach("/events/{conferenceID}/sessions/{sessionID}/player", PlayerResource.class);
		router.attach("/events/{conferenceID}/sessions/{sessionID}/editor", EditorResource.class);
		router.attach("/events/{conferenceID}/sessions/{sessionID}/session-status", SessionStatusResource.class);
		router.attach("/availability/", AvailabilityResource.class);
		router.attach("/monitor", MonitorResource.class);
		Filter filter = new Filter() {
			public int beforeHandle(Request req, Response resp) {
				log.debug("New " + req.getMethod() + " Request from " + req.getClientInfo().getAddress() + ": " + req.getResourceRef().getPath());
				return Filter.CONTINUE;
			}
			
			public void afterHandle(Request req, Response resp) {
				log.debug("Response: " + resp.getStatus().getCode() + " - Message: " + resp.getStatus().getDescription());
			}
		};
		filter.setNext(router);
		return filter;
	}
}
