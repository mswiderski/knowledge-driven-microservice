package org.jbpm.vertx.handlers;

import org.jbpm.vertx.VerticleServiceProvider;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;


public class VertxSendTaskWorkItemHandler implements WorkItemHandler {
    
    private VerticleServiceProvider provider;
    
    public VertxSendTaskWorkItemHandler() {
        provider = VerticleServiceProvider.get();
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                
        String address = (String) workItem.getParameter("address");
        Object data = workItem.getParameter("data");
        
        provider.getContainer().logger().info("About to send data to " + address + " with content " + data);
        JsonObject message = new JsonObject(Json.encode(data));
//        message.putString("type", data.getClass().getName());
//        message.putString("data", data.toString());
        
        provider.getVertx().eventBus().send(address, message);
        provider.getContainer().logger().info("Message send via Vert.x event bus");
        
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        // no op

    }

}
