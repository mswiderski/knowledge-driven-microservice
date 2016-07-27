def eb = vertx.eventBus

def names = ['john', 'mary', 'paul', 'kris', 'mark', 'peter'];

eb.registerHandler("Groovy Loan Request", { message ->
  println "Received loan decision: ${message.body()}"
})

vertx.setPeriodic(2000) {

  def nameIndex = getRandomInt(0, 5);

  def json = [
  "name" : names.get((int)nameIndex),
  "requestId" : "Groovy Loan Request",
  "income" : getRandomInt(1, 10) * 1000.0,
  "amount" : getRandomInt(1, 10) * 1000.0,
  "lengthYears" : getRandomInt(1, 10),
  "approved" : false,
];

  eb.send("LoanApplication", json)
}

def getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
