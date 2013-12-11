package org.openengsb.framework.vfs.configurationserviceapi.configurableservice;

import java.util.List;

/**
 * A ConfigurableService is a service that can be reconfigured using VFS. Every service that wants to be configured using VFS must implement this interface.
 */
public interface ConfigurableService {
    /**
     * Reconfigure the service.
     * @return True if the reconfiguration was successful, false if it failed.
     */
    boolean reconfigure();

    /**
     * Returns a list of the paths of the configurations that the service needs.
     */
    List<String> getPropertyList();
}
