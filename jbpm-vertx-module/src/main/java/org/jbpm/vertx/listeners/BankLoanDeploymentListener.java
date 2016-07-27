package org.jbpm.vertx.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;
import org.jbpm.services.api.ProcessService;
import org.jbpm.vertx.ServiceInvoker;
import org.jbpm.vertx.VerticleServiceProvider;
import org.jbpm.vertx.invoker.BasicLoanProcessInvoker;
import org.jbpm.vertx.invoker.LongTermLoanProcessInvoker;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class BankLoanDeploymentListener implements DeploymentEventListener {
	
	private Vertx vertx;
	private VerticleServiceProvider provider;
	private Map<String, Handler<Message<JsonObject>>> handlers = new ConcurrentHashMap<String, Handler<Message<JsonObject>>>();
	
	public BankLoanDeploymentListener(Vertx vertx, VerticleServiceProvider provider) {
		this.vertx = vertx;
		this.provider = provider;
	}

	@Override
	public void onDeploy(final DeploymentEvent event) {
		Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				
				Object result = null;
	
				ServiceInvoker invoker = new BasicLoanProcessInvoker(event.getDeploymentId(), (ProcessService) provider.getService("process"));
				result = invoker.invoke(message.body());

				if (result != null) {
					message.reply(result);
				}
				
			}
		}; 
		
		vertx.eventBus().registerHandler("BasicLoan", handler);
		handlers.put("BasicLoan", handler);
		
		Handler<Message<JsonObject>> handler2 = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                
                Object result = null;
    
                ServiceInvoker invoker = new LongTermLoanProcessInvoker(event.getDeploymentId(), (ProcessService) provider.getService("process"));
                result = invoker.invoke(message.body());

                if (result != null) {
                    message.reply(result);
                }
                
            }
        }; 
        
        vertx.eventBus().registerHandler("LongTermLoan", handler2);
        handlers.put("LongTermLoan", handler2);
	}

	@Override
	public void onUnDeploy(DeploymentEvent event) {
		Handler<Message<JsonObject>> handler = handlers.remove("BasicLoan");
		if (handler != null) {
			vertx.eventBus().unregisterHandler(event.getDeploymentId(), handler);
		}
		
		handler = handlers.remove("LongTermLoan");
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
