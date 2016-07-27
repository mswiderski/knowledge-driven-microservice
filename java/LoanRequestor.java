import java.util.Arrays;
import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class LoanRequestor extends Verticle {

    public void start() {
        final List<String> names = Arrays.asList(new String[] {"john", "mary", "paul", "kris", "mark", "peter"});

        vertx.eventBus().registerHandler("Java Loan Request",new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
              System.out.println("Received loan decision: " + message.body().toString());
            }
          });

        vertx.setPeriodic(2000, new Handler<Long>() {


            @Override
            public void handle(Long timerID) {
                int nameIndex = (int) getRandomInt(0, 5);

                String loanRequest = "{" +
                        "\"name\" : \""+names.get(nameIndex) + "\"," +
                        "\"requestId\" : \"Java Loan Request\"," +
                        "\"income\" : " + getRandomInt(1, 10) * 1000.0 + "," +
                        "\"amount\" : " + getRandomInt(1, 10) * 1000.0 + "," +
                        "\"lengthYears\" : " + getRandomInt(1, 10) +
                      "}";

                vertx.eventBus().send("LoanApplication", new JsonObject(loanRequest));
            }
        });
    }

    protected double getRandomInt(int min, int max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
}
