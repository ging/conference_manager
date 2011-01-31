package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.ConflictEntity;
import isabel.component.conference.data.ForbiddenEntity;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.UnprocessableEntity;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.SchedulerResponse;

import java.util.Date;

import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jcervino@dit.upm.es
 * 
 */
public class ConferenceResource extends ServerResource {
	
	protected static Logger log = LoggerFactory
	.getLogger(ConferenceResource.class);
	
	/**
	 * Conference that will be managed.
	 */
	private Conference conference;
	
	public ConferenceResource() {
		super();
	}
	
	public ConferenceResource(Conference conference) {
		super();
		this.conference = conference;
	}
	
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
	 * Returns the representation of a conference.
	 * 
	 * @return
	 */
	@Get
	public Object represent() {
		this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
		return conference;
	}

	/**
	 * Removes a conference.
	 */
	@Delete
	public Object remove() {
		try {
			synchronized (IsabelScheduler.getInstance()) {
			
				this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
	
				if (conference.getResourcesStopTime().before(new Date())) {
					// La conferencia ha terminado.
					// Lo borramos
					ConferenceRegistry.remove(conference);
					getResponse().setStatus(Status.SUCCESS_OK,
							"OK");
					// TODO Habria que borrar tambien los video grabados? 
				} else if (conference.getResourcesStartTime().before(new Date())) {
					return createForbidden("The conference is running now");
				} else {
					// No ha empezado
					IsabelScheduler.getInstance().unscheduleConference(conference);
				}
			
			}
			return " ";
		} catch (Exception e) {
			log.error("Error removing Conference " + conference.getId() + "; message: " + e.getLocalizedMessage());
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
			"Internal Server Error");
			return new InternalErrorEntity().setException(e);
		}
	}

	/**
	 * Changes a conference that is on the database.
	 * 
	 * @param newConference
	 *      New representation of the conference.
	 * 
	 * @return Return the conference or an error message.
	 */
	@Put
	public Object changeConference(Conference newConference) {
		try {
			Conference conf;
			log.info("Updating conference " + conference.getId() + " to: " + newConference.toString());
			synchronized (IsabelScheduler.getInstance()) {
				UnprocessableEntity error = ConferenceRegistry.check(newConference);
				if (error != null) {
					log.error("Error checking new conference");
					this.getResponse().setStatus(
							Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
							"Unprocessable Entity");
				}
				SchedulerResponse response = IsabelScheduler.getInstance().rescheduleConference(newConference, conference);
				if (!response.ok) {
					ConflictEntity conflict = new ConflictEntity();
					getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,response.errorMessage);
					return conflict;
				}
				
				
			}
			this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
			this.getResponse().setLocationRef("" + conference.getId());
			conf = ConferenceRegistry.get(conference.getId());
			return conf;
			
		} catch (Exception e) {
			log.error("Error updating conference " + conference.getId() + "; message: " + e.getLocalizedMessage());
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "ERROR");
			return new InternalErrorEntity().setException(e);
		}
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
