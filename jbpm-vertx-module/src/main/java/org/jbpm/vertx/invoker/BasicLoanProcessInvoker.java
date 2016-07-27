package org.jbpm.vertx.invoker;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.services.api.ProcessService;
import org.jbpm.vertx.ServiceInvoker;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;


public class BasicLoanProcessInvoker implements ServiceInvoker {

    private ProcessService processService;
    private String deploymentId;

    public BasicLoanProcessInvoker(String deploymentId, ProcessService processService) {
        this.deploymentId = deploymentId;
        this.processService = processService;
    }
    
    @Override
    public Object invoke(JsonObject data) {
        Object loan = Json.decodeValue(data.toString(), org.jbpm.demo.bank.LoanApplication.class);
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("loan", loan);
        
        long piId = processService.startProcess(deploymentId, "bank-loan-process.BasicLoanProcess", parameters);
        
        return piId;
    }

}
