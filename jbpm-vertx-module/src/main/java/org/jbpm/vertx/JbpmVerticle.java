package org.jbpm.vertx;

import java.util.Collection;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorImpl;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorManager;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorMerger;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.vertx.listeners.BankLoanDeploymentListener;
import org.jbpm.vertx.listeners.LoanRulesDeploymentListener;
import org.jbpm.vertx.listeners.RegisterHandlerDeploymentListener;
import org.kie.internal.query.QueryContext;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/*
 */
public class JbpmVerticle extends Verticle {

	

	
	protected RegisterHandlerDeploymentListener listener;
	protected VerticleServiceProvider provider;

	public void start() {
	    
        
	    JsonObject config = container.config();	    	    
	    final String containerToStart = config.getString("containerId");
	    
		if (containerToStart == null || containerToStart.isEmpty()) {
		    container.logger().error("No container info given, exiting");
		    container.exit();
		    return;
		}
		provider = VerticleServiceProvider.configure(vertx, container, config);
		listener = new RegisterHandlerDeploymentListener(vertx, provider);
		
		container.logger().info("Starting deployment of container " + containerToStart + " ... please wait");
		
		DeploymentService deploymentService = provider.getService("deployment");
        if (deploymentService instanceof ListenerSupport) {
            
            ((ListenerSupport) deploymentService).addListener(listener);
            
            if (containerToStart.indexOf("bank-loan-rules") != -1) {
                ((ListenerSupport) deploymentService).addListener(new LoanRulesDeploymentListener(vertx, provider));
                
            }
            if (containerToStart.indexOf("bank-loan-process") != -1) {
                ((ListenerSupport) deploymentService).addListener(new BankLoanDeploymentListener(vertx, provider));
                
            }
                
        }
		
		try {
		    String[] gav = containerToStart.split(":");
		    
			KModuleDeploymentUnit unit = new KModuleDeploymentUnit(gav[0], gav[1], gav[2]);
			unit.setStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);
			unit.setDeploymentDescriptor(getDescriptor());
			
			if (!deploymentService.isDeployed(containerToStart)){
			    deploymentService.deploy(unit);
			}
			
			container.logger().info("Container " + containerToStart + " deployed and running");
		} catch (Exception e) {
		    container.logger().error("Error when deploying container " + containerToStart, e);
		}
		
		vertx.eventBus().registerHandler("jbpm-endpoint",
				new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> message) {
						JsonObject data = new JsonObject();
						data.putString("containerId", containerToStart);
						RuntimeDataService runtimeDataService = provider.getService("runtime");
						Collection<ProcessDefinition> processes = runtimeDataService.getProcesses(new QueryContext(0, 100));
						JsonArray array = new JsonArray();
						for (ProcessDefinition process : processes) {
							array.addObject(new JsonObject("{\"" + process.getId() + "\":\"" + process.getName() + "\"}"));
						}
						data.putArray("processes", array);
						vertx.eventBus().publish("jbpm-processes", data);
					}
				});

		
		if (containerToStart.indexOf("rules") != -1) {
		    container.logger().info("Drools verticle started");            
        }
        if (containerToStart.indexOf("process") != -1) {
            container.logger().info("jBPM verticle started");            
        }
        
        container.logger().info("");
        container.logger().info("");
        container.logger().info("");
	}
	
	protected DeploymentDescriptor getDescriptor() {
	    DeploymentDescriptorManager descriptorManager = new DeploymentDescriptorManager("org.jbpm.domain");
	    
	    DeploymentDescriptor defaultDescriptor = descriptorManager.getDefaultDescriptor();
	    
	    DeploymentDescriptor descriptor = new DeploymentDescriptorImpl("org.jbpm.domain");
        
        descriptor.getBuilder()
        .addWorkItemHandler(new NamedObjectModel("mvel", "Send Task", "new org.jbpm.vertx.handlers.VertxSendTaskWorkItemHandler()"))
        .addWorkItemHandler(new NamedObjectModel("mvel", "Receive Task", "new org.jbpm.vertx.handlers.VertxReceiveTaskWorkItemHandler(runtimeManager)"));
	    
	    return new DeploymentDescriptorMerger().merge(defaultDescriptor, descriptor, MergeMode.MERGE_COLLECTIONS); 
	}

	@Override
	public void stop() {
		super.stop();
	}




	
	
}
