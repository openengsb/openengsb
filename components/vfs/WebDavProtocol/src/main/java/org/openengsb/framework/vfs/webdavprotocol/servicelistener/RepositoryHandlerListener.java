package org.openengsb.framework.vfs.webdavprotocol.servicelistener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler.RepositoryHandler;
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
public class RepositoryHandlerListener {

    private final Logger log = LoggerFactory.getLogger(RepositoryHandlerListener.class);
    private BundleContext context;
    private WebDavHandler webDavHandler;
    private ServiceTracker<RepositoryHandler, RepositoryHandler> tracker;

    public RepositoryHandlerListener(BundleContext context, WebDavHandler webDavHandler) {
        this.context = context;
        this.webDavHandler = webDavHandler;
    }

    public void open() {
        tracker = new ServiceTracker<RepositoryHandler, RepositoryHandler>(context, RepositoryHandler.class, null) {
            @Override
            public RepositoryHandler addingService(ServiceReference<RepositoryHandler> reference) {

                RepositoryHandler service = context.getService(reference);
                register(service);
                context.ungetService(reference);
                return service;
            }

            @Override
            public void removedService(ServiceReference<RepositoryHandler> reference, RepositoryHandler service) {
                //RepositoryHandler service = service;
                unregister(service);
            }
        };

        tracker.open();
    }

    public void register(RepositoryHandler repositoryHandler) {
        log.debug("add new RepositoryHandler");
        webDavHandler.registerRepositoryHandler(repositoryHandler);
    }

    public void unregister(RepositoryHandler repositoryHandler) {
        log.debug("remove RepositoryHandler");
        webDavHandler.unregisterRepositoryHandler();
    }

    public void close() {
        tracker.close();
    }
}
