package org.jbpm.vertx;

import org.vertx.java.core.json.JsonObject;

public interface ServiceInvoker {

	Object invoke(JsonObject data);
}
