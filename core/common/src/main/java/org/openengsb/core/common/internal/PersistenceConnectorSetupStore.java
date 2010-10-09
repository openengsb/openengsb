/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.common.connectorsetupstore.ConnectorSetupBean;
import org.openengsb.core.common.connectorsetupstore.ConnectorSetupStore;
import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceService;

public class PersistenceConnectorSetupStore implements ConnectorSetupStore {

    PersistenceService persistence;

    @Override
    public void storeConnectorSetup(String connector, String id, Map<String, String> properties) {
        try {
            deleteEntry(connector, id);
            ConnectorSetupBean setupBean = new ConnectorSetupBean(connector, id, properties);
            persistence.create(setupBean);
        } catch (PersistenceException pe) {
            throw new RuntimeException(pe);
        }
    }

    private void deleteEntry(String connector, String id) throws PersistenceException {
        ConnectorSetupBean example = new ConnectorSetupBean(connector, id, null);
        List<ConnectorSetupBean> storedForThisId = persistence.query(example);
        for (ConnectorSetupBean old : storedForThisId) {
            persistence.delete(old);
        }
    }

    private ConnectorSetupBean getEntry(String connector, String id) {
        ConnectorSetupBean example = new ConnectorSetupBean(connector, id, null);
        List<ConnectorSetupBean> storedForThisId = persistence.query(example);
        if (storedForThisId.isEmpty()) {
            return null;
        }
        return storedForThisId.get(0);
    }

    @Override
    public Map<String, String> loadConnectorSetup(String connector, String id) {
        ConnectorSetupBean entry = getEntry(connector, id);
        if (entry == null) {
            return new HashMap<String, String>();
        }
        return entry.getProperties();
    }

    @Override
    public Set<String> getStoredConnectors(String connector) {
        ConnectorSetupBean example = new ConnectorSetupBean(connector, null, null);
        List<ConnectorSetupBean> beans = persistence.query(example);
        Set<String> result = new HashSet<String>(beans.size());
        for (ConnectorSetupBean bean : beans) {
            result.add(bean.getId());
        }
        return result;
    }

    @Override
    public void deleteConnectorSetup(String connector, String id) {
        try {
            deleteEntry(connector, id);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPersistence(PersistenceService persistence) {
        this.persistence = persistence;
    }

}
