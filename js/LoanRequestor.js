var eb = require("vertx/event_bus");
var console = require("vertx/console");
var vertx = require("vertx")

var eb = vertx.eventBus;

var names = ['john', 'mary', 'paul', 'kris', 'mark', 'peter'];

eb.registerHandler("JS Loan Request", function(message) {
  console.log('Received loan decision: ' + JSON.stringify(message));
});

vertx.setPeriodic(2000, function sendMessage() {
  var nameIndex = getRandomInt(0, 5);

  var jsonMsg = {
  "name" : names[nameIndex],
  "requestId" : "JS Loan Request",
  "income" : getRandomInt(1, 10) * 1000.0,
  "amount" : getRandomInt(1, 10) * 1000.0,
  "lengthYears" : getRandomInt(1, 10),
  "approved" : false,
  "explanantion" : null
};
  eb.send('LoanApplication', jsonMsg);
})

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}


