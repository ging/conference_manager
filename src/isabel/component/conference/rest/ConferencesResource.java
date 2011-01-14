package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.Conferences;
import isabel.component.conference.data.ConflictEntity;
import isabel.component.conference.data.ForbiddenEntity;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.UnprocessableEntity;
import isabel.component.conference.scheduler.IsabelScheduler;

import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representaci�n de una colecci�n de conferencias de la aplicaci�n.
 * 
 * Sobre �l podremos crear nuevas conferencias (POST) y listar las ya existentes
 * (GET).
 * 
 * @author jcervino@dit.upm.es
 * 
 */
public class ConferencesResource extends ServerResource {

	protected static Logger log = LoggerFactory
			.getLogger(ConferencesResource.class);
	
	public ConferencesResource() {
		super();
	}

	/**
	 * Crea una nueva conferencia a partir del POST recibido junto con un
	 * documento XML que representa a la nueva conferencia.
	 * 
	 * @param conference
	 *            Nueva conferencia que queremos crear.
	 * 
	 * @return Devuelve la representaci�n de la conferencia actualizada.
	 */
	@Post
	public Object createConference(Conference conference) {
		try {
			synchronized (IsabelScheduler.getInstance()) {
				UnprocessableEntity error = ConferenceRegistry.check(conference);
				if (error != null) {
					this.getResponse().setStatus(
							Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
							"Unprocessable Entity");
				}
			
				log.info("New Conference received. " + conference.toString());
				
				if (!conference.getStartTime().before(conference.getStopTime())) {
					// La conferencia ha terminado.
					ForbiddenEntity forbidden = new ForbiddenEntity();
					forbidden.message = "Start time after stop time";
					getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,
							"Forbidden");
					return forbidden;
				}
			
				if (IsabelScheduler.getInstance().scheduleConference(conference)) {
					log.debug("Conference " + conference.getName()
						+ " created and scheduled");
	
					this.getResponse().setLocationRef("events/" + conference.getId());
					this.getResponse().setStatus(Status.SUCCESS_OK, "OK");
					return conference;
				} else {
					this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Conflict");
					return new ConflictEntity();
				}
			}
		} catch (Exception e) {
			log.error("Error creating conference: " + e.getLocalizedMessage());
			e.printStackTrace();
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					"Internal Server Error");
			return new InternalErrorEntity().setException(e);
		}
	}

	/**
	 * Obtiene una lista de conferencias ya creadas.
	 * 
	 * @return
	 */
	@Get
	public Conferences getList() {
		Conferences list = new Conferences(ConferenceRegistry.getConferences());
		return list;
	}

}
