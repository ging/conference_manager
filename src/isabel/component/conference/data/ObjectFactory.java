package isabel.component.conference.data;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * Clase que se dedica a generar objetos. Es utilizada por JAXB para hacer el marshalling/unmarshalling de 
 * los documentos XML a Objetos Java.
 * 
 * @author jcervino@dit.upm.es
 *
 */
@XmlRegistry
public class ObjectFactory {
	
	/**
	 * Crea una instancia el la fabrica de objetos
	 */
	public ObjectFactory() {
		
	}
	
	/**
	 * Devuelve un nuevo objeto de una lista de conferencias.
	 * @return
	 */
	public static Conferences createConferences()  {
		return new Conferences();
	}
	
	/**
	 * Devuelve un nuevo objeto de conferencia.
	 * @return
	 */
	public static Conference createConference()  {
		return new Conference();
	}
	
	/**
	 * Devuelve un nuevo objeto de sesi�n.
	 * @return
	 */
	public static Session createSession() {
		return new Session();
	}
	
	/**
	 * Devuelve un nuevo objeto UnprocessableEntity.
	 * @return
	 */
	public static UnprocessableEntity createUnprocessableEntity() {
		return new UnprocessableEntity();
	}
	
	/**
	 * Devuelve un nuevo objeto NotFoundEntity.
	 * @return
	 */
	public static NotFoundEntity createNotFoundEntity() {
		return new NotFoundEntity();
	}
	
	/**
	 * Devuelve un nuevo objeto InternalErrorEntity.
	 * @return
	 */
	public static InternalErrorEntity createInternalErrorEntity() {
		return new InternalErrorEntity();
	}
	
	/**
	 * Devuelve un nuevo objeto ForbiddenEntity.
	 * @return
	 */
	public static ForbiddenEntity createForbiddenEntity() {
		return new ForbiddenEntity();
	}
	
	/**
	 * Devuelve un nuevo objeto ConflictEntity.
	 * @return
	 */
	public static ConflictEntity createConflictEntity() {
		return new ConflictEntity();
	}
	
	/**
	 * Devuelve un nuevo objeto ConflictEntity.
	 * @return
	 */
	public static SessionStatus createSessionStatus() {
		return new SessionStatus();
	}
	
	/**
	 * Devuelve un nuevo objeto ConflictEntity.
	 * @return
	 */
	public static ConferenceStatus createConferenceStatus() {
		return new ConferenceStatus();
	}
	
}
