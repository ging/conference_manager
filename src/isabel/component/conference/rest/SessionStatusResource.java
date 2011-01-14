package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.ForbiddenEntity;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.Session;
import isabel.component.conference.data.SessionStatus;
import isabel.component.conference.scheduler.jobs.StartSessionJob;
import isabel.component.conference.scheduler.jobs.StopSessionJob;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionStatusResource extends ServerResource {
	
	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory.getLogger(SessionStatus.class);
	
	private Conference conference;
	private Session session;

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
			log.error(e.getLocalizedMessage());
			getResponse().setEntity(
					new JaxbRepresentation<InternalErrorEntity>(
							new InternalErrorEntity().setException(e)));
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Internal Server Error");
			setExisting(false);
		}
	}

	/**
	 * Devuelve el codigo que hay que empotrar en la p�gina para mostrar el
	 * reproductor.
	 * 
	 * @return
	 */
	@Put
	public synchronized Object changeSessionStatus(SessionStatus status) {
		log.debug("Changing status to " + status.getStatus() + "; current: " + session.getStatus());
		if (session.isAutomaticRecord()) {
			return createForbidden("The session is automatic"); 
		}
		if (status.getStatus()==null) {
			return createForbidden("The status is null");
		}
		if (status.getStatus().equals(session.getStatus())) {
			log.debug("The status has not changed");
			return null;
		}
		
		switch (status.getStatus()) {
		case INIT:
			switch(session.getStatus()) {
			default:
				return createForbidden("Status forbidden");
			}
		case RECORDING:
			switch(session.getStatus()) {
			case INIT:
				// Empezamos a grabar por primera vez.
			case RECORDED:
				// Empezamos a grabar otra vez (ya hay un video anterior).
				startRecording();
				break;
			default:
				return createForbidden("Status forbidden");
			}
			break;
		case RECORDED:
			switch(session.getStatus()) {
			case RECORDING:
				// Paramos la grabacion
				stopRecording();
				break;
			default:
				return createForbidden("Status forbidden");
			}
			break;
		case PUBLISHED:
			switch(session.getStatus()) {
			case RECORDED:
				// Publicamos el video.
				publishRecorded();
				break;
			default:
				return createForbidden("Status forbidden");
			}
			break;
		}
		return null;
	}
	
	@Get
	public synchronized SessionStatus getSessionStatus() {
		try {
			SessionStatus status = new SessionStatus();
			status.setStatus(session.getStatus());
			return status;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
		return null;
	}
	
	private void startRecording() {
		for (Session session : conference.getSession()) {
			if (session.getStatus().equals(isabel.component.conference.data.Session.Status.RECORDING)) {
				// Actualizamos su valor.
				// Paramos la grabacion de la sesion.
				StopSessionJob stopJob = new StopSessionJob(conference, session);
				stopJob.stopSession();
			}
		}
		
		// Lo ponemos a grabar.
		StartSessionJob startJob = new StartSessionJob(conference, session);
		startJob.startSession();
	}
	
	private void stopRecording() {
		StopSessionJob stopJob = new StopSessionJob(conference, session);
		stopJob.stopSession();
	}
	
	private void publishRecorded() {
		StopSessionJob stopJob = new StopSessionJob(conference, session);
		stopJob.publishSession();
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
