package org.openengsb.core.persistence.internal;

import java.util.Properties;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;

// TODO: Implement
public class CorePersistenceServiceBackend implements ConfigPersistenceBackendService {

    @Override
    public ConfigItem<?> load(Properties metadata) throws PersistenceException, InvalidConfigurationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void persist(ConfigItem<?> config) throws PersistenceException, InvalidConfigurationException {
        // TODO Auto-generated method stub

    }

}
