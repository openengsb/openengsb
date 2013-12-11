package org.openengsb.framework.vfs.configurationserviceapi.configurationservice;

import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;

/**
 * The Configuration Service is used to reconfigure services that want to be configured via VFS.
 * It needs to be implemented by the service that handles the reconfiguration of services.
 */
public interface ConfigurationService {
    /**
     * Notifies the configuration service that a new tag is available.
     * @param tag The available Tag.
     */
    void notifyAboutNewTag(Tag tag);
}
