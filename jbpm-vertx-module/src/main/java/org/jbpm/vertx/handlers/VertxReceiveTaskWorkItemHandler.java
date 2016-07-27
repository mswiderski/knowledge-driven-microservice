package org.jbpm.vertx.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.vertx.VerticleServiceProvider;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.Cacheable;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;


public class VertxReceiveTaskWorkItemHandler implements WorkItemHandler, Cacheable {
    
    private VerticleServiceProvider provider;
    private Map<Long, Handler<Message<JsonObject>>> handlers = new ConcurrentHashMap<Long, Handler<Message<JsonObject>>>();
    
    private RuntimeManager runtimeManager;
    
    public VertxReceiveTaskWorkItemHandler(RuntimeManager runtimeManager) {
        provider = VerticleServiceProvider.get();
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        final String address = (String) workItem.getParameter("address");
        final Long processInstanceId = getProcessInstanceId(workItem);
        final Long workItemId = workItem.getId();
        
        Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                Map<String, Object> results = new HashMap<String, Object>();
                               
                provider.getContainer().logger().info("Message received via Vert.x event bus at address " 
                + address + " with content " + message.body().getString("data"));
                
                results.put("message", message.body().getString("data"));
                
                provider.getContainer().logger().info("Completing recive task for work item " + workItemId + " with result " + results);
                RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
                engine.getKieSession().getWorkItemManager().completeWorkItem(workItemId, results);
                
                Handler<Message<JsonObject>> handler = handlers.remove(workItemId);
                if (handler != null) {                    
                    provider.getVertx().eventBus().unregisterHandler(address, handler);
                }
            }
        }; 
        provider.getVertx().eventBus().registerHandler(address, handler);
        handlers.put(workItem.getId(), handler);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        Handler<Message<JsonObject>> handler = handlers.remove(workItem.getId());
        if (handler != null) {
            String address = (String) workItem.getParameter("address");
            provider.getVertx().eventBus().unregisterHandler(address, handler);
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }
    
    protected long getProcessInstanceId(WorkItem workItem) {
        return ((WorkItemImpl) workItem).getProcessInstanceId();
    }

}
