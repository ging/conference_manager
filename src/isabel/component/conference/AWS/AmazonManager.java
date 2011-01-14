package isabel.component.conference.AWS;

import isabel.component.conference.CloudManager;
import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.Conference;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.scheduler.IsabelScheduler;
import isabel.component.conference.util.ConfigurationParser;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmazonManager implements CloudManager {
	
	/**
	 * Logs de la aplicacion
	 */
	protected static Logger log = LoggerFactory
			.getLogger(IsabelScheduler.class);

	private boolean isabelInitialized = false;
	
	//TODO private boolean vncInitialized = false;
	
	@Override
	public void cleanConference(Conference conference) {
		List<IsabelMachine> machines = new ArrayList<IsabelMachine>();
		for (IsabelMachine machine : conference.getIsabelMachines()) {
			switch (machine.getCloud()) {
			case Amazon:
				machines.add(machine);
				break;
			}
		}
		
		AWSAPI.amazonURL = ConfigurationParser.amazonURL;
		AWSAPI.serviceKey = ConfigurationParser.amazonServiceKey;
		AWSAPI.serviceSecret = ConfigurationParser.amazonServiceSecret;
		
		MachinesEraser eraser = new MachinesEraser(machines);
		eraser.start();
		
	}

	@Override
	public void prepareConference(Conference conference) {
		// Iniciamos todas las m�quinas en paralelo para no tener que esperar demasiado tiempo.
		
		List<IsabelMachine> vncs = new ArrayList<IsabelMachine>();
		List<IsabelMachine> isabeles = new ArrayList<IsabelMachine>();
		for (IsabelMachine machine : conference.getIsabelMachines()) {
			switch (machine.getType()) {
			case Isabel:
				isabeles.add(machine);
				break;
			case VNC:
				vncs.add(machine);
				break;
			}
		}
		
		AWSAPI.amazonURL = ConfigurationParser.amazonURL;
		AWSAPI.serviceKey = ConfigurationParser.amazonServiceKey;
		AWSAPI.serviceSecret = ConfigurationParser.amazonServiceSecret;
		
		log.info("Starting amazon instances...");
		
		// Iniciamos las maquinas de Isabel.
		MachinesInitializer initIsabel = new MachinesInitializer(this, ConfigurationParser.amazonIsabelAMI, "c1.medium", isabeles);
		
		// Iniciamos las maquinas de VNC.
		//TODO MachinesInitializer initVNC = new MachinesInitializer(this, ConfigurationParser.amazonVNCAMI, "m1.small", vncs);
		
		initIsabel.start();
		//initVNC.start();
		
		while (!isabelInitialized) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		log.info("Amazon instances started.");
		try {
			Thread.sleep(90000);
		} catch (Exception e) {
		}
		
	}
	
	protected void machinesInitialized(String ami) {
		if (ami.equals(ConfigurationParser.amazonIsabelAMI)) {
			isabelInitialized = true;
		} else {
			//TODO vncInitialized = true;
		}
	}
	
}

class MachinesInitializer extends Thread {
	
	private String _amiID;
	private String _size;
	private AmazonManager _manager;
	private List<IsabelMachine> _machines;
	
	public MachinesInitializer(AmazonManager amazonManager, String amiID, String size, List<IsabelMachine> machines) {
		_amiID = amiID;
		_size = size;
		_machines = machines;
		_manager = amazonManager;
	}
	
	public void run() {
		List<AWSInstance> instances = AWSAPI.startHost(_amiID, _size, _machines.size());
		
		for (int i = 0; i < instances.size(); i++) {
			_machines.get(i).setExternalID(instances.get(i).instanceID);
			_machines.get(i).setHostname(instances.get(i).ipAddress);
			IsabelMachineRegistry.updateIsabelMachine(_machines.get(i).getId(), _machines.get(i));
		}
		
		_manager.machinesInitialized(_amiID);
	}
}

class MachinesEraser extends Thread {
	private List<IsabelMachine> _machines;
	
	public MachinesEraser(List<IsabelMachine> machines) {
		_machines = machines;
	}
	
	public void run() {
		
		List<AWSInstance> instances = new ArrayList<AWSInstance>();
		
		for (IsabelMachine machine : _machines) {
			AWSInstance instance = new AWSInstance();
			instance.instanceID = machine.getExternalID();
			instances.add(instance);
			machine.setExternalID("");
			machine.setHostname("");
			IsabelMachineRegistry.updateIsabelMachine(machine.getId(), machine);
		}
		
		AWSAPI.stopHost(instances);
		
	}
}