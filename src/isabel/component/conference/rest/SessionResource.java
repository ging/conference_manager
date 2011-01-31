package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.ConflictEntity;
import isabel.component.conference.data.ForbiddenEntity;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.UnprocessableEntity;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.SchedulerResponse;

import java.util.Date;
import java.util.List;

import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionResource extends ServerResource {

	/**
	 * SessionResource logs.
	 */
	protected static Logger log = LoggerFactory
			.getLogger(SessionResource.class);
	
	private Conference conference = null;

	private Session session = null;

	/**
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {

		try {
		long conferenceID = Long.parseLong((String) getRequest()
				.getAttributes().get("conferenceID"));

		conference = ConferenceRegistry.get(conferenceID);

		if (getRequestAttributes().get("sessionID") != null) {
			long sessionID = Long.parseLong((String) getRequest()
					.getAttributes().get("sessionID"));
			if (conference != null) {
				List<Session> sessions = conference.getSession();
				for (Session session : sessions) {
					if (session.getId() == sessionID) {
						this.session = session;
						getRequestAttributes().put("session", session);
						break;
					}
				}
			}

			if (this.session == null) {
				NotFoundEntity notFound = new NotFoundEntity();
				notFound.session_not_found = (String) getRequestAttributes()
						.get("sessionID");
				if (conference == null) {
					notFound.event_not_found = (String) getRequestAttributes()
							.get("conferenceID");
				}
				getResponse().setEntity(
						new JaxbRepresentation<NotFoundEntity>(notFound));
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
						"Not Found");
			}
			setExisting(session != null);
		}
		} catch (Exception e) {
			
			getResponse().setEntity(
					new JaxbRepresentation<InternalErrorEntity>(new InternalErrorEntity().setException(e)));
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Internal Server Error");
			setExisting(false);
		}
	}
	
	/**
	 * It changes the parameters of the session. Even it can reschedule with new times.
	 * 
	 * @param newSession
	 *           New session representation.
	 * 
	 * @return The session or an error.
	 */
	@Put
	public Object changeSession(Session newSession) {
		try {
			log.debug("Updating session " + session.getId());
			UnprocessableEntity error = ConferenceRegistry.check(newSession);
			if (error != null) {
				this.getResponse().setStatus(
						Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
						"Unprocessable Entity");
			}
			
			// If the session does not change key parameters we can update it.
			newSession.setConference(conference);
			newSession.setStatus(session.getStatus()); // We can not update the state from this interface.
			if (	session.getStartDate().equals(newSession.getStartDate()) &&
					session.getStopDate().equals(newSession.getStopDate()) &&
					session.isAutomaticRecord() == newSession.isAutomaticRecord() &&
					session.getEnableStreaming() == newSession.getEnableStreaming()) {
				
				ConferenceRegistry.updateSession(session.getId(), newSession);
				this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
				this.getResponse().setLocationRef("" + session.getId());
				
				return newSession;
			}
			
			// If the session is running now and we want to change automatic recording we can not update it.
			if (session.getStartDate().before(new Date()) &&
					session.getStopDate().after(new Date())) {
				// Session is running now.
				if (session.isAutomaticRecord() != newSession.isAutomaticRecord()) {
					return createForbidden("Cannot change recording configuration in a running session");
				}
			}
			
			SchedulerResponse response = IsabelScheduler.getInstance().rescheduleSession(session, newSession);
			if (!response.ok) {
				ConflictEntity conflict = new ConflictEntity();
				getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, response.errorMessage);
				return conflict;
			}
			
			// We could update the session and schedule it.
			this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
			this.getResponse().setLocationRef("" + session.getId());
			
			return newSession;
		} catch (Exception e) {
			
			// For any exception we show the logs.
			log.error("Error updating session " + session.getId() + "; message: " + e.getLocalizedMessage());
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
			"Internal Server Error");
			return new InternalErrorEntity().setException(e);
		}
	}
	
	@Get
	public Object getSession() {
		this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
		return session;
	}
	
	/**
	 * Removes an old session.
	 * 
	 * @return
	 */
	@Delete
	public Object removeSession() {
		try {
			if (session.getStopDate().before(new Date())) {
				// La conferencia ha terminado. La podemos borrar.
				ConferenceRegistry.removeSession(session);
				getResponse().setStatus(Status.SUCCESS_OK,
						"OK");
			} else if (session.getStartDate().before(new Date())) {
				return createForbidden("The session is running");
			} else {
				this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
				IsabelScheduler.getInstance().unscheduleSession(conference, session);
			}
		} catch (Exception e) {
			log.error("Error removing session " + session.getId() + "; message: " + e.getLocalizedMessage());
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
			"Internal Server Error");
			return new InternalErrorEntity().setException(e);
		}
		return null;
	}
	
	/**
	 * It creates a Forbidden message.
	 * @param message
	 * Message to be included in the entity.
	 * @return
	 * Thge forbidden message entity.
	 */
	private ForbiddenEntity createForbidden(String message) {
		log.error("Forbidden: " + message);
		ForbiddenEntity forbidden = new ForbiddenEntity();
		forbidden.message = message;
		getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,
				"Forbidden");
		return forbidden;
	}
}
