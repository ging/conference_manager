package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.InternalErrorEntity;
import isabel.component.conference.data.NotFoundEntity;
import isabel.component.conference.data.Session;
import isabel.component.conference.util.ConfigurationParser;

import java.io.File;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that represents video sessions.
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class VideoSessionResource extends ServerResource {
	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(PlayerResource.class);
	
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
	 * Representation of the video.
	 * @return
	 * The FileRepresentation of the video, or a NotFound message if it does not exist.
	 */
	@Get
	public Representation getVideo() {
		File videoFile = new File(ConfigurationParser.videoPath + "/" + conference.getId() + "/" + conference.getId() + "_" + session.getId());
		if (videoFile.exists())
		{
			FileRepresentation representation = new FileRepresentation(videoFile, MediaType.VIDEO_MP4);
			return representation;
		} else {
			NotFoundEntity notFound = new NotFoundEntity();
			notFound.session_not_found = (String) getRequestAttributes()
					.get("sessionID");
			if (conference == null) {
				notFound.event_not_found = (String) getRequestAttributes()
						.get("conferenceID");
			}
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"Not Found");
			
			setExisting(false);
			
			return new JaxbRepresentation<NotFoundEntity>(notFound);
		}
	}
}
