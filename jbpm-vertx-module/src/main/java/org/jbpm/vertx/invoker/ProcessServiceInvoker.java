package org.jbpm.vertx.invoker;

import java.util.Map;

import org.jbpm.services.api.ProcessService;
import org.jbpm.vertx.ServiceInvoker;
import org.vertx.java.core.json.JsonObject;

public class ProcessServiceInvoker implements ServiceInvoker {

	private static final String OPERATION = "operation";
	private static final String INST_ID = "instanceId";
	private static final String ID = "id";
	private static final String SIGNAL = "signal";
	private static final String DATA = "data";

	private ProcessService processService;
	private String deploymentId;

	public ProcessServiceInvoker(String deploymentId, ProcessService processService) {
		this.deploymentId = deploymentId;
		this.processService = processService;
	}

	@Override
	public Object invoke(JsonObject data) {
		String operation = data.getString(OPERATION);
		JsonObject result = new JsonObject();
		Long processInstanceId = data.getLong(INST_ID);
		result.putString("operation", operation);
		
		try {
			switch (operation) {
			case "start":
				
				String processId = data.getString(ID);
				
				Map<String, Object> parameters = data.getObject(DATA) == null?null:data.getObject(DATA).toMap();
				
				long piId = processService.startProcess(deploymentId, processId, parameters);
				
				result.putNumber("processInstanceId", piId);
				result.putString("outcome", "success");
				break;
		
			case "signal":
				
				String signalName = data.getString(SIGNAL);
				Object event = data.getObject(DATA);
				
				processService.signalProcessInstance(processInstanceId, signalName, event);
				result.putString("outcome", "success");
				break;
		
			case "abort":
				processService.abortProcessInstance(processInstanceId);
				result.putString("outcome", "success");
				break;
		
			default:
				break;
			}
		} catch (Exception e) {
			result.putString("outcome", "failure");
			result.putString("cause", e.getMessage());
		}
		return result;
	}

}
