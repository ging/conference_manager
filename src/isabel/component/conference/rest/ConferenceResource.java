package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.ConflictEntity;
import isabel.component.conference.data.ForbiddenEntity;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.UnprocessableEntity;
import isabel.component.conference.scheduler.IsabelScheduler;

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
/**
 * @author javi
 * 
 */
public class ConferenceResource extends ServerResource {
	
	protected static Logger log = LoggerFactory
	.getLogger(ConferenceResource.class);
	
	/**
	 * Conferencia sobre la que llega una nueva peticion REST
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
	 * Devuelve a un GET la representaci�n XML de una conferencia.
	 * 
	 * @return
	 */
	@Get
	public Object represent() {
		this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
		return conference;
	}

	/**
	 * Elimina la conferencia ante un DELETE
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
	 * Modifica una conferencia creada anteriormente.
	 * 
	 * @param newConference
	 *            Nueva representacion de la conferencia.
	 * 
	 * @return Devuelve la conferencia o un error.
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
				
				if (!newConference.getStartTime().before(newConference.getStopTime())) {
					return createForbidden("Start time after stop time");
				}
	
				if (conference.getResourcesStopTime().before(new Date())) {
					return createForbidden("Old conference");
				} else if (conference.getResourcesStartTime().before(new Date()) &&
						(!newConference.getResourcesStartTime().equals(conference.getResourcesStartTime()))	
					) {
					return createForbidden("Cannot change start date from a conference that is running");
				} else if (conference.getResourcesStartTime().after(new Date()) &&
						newConference.getResourcesStartTime().before(new Date())) {
					return createForbidden("Start date is before now");
				} else if (conference.getResourcesStopTime().after(new Date()) &&
						newConference.getResourcesStopTime().before(new Date())) {
					return createForbidden("Stop date is before now.");
				} else {
					if (!IsabelScheduler.getInstance().rescheduleConference(newConference, conference)) {
						log.error("There was a conflict");
						ConflictEntity conflict = new ConflictEntity();
						getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,
						"Conflict");
						return conflict;
					}
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
