package org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener;

import org.openengsb.framework.vfs.api.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice.VFSConfigurationService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author
 * Richard
 */
public class RepositoryHandlerListener {
    private final Logger log = LoggerFactory.getLogger(RepositoryHandlerListener.class);
    private BundleContext context;
    private VFSConfigurationService vfsConfigurationService;
    private ServiceTracker<RepositoryHandler, RepositoryHandler> tracker;

    public RepositoryHandlerListener(BundleContext bc, VFSConfigurationService vfsConfigurationService) {
        this.context = bc;
        this.vfsConfigurationService = vfsConfigurationService;
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
            public void modifiedService(ServiceReference<RepositoryHandler> reference, RepositoryHandler service) {
                register(service);
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
        vfsConfigurationService.registerRepositoryHandler(repositoryHandler);
    }

    public void unregister(RepositoryHandler repositoryHandler) {
        log.debug("remove RepositoryHandler");
        vfsConfigurationService.unregisterRepositoryHandler();
    }

    public void close() {
        tracker.close();
    }
}
