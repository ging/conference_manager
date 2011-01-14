package isabel.component.conference.tests;

import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.scheduler.JobScheduler;

import java.util.List;

public class TestConfigurator {
	
	public static void initializeAll() {
		// Iniciamos el programador de tareas de arranque y parada.
		JobScheduler.configFile = "config/quartz2test.properties";
		IsabelScheduler scheduler = IsabelScheduler.getInstance();
		
		try {
		scheduler.getJobScheduler().unscheduleAllJobs();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		scheduler.start();

		// Borramos las conferencias antiguas.
		List<Conference> conferences = ConferenceRegistry.getConferences();
		if (conferences.size() > 0) {
			for (Conference conf : conferences) {
				ConferenceRegistry.remove(conf);
			}
		}

		// Borramos los isabeles antiguos.
		List<IsabelMachine> machines = IsabelMachineRegistry
				.getIsabelMachines();
		if (machines.size() > 0) {
			for (IsabelMachine machinee : machines) {
				IsabelMachineRegistry.removeIsabelMachine(machinee);
			}
		}
	}
}
