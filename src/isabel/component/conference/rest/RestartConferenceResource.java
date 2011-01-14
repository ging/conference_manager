package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.scheduler.IsabelScheduler;

import java.util.Date;

import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestartConferenceResource extends ServerResource {

	protected static Logger log = LoggerFactory
		.getLogger(RestartConferenceResource.class);
	
	private Conference conference;
	
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
	
	@Post
	public void restartConference() {
		Date now = new Date();
		Date later = new Date(now.getTime()+60*1000 /* 1 minuto */);
		
		// Borramos la tarea de inicio de conferencia.
		IsabelScheduler.getInstance().getJobScheduler().unscheduleStartConferenceJob(conference);

		// Paramos la conferencia.
		IsabelScheduler.getInstance().getJobScheduler().scheduleStopConferenceJob(conference, now, conference.getId() + "confstopRestart");
		
		// Iniciamos la conferencia.
		IsabelScheduler.getInstance().getJobScheduler().scheduleStartConferenceJob(conference, later);
	}
}
