package isabel.component.conference.tests;

import java.util.List;

import isabel.component.conference.IsabelMachineRegistry;
import isabel.component.conference.data.IsabelMachine;
import isabel.component.conference.data.IsabelMachine.SubType;
import junit.framework.TestCase;

public class IsabelMachinesTest extends TestCase {
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    protected void setUp() {
    	
    }
    
    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() {
    	TestConfigurator.initializeAll();
    }
    
    public void testEmptyMachines() {
    	List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
		assertTrue("Hay maquinas en la base de datos", machines.size() == 0);
    }
    
    public void testAddIsabelMachineDirectly() {
    	IsabelMachine machine = addIsabelMachine("machine1", SubType.vmIsabel);
		
		List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
		assertTrue("Hay más máquinas de las que debería", machines.size() <= 1);
		assertTrue("Hay menos máquinas de las que debería", machines.size() >= 1);
		IsabelMachine result = machines.get(0);
		assertTrue("No se ha guardado correctamente", machine.equals(result));
    }
    
    public void testRemoveIsabelMachineDirectly() {
    	addIsabelMachine("machine1", SubType.vmIsabel);
		
		List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
		IsabelMachine result = machines.get(0);
		
		IsabelMachineRegistry.removeIsabelMachine(result);
		
		machines = IsabelMachineRegistry.getIsabelMachines();
		assertTrue("No se ha borrado la máquina de la base de datos", machines.size() == 0);
    }
    
    public void testAddVMIsabel() {
    	IsabelMachine machine = addIsabelMachine("machine1", SubType.vmIsabel);
    	List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
    	IsabelMachine result = machines.get(0);
		assertTrue("No se ha guardado correctamente una maquina vmIsabel", machine.equals(result));
    }
    
    public void testAddVMVNC() {
    	IsabelMachine machine = addIsabelMachine("machine1", SubType.vmVNC);
    	List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
    	IsabelMachine result = machines.get(0);
		assertTrue("No se ha guardado correctamente una maquina vmVNC", machine.equals(result));
    }
    
    public void testAddAMIIsabel() {
    	IsabelMachine machine = addIsabelMachine("machine1", SubType.amiIsabel);
    	List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
    	IsabelMachine result = machines.get(0);
		assertTrue("No se ha guardado correctamente una maquina amiIsabel", machine.equals(result));
    }
    
    public void testAddAMIVNC() {
    	IsabelMachine machine = addIsabelMachine("machine1", SubType.amiVNC);
    	List<IsabelMachine> machines = IsabelMachineRegistry.getIsabelMachines();
    	IsabelMachine result = machines.get(0);
		assertTrue("No se ha guardado correctamente una maquina amiVNC", machine.equals(result));
    }
    
    private IsabelMachine addIsabelMachine(String hostname, SubType type) {
    	IsabelMachine machine = new IsabelMachine();
		machine.setHostname(hostname);
		machine.setSubType(type);
		IsabelMachineRegistry.addIsabelMachine(machine);
		return machine;
    }
    
}
