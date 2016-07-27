package org.jbpm.vertx.invoker;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.services.api.ProcessService;
import org.jbpm.vertx.ServiceInvoker;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;


public class LoanRuleInvoker implements ServiceInvoker {

    private ProcessService processService;
    private String deploymentId;
    
    private KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    public LoanRuleInvoker(String deploymentId, ProcessService processService) {
        this.deploymentId = deploymentId;
        this.processService = processService;
    }
    
    @Override
    public Object invoke(JsonObject data) {
        
        try {
            Object loan = Json.decodeValue(data.toString(), org.jbpm.demo.bank.LoanApplication.class);
            List<Command<?>> commands = new ArrayList<>();
            commands.add(commandsFactory.newInsert(loan));
            commands.add(commandsFactory.newFireAllRules());
            BatchExecutionCommand cmd = commandsFactory.newBatchExecution(commands);
            
            processService.execute(deploymentId, cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }

}
