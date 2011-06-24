package org.openengsb.ui.admin;

import org.apache.wicket.Request;
import org.openengsb.ui.common.OpenEngSBWebSession;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.springframework.security.authentication.AuthenticationManager;

@SuppressWarnings("serial")
public class AdminWebSession extends OpenEngSBWebSession {

    @PaxWicketBean(name="authenticationManager")
    private AuthenticationManager authenticationManager;
    
    public AdminWebSession(Request request) {
        super(request);
        injectDependencies();
        ensureDependenciesNotNull();
    }

    @Override
    protected AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
    
    

}
