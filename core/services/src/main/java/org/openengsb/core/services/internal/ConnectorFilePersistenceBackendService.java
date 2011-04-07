package org.openengsb.core.services.internal;

import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;

/**
 * Implementation of ConnectorConfiguration persistence backend
 */
// TODO: [OPENENGSB-1251] Provide file backend object for connector; dont forget blueprint and cfg file
public class ConnectorFilePersistenceBackendService implements ConfigPersistenceBackendService {

    @Override
    public List<ConfigItem<?>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        return null;
    }

    @Override
    public void persist(ConfigItem<?> config) throws PersistenceException, InvalidConfigurationException {

    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return false;
    }

}
