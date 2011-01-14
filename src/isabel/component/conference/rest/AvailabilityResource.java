package isabel.component.conference.rest;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.Conference;

import java.sql.Date;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class AvailabilityResource extends ServerResource {
	
	/**
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		
	}
	
	@Get
	public void checkAvailability() {
		Form form = getRequest().getResourceRef().getQueryAsForm();
		
		String type = form.getFirst("type").getValue();
		
		Parameter startParam = form.getFirst("start");
		Parameter endParam = form.getFirst("end");
		
		long startTimeInMilliseconds = Long.parseLong(startParam.getValue());
		long endTimeInMilliseconds = Long.parseLong(endParam.getValue());
		
		Date startDateTime = new Date(startTimeInMilliseconds);
		Date stopDateTime = new Date(endTimeInMilliseconds);
		
		if (type.trim().equalsIgnoreCase("simple")) {
			
			boolean ok = IsabelMachineRegistry.checkAvailability(startDateTime, stopDateTime);
			if (ok) {
				this.getResponse().setStatus(Status.SUCCESS_OK);
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
			}
		} else if (type.trim().equalsIgnoreCase("extended")) {
			
			
		} else if (type.trim().equalsIgnoreCase("conference")) {
			Long id = Long.getLong(form.getFirst("conference").getValue());
			Conference conference = ConferenceRegistry.get(id);
			
			boolean ok = IsabelMachineRegistry.checkAvailability(startDateTime, stopDateTime, conference);
			if (ok) {
				this.getResponse().setStatus(Status.SUCCESS_OK);
			} else {
				this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
			}
		}
		
		
		
	}
}
