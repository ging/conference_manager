package isabel.component.conference.rest;

import isabel.component.conference.monitoring.MonitorManager;

import javax.ws.rs.Produces;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Representacion del estado de la monitorizacion.
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class MonitorResource extends ServerResource {
	
	/** Logger object. */
	protected static Logger log = LoggerFactory.getLogger(MonitorResource.class);

	@Get
	@Produces("text/html")
	public Representation getMonitorStatus() {
		MonitorManager mon = MonitorManager.getInstance();
		StringBuilder html = new StringBuilder();
		html.append("<html><head><title>Monitoring console</title></head><body>");
		Form form = getRequest().getResourceRef().getQueryAsForm();
		Parameter historical = form.getFirst("historical");
		if (historical != null) {
			html.append(mon.getHistoricalHTML());
		} else {
			html.append(mon.getStatusHTML());
		}
		html.append("</body></html>");
		StringRepresentation repr = new StringRepresentation(html.toString());
		repr.setMediaType(MediaType.TEXT_HTML);

		return repr;
	}
}
