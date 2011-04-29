package isabel.component.conference.scheduler.jobs;

import isabel.component.conference.CloudManager;
import isabel.component.conference.ConferenceRegistry;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.VMWareManager;
import isabel.component.conference.AWS.AmazonManager;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.Session;
import isabel.component.conference.dst.DSTManager;
import isabel.component.conference.ist.ISTManager;
import isabel.component.conference.monitoring.MonitorManager;
import isabel.component.conference.util.ConfigurationParser;
import isabel.component.conference.vst.VSTManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopConferenceJob extends ConferenceJob {
	
	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory.getLogger(StopConferenceJob.class);
	
	/**
	 * Para la maquina Spy.
	 * @param spy
	 */
	private void shutdownSpy(IsabelMachine spy) {
		log.info("Conference: " + conference.getName() + "; Spy " + spy.getHostname() + " shutdown");
		
		if (manager == null)
			manager =  new ISTManager();
		
		try {
			manager.killIsabel(spy.getHostname());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Para la maquina Gateway Flash.
	 * @param gateway
	 */
	private void shutdownIsabel(IsabelMachine isabel) {
		log.info("Conference: " + conference.getName() + "; Isabel machine " + isabel.getHostname() + " shutdown");
		
		if (manager == null)
			manager =  new ISTManager();
		
		try {
			manager.killIsabel(isabel.getHostname());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Long id = (Long)context.getJobDetail().getJobDataMap().get("conference");
		Conference conference = ConferenceRegistry.get(id);
		
		// Si hay alguna sesion grabandose manualmente la paramos y publicamos.
		for (Session session:conference.getSession()) {
			if (!session.isAutomaticRecord()) {
				StopSessionJob stopJob = new StopSessionJob(conference, session);
				switch(session.getStatus()) {
				case RECORDING:
					stopJob.stopSession();
				case RECORDED:
					stopJob.publishSession();
					break;
				}
			}
		}
		
		stopConference(ConferenceRegistry.get(id));
	}
	
	/**
	 * Metodo que para los Isabeles de la conferencia y restaura la configuracion.
	 * 
	 * @param conference
	 * Conferencia que va a finalizar.
	 * 
	 * @throws JobExecutionException
	 */
	public void stopConference(Conference conference) throws JobExecutionException {
		synchronized (StopSessionJob.class) {
			
			this.conference = conference;
			
			if (conference == null) {
				throw new JobExecutionException("Error retrieving conference information");
			}
			
			log.info("Conference " + conference.getName() + " stops at " + new Date());
			
			// Paramos la monitorizacion.
			try {
				MonitorManager.getInstance().stopConferenceMonitoring(conference);
			} catch (Exception e) {
			}
			
			// Generamos las listas de Isabels, Espias y VNCs.
			List<IsabelMachine> vIsabeles = new ArrayList<IsabelMachine>();
			List<IsabelMachine> spies = new ArrayList<IsabelMachine>();
			List<IsabelMachine> vncs = new ArrayList<IsabelMachine>();
			for (IsabelMachine machine : conference.getIsabelMachines()) {
				
				// Vamos borrando la informacion guardada sobre las maquinas de la conferencia.
				machine.setIsabelNodeType(null);
				IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
				
				switch (machine.getType()) {
				case Isabel:
					vIsabeles.add(machine);
					break;
				case Spy:
					spies.add(machine);
					break;
				case VNC:
					vncs.add(machine);
					break;
				}
			}
			
			// Paramos maquinas.
			
			for (IsabelMachine spy : spies) {
				shutdownSpy(spy);
			}
			
			for (IsabelMachine isabel : vIsabeles) {
				shutdownIsabel(isabel);
			}
			
			// Quitamos la configuracion del VNC.
			VSTManager vst = new VSTManager();
			for (IsabelMachine vnc : vncs) {
				try {
					if (!ConfigurationParser.debug) {
						vst.setDefaultFolder(vnc);
					}
				} catch (Exception e) {
					log.error("Error setting default folder on Samba" + e.getLocalizedMessage());
				}
			}
			
			// Comprobamos en que Cloud estaban las maquinas para tener que apagarlas o no.
			CloudManager manager = null;
			
			if (conference.getCloud() != null) {
			
				switch (conference.getCloud()) {
				case VMWare:
					manager = new VMWareManager();
					break;
				case Amazon:
					manager = new AmazonManager();
					break;
				}
			}
			manager.cleanConference(conference);
			
			conference = ConferenceRegistry.get(conference.getId());
			
			// Restaura la configuracion del DNS.
			DSTManager dst = new DSTManager();
			try {
				dst.delDns(conference.getId() + "", null);
			} catch (IOException e) {
				log.error("Error deleting Dynamic DNS " + conference.getId());
			}
			
			// Desasignamos las maquinas a la conferencia.
			ConferenceRegistry.removeIsabelMachinesFromConference(conference);

		}
	}

}
