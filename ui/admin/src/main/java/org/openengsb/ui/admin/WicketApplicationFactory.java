package org.openengsb.ui.admin;

import org.apache.wicket.protocol.http.WebApplication;
import org.ops4j.pax.wicket.api.ApplicationFactory;
import org.ops4j.pax.wicket.api.ApplicationLifecycleListener;

public class WicketApplicationFactory implements ApplicationFactory {

    @Override
    public WebApplication createWebApplication(ApplicationLifecycleListener lifecycleListener) {
        return new WicketApplication(lifecycleListener);
    }

}
