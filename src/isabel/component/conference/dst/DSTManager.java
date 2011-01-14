package isabel.component.conference.dst;

import isabel.component.conference.util.ConfigurationParser;
import isabel.lib.tasks.Task;
import isabel.lib.tasks.TaskListener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSTManager {
	/**
	 * Logs de la aplicacion
	 */
	protected static Logger log = LoggerFactory
			.getLogger(DSTManager.class);
	
	public Task addDns(String prefix, String value,
			TaskListener listener) throws IOException {
		return executeDST("ADD", prefix, value, listener);
	}
	
	public Task delDns(String prefix, TaskListener listener) throws IOException {
		return executeDST("DEL", prefix, "", listener);
	}
	
	private Task executeDST(String op, String prefix, String value, TaskListener listener) throws IOException {
		
		String scriptName = "dst/vncdns";
		
		String[] cmd = {scriptName, op, prefix, value};
		if (!ConfigurationParser.debug) {
			Task t = new Task(cmd, false);
			if (listener != null)
				t.addTaskListener(listener);
			t.start();
			return t;
		} else {
			return null;
		}
	}
}
