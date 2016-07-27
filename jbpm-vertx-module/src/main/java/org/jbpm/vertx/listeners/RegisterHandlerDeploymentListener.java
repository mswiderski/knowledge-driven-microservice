package org.jbpm.vertx.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;
import org.jbpm.services.api.ProcessService;
import org.jbpm.vertx.ServiceInvoker;
import org.jbpm.vertx.VerticleServiceProvider;
import org.jbpm.vertx.invoker.ProcessServiceInvoker;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class RegisterHandlerDeploymentListener implements DeploymentEventListener {
	
	private Vertx vertx;
	private VerticleServiceProvider provider;
	private Map<String, Handler<Message<JsonObject>>> handlers = new ConcurrentHashMap<String, Handler<Message<JsonObject>>>();
	
	public RegisterHandlerDeploymentListener(Vertx vertx, VerticleServiceProvider provider) {
		this.vertx = vertx;
		this.provider = provider;
	}

	@Override
	public void onDeploy(final DeploymentEvent event) {
		Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				
				Object result = null;
				String service = message.body().getString("service");
				
				if (service.equals("process")) {
					
					ServiceInvoker invoker = new ProcessServiceInvoker(event.getDeploymentId(), (ProcessService) provider.getService(service));
					result = invoker.invoke(message.body());
				}
				
				if (result != null) {
					message.reply(result);
				}
				
			}
		}; 
		
		vertx.eventBus().registerHandler(event.getDeploymentId(), handler);
		handlers.put(event.getDeploymentId(), handler);
	}

	@Override
	public void onUnDeploy(DeploymentEvent event) {
		Handler<Message<JsonObject>> handler = handlers.remove(event.getDeploymentId());
		if (handler != null) {
			vertx.eventBus().unregisterHandler(event.getDeploymentId(), handler);
		}
	}

    @Override
    public void onActivate(DeploymentEvent event) {
        
    }

    @Override
    public void onDeactivate(DeploymentEvent event) {
        
    }

}
