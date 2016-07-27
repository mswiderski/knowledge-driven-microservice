package org.jbpm.vertx.security;

import java.util.Collections;
import java.util.List;

import org.kie.internal.identity.IdentityProvider;

public class VertxIdentityProvider  implements IdentityProvider {

    public String getName() {
        return "VertxUser";
    }

    public List<String> getRoles() {
        return Collections.emptyList();
    }

	@Override
	public boolean hasRole(String role) {
		return false;
	}

}
