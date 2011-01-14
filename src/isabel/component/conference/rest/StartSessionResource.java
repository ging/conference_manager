package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.Session;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.jobs.StartSessionJob;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartSessionResource extends ServerResource {

	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory.getLogger(StartSessionJob.class);
	
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
	@Post
	public void startSession() {
		IsabelScheduler.getInstance().getJobScheduler().unscheduleStartSessionJob(session);
		if (session.isAutomaticRecord()) {
			// Borramos, si existen, los eventos de inicio de conferencia programados.
			StartSessionJob startJob = new StartSessionJob(conference, session);
			startJob.startSession();
		}
	}
}
