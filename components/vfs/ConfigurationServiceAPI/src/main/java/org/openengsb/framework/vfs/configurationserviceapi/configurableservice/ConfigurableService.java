package org.openengsb.framework.vfs.configurationserviceapi.configurableservice;

import java.util.List;

public interface ConfigurableService {
    /**
     * Reconfigure the service.
     * @return True if the reconfiguration was successful, false if it failed.
     */
    boolean reconfigure();

    /**
     * Returns a list of the paths of the configurations that the service needs.
     * @return A list of the paths of the configurations that the service needs.
     */
    List<String> getPropertyList();
}
