package org.jbpm.vertx;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.vertx.security.VertxIdentityProvider;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;
import org.vertx.java.platform.Container;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class VerticleServiceProvider {
    
    private static VerticleServiceProvider INSTANCE;
    
    public static VerticleServiceProvider get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("VerticleServiceProvider not yet configured");
        }
        return INSTANCE;
    }
    
    protected synchronized static VerticleServiceProvider configure(Vertx vertx, Container container, JsonObject config) {
        if (INSTANCE == null) {
            INSTANCE = new VerticleServiceProvider(vertx, container, config);
        }
        
        return INSTANCE;
    }

    private Vertx vertx;
    private Container container;
	private Map<String, Object> registry = new HashMap<String, Object>(); 
	
	private PoolingDataSource ds;
	
	   
    protected EntityManagerFactory emf;
    protected DeploymentService deploymentService;    
    protected DefinitionService bpmn2Service;
    protected RuntimeDataService runtimeDataService;
    protected ProcessService processService;
    protected UserTaskService userTaskService;
	
	private VerticleServiceProvider(Vertx vertx, Container container, JsonObject config) {
	    this.vertx = vertx;
	    this.container = container;

	    buildDatasource(config.getString("containerId"), config.getBoolean("managed"));
	    
	    configureServices(config.getString("containerId"), config.getBoolean("managed"));
	}
	
	public void addService(String type, Object service) {
		this.registry.put(type, service);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getService(String type) {
		if (!registry.containsKey(type)) {
			throw new IllegalArgumentException("No service " + type + " was found in registry");
		}
		return (T) registry.get(type);
		
	}

    public Vertx getVertx() {
        return vertx;
    }

    public Container getContainer() {
        return container;
    }
    
    public void send(String address, Object data) {
        getVertx().eventBus().send(address, new JsonObject(Json.encode(data)));
    }
    
    public void sendAsMap(String address, String dataName, Object data) {
        
        getVertx().eventBus().send(address, new JsonObject(Json.encode(data)));
    }
    
    protected void buildDatasource(String containerId, boolean managed) {
        
        TransactionManagerServices.getConfiguration()
        .setLogPart1Filename(containerId + "-btm1.tlog")
        .setLogPart2Filename(containerId + "-btm2.tlog")
        .setServerId(containerId + "@" + TransactionManagerServices.getConfiguration().getServerId());
        
        ds = new PoolingDataSource();
        ds.setUniqueName("jdbc/jbpm");

        if (!managed) {
            ds.setClassName("org.h2.jdbcx.JdbcDataSource");
            ds.setMaxPoolSize(15);
            ds.setAllowLocalTransactions(true);
            ds.getDriverProperties().put("user", "sa");
            ds.getDriverProperties().put("password", "sasa");
            ds.getDriverProperties().put("URL", "jdbc:h2:mem:mydb");
        } else {
            ds.setClassName("org.postgresql.xa.PGXADataSource");
            ds.setMaxPoolSize(15);
            ds.setAllowLocalTransactions(true);
            ds.getDriverProperties().put("user", "jbpm");
            ds.getDriverProperties().put("password", "jbpm");
            ds.getDriverProperties().put("serverName", "localhost");
            ds.getDriverProperties().put("portNumber", "5432");
            ds.getDriverProperties().put("databaseName", "demo");
        }
        ds.init();
    }
    
    protected void configureServices(String containerId, boolean managed) {
        
        Map<String, String> props = new HashMap<String, String>();
        if (managed) {
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        }
        emf = Persistence.createEntityManagerFactory("org.jbpm.domain", props);
        EntityManagerFactoryManager.get().addEntityManagerFactory("org.jbpm.domain", emf);
        System.setProperty("org.jbpm.ht.callback", "jaas");
        // build definition service
        bpmn2Service = new BPMN2DataServiceImpl(); 
        addService("definition", bpmn2Service);
        
        // build deployment service
        deploymentService = new KModuleDeploymentService();
        ((KModuleDeploymentService)deploymentService).setBpmn2Service(bpmn2Service);
        ((KModuleDeploymentService)deploymentService).setEmf(emf);
        ((KModuleDeploymentService)deploymentService).setIdentityProvider(new VertxIdentityProvider());
        ((KModuleDeploymentService)deploymentService).setManagerFactory(new RuntimeManagerFactoryImpl());
        ((KModuleDeploymentService)deploymentService).setFormManagerService(new FormManagerServiceImpl());
        addService("deployment", deploymentService);
        
        // build runtime data service
        runtimeDataService = new RuntimeDataServiceImpl();
        ((RuntimeDataServiceImpl) runtimeDataService).setCommandService(new TransactionalCommandService(emf));
        ((RuntimeDataServiceImpl) runtimeDataService).setIdentityProvider(new VertxIdentityProvider());
        ((RuntimeDataServiceImpl) runtimeDataService).setTaskService(HumanTaskServiceFactory.newTaskServiceConfigurator().entityManagerFactory(emf).getTaskService());
        ((KModuleDeploymentService)deploymentService).setRuntimeDataService(runtimeDataService);
        addService("runtime", runtimeDataService);
        
        // set runtime data service as listener on deployment service
        ((KModuleDeploymentService)deploymentService).addListener(((RuntimeDataServiceImpl) runtimeDataService));
        
        // build process service
        processService = new ProcessServiceImpl();
        ((ProcessServiceImpl) processService).setDataService(runtimeDataService);
        ((ProcessServiceImpl) processService).setDeploymentService(deploymentService);
        addService("process", processService);
        
        // build user task service
        userTaskService = new UserTaskServiceImpl();
        ((UserTaskServiceImpl) userTaskService).setDataService(runtimeDataService);
        ((UserTaskServiceImpl) userTaskService).setDeploymentService(deploymentService);
        addService("task", userTaskService);
        
    }

}
