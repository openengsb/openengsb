package org.openengsb.framework.vfs.webdavprotocol.servicelistener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.framework.vfs.webdavprotocol.webdavhandler.WebDavHandler;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class AuthenticationDomainTracker {

    private final Logger log = LoggerFactory.getLogger(AuthenticationDomainTracker.class);
    private BundleContext context;
    private WebDavHandler webDavHandler;
    private ServiceTracker<AuthenticationDomain, AuthenticationDomain> tracker;

    public AuthenticationDomainTracker(BundleContext context, WebDavHandler webDavHandler) {
        this.context = context;
        this.webDavHandler = webDavHandler;
    }

    public void open() {
        tracker = new ServiceTracker
                <AuthenticationDomain, AuthenticationDomain>(context, AuthenticationDomain.class, null) {
            @Override
            public AuthenticationDomain addingService(ServiceReference<AuthenticationDomain> reference) {

                AuthenticationDomain service = context.getService(reference);
                register(service);
                return service;
            }

            @Override
            public void removedService(ServiceReference<AuthenticationDomain> reference, AuthenticationDomain service) {
                unregister(service);
            }
        };
        tracker.open();
    }

    public void register(AuthenticationDomain authenticationDomain) {
        log.debug("add new AuthenticationDomain");
        webDavHandler.registerAuthenticationDomainService(authenticationDomain);
    }

    public void unregister(AuthenticationDomain authenticationDomain) {
        log.debug("remove HttpService");
        webDavHandler.unregisterAuthenticationDomainService();
    }

    public void close() {
        tracker.close();
    }
}
