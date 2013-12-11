package org.openengsb.framework.vfs.vfsrepositoryhandler.servicelistener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.openengsb.framework.vfs.api.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.vfsrepositoryhandler.VFSRepositoryHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class ConfigurationServiceListener {

    private final Logger log = LoggerFactory.getLogger(ConfigurationServiceListener.class);
    private BundleContext context;
    private VFSRepositoryHandler vfsRepositoryHandler;
    private ServiceTracker<ConfigurationService, ConfigurationService> tracker;

    public ConfigurationServiceListener(BundleContext context, VFSRepositoryHandler vfsRepositoryHandler) {
        this.context = context;
        this.vfsRepositoryHandler = vfsRepositoryHandler;
    }

    public void open() {
        tracker = new ServiceTracker<ConfigurationService, ConfigurationService>(
                context, ConfigurationService.class, null) {
            @Override
            public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
                ConfigurationService service = context.getService(reference);
                register(service);
                context.ungetService(reference);
                return service;
            }

            @Override
            public void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
                //RepositoryHandler service = service;
                unregister(service);
            }
        };

        tracker.open();
    }

    public void register(ConfigurationService configurationService) {
        log.debug("add new ConfigurationService");
        vfsRepositoryHandler.registerConfigurationService(configurationService);
    }

    public void unregister(ConfigurationService configurationService) {
        log.debug("remove ConfigurationService");
        vfsRepositoryHandler.deregisterConfigurationService();
    }

    public void close() {
        tracker.close();
    }
}
