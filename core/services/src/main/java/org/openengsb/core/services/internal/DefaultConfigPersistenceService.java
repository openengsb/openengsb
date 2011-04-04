package org.openengsb.core.services.internal;

import java.util.Properties;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;

// TODO: implement
public class DefaultConfigPersistenceService implements ConfigPersistenceService {

    // injected by blueprint
    private ConfigPersistenceBackendService backend;

    @Override
    public <ConfigType> ConfigItem<ConfigType> load(Properties metadata) throws PersistenceException,
        InvalidConfigurationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <ConfigType> void persist(ConfigItem<ConfigType> configuration) throws PersistenceException,
        InvalidConfigurationException {
        // TODO Auto-generated method stub

    }

}
