# knowledge-driven-microservice
Knowledge Driven Microservice - example how to employ jBPM, Drools and Vert.x to build responsive knowledge driven microservice


1. Build bank-data-model project (mvn clean install)
2. Build with maven jbpm-vertx-module (mvn clean install)
3. Copy jbpm-vertx-module/target/jbpm-vertx-module-1.0.0-mod.zip to op directory (where this readme file is)

4. Start rule service: 
vertx runzip jbpm-vertx-module-1.0.0-mod.zip -conf conf/loan-rules.conf -cluster -cluster-host localhost
5. Start process service:
vertx runzip jbpm-vertx-module-1.0.0-mod.zip -conf conf/loan-process.conf -cluster -cluster-host localhost
look at the conf file as it allows to run the process with same db as workbench for visualization purpose.

6. Run client apps to send sample requests
vertx run java/SingleLoanRequestor.java  -cluster -cluster-host localhost

vertx run js/LoanRequestor.js  -cluster -cluster-host localhost

vertx run java/LoanRequestor.java  -cluster -cluster-host localhost

vertx run groovy/LoanRequestor.groovy  -cluster -cluster-host localhost



