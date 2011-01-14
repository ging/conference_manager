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
	 * Logs de la aplicacion
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
	 * Modifica una sesion creada anteriormente.
	 * 
	 * @param newSession
	 *            Nueva representaci�n de la sesion.
	 * 
	 * @return Devuelve la sesion o un error.
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
			
			newSession.setConference(conference);
			newSession.setStatus(session.getStatus()); // Desde aqui no se puede modificar el estado por defecto.
			if (	session.getStartDate().equals(newSession.getStartDate()) &&
					session.getStopDate().equals(newSession.getStopDate()) &&
					session.isAutomaticRecord() == newSession.isAutomaticRecord() &&
					session.getEnableStreaming() == newSession.getEnableStreaming()) {
				
				ConferenceRegistry.updateSession(session.getId(), newSession);
				this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
				this.getResponse().setLocationRef("" + session.getId());
				
				return newSession;
			}
			
			if (session.getStartDate().before(new Date()) &&
					session.getStopDate().after(new Date())) {
				// La sesion esta corriendo ahora.
				if (session.isAutomaticRecord() != newSession.isAutomaticRecord()) {
					return createForbidden("Cannot change recording configuration in a running session");
				}
			}
			
			if (newSession.getStartDate().after(newSession.getStopDate())) {
				return createForbidden("Wrong dates");
			}else if (session.getStopDate().before(new Date())) {
				return createForbidden("Old session");
			} else if (newSession.getStopDate().before(new Date())) {
				return createForbidden("New stop date is before now");
			} else if (session.getStartDate().before(new Date()) && 
						!session.getStartDate().equals(newSession.getStartDate())) {
				return createForbidden("Cannot change start date from a running session");
			} else if (session.getStartDate().after(new Date()) &&
					newSession.getStartDate().before(new Date())) {
				return createForbidden("New start date is before now");
			} else if (!IsabelScheduler.getInstance().rescheduleSession(session, newSession)) {
				log.error("Conflict");
				ConflictEntity conflict = new ConflictEntity();
				getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,
				"Conflict");
				return conflict;
			}
			
			//ConferenceRegistry.updateSession(session.getId(), newSession);
			
			this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
			this.getResponse().setLocationRef("" + session.getId());
			
			return newSession;
		} catch (Exception e) {
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
	 * Elimina una sesion.
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
	 * Metodo que crea un mensaje de prohibicion.
	 * @param message
	 * Mensaje que incluir a la prohibicion.
	 * @return
	 * La prohibicion que se ha creado.
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
