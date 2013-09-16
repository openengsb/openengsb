package org.openengsb.framework.vfs.configurationserviceapi.configurationservice;

import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;

public interface ConfigurationService {
    /**
     * Notifies the configuration service that a new tag is available.
     * @param tag The available Tag.
     */
    void newTag(Tag tag);
}
