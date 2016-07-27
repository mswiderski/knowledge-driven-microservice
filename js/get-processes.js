var eb = require("vertx/event_bus");
var console = require("vertx/console");
var vertx = require("vertx")

var eb = vertx.eventBus;

  
eb.registerHandler("jbpm-processes", function(message) {
  console.log('Reply ' + JSON.stringify(message, null, 2));
});

eb.publish('jbpm-endpoint', "");