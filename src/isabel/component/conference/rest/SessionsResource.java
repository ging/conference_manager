package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.ConflictEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.UnprocessableEntity;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.SchedulerResponse;

import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionsResource extends ServerResource {
	protected static Logger log = LoggerFactory
			.getLogger(SessionsResource.class);

	private Conference conference = null;
	
	/**
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		// Get the "itemName" attribute value taken from the URI template
		// /items/{itemName}.
		long conferenceID = Long.parseLong((String) getRequest()
				.getAttributes().get("conferenceID"));
		conference = ConferenceRegistry.get(conferenceID);
		getRequestAttributes().put("conference", conference);
		
		if (conference == null) {
			NotFoundEntity notFound = new NotFoundEntity();
			notFound.event_not_found = "" + conferenceID;
			getResponse().setEntity(
					new JaxbRepresentation<NotFoundEntity>(notFound));
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Not Found");
		}
		setExisting(conference != null);
	}
	
	/**
	 * Creates a new session in a conference with the given parameters 
	 * 
	 * @param session
	 *            Info for the session to be created.
	 * @return
	 * 			The session created with more information about the conference.
	 */
	@Post
	public Object createSession(Session session) {
		log.debug("Creating new session.");
		try {
			UnprocessableEntity error = ConferenceRegistry.check(session);
			if (error != null) {
				this.getResponse().setStatus(
						Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
						"Unprocessable Entity");
			}

			log.debug("New session request with name " + session.getName() + "; in conference " + conference.getId());
			
			// We already know the conference that owns it. Try scheduling.
			SchedulerResponse response = IsabelScheduler.getInstance().scheduleSession(conference.getId(), session);

			if (response.ok) {
			
				getResponse().setLocationRef("sessions/" + session.getId());
				this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
				return session;
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, response.errorMessage);
				ConflictEntity ent = new ConflictEntity();
				return ent;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Internal Server Error");
			return null;
		}
	}
}
