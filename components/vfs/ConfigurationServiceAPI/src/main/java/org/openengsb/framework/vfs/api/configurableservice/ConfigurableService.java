package org.openengsb.framework.vfs.api.configurableservice;

import java.util.List;
import org.openengsb.framework.vfs.api.exceptions.ReconfigurationException;

/**
 * A ConfigurableService is a service that can be reconfigured using VFS. Every service that wants to be configured using VFS must implement this interface.
 */
public interface ConfigurableService {
    /**
     * Reconfigure the service.
	 * @throws  ReconfigurationException when an failure occurs during reconfiguration
	 * this exception will be thrown.
     */
    void reconfigure() throws ReconfigurationException;

    /**
     * Returns a list of the paths of the configurations that the service needs.
     */
    List<String> getPropertyList();
}
