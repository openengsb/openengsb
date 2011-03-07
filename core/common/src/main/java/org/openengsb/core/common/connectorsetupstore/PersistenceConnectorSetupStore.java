/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.connectorsetupstore;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.common.BundleContextAware;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.osgi.framework.BundleContext;

public class PersistenceConnectorSetupStore implements ConnectorSetupStore, BundleContextAware {

    private PersistenceService persistence;

    private PersistenceManager persistenceManager;

    private BundleContext bundleContext;

    @Override
    public void storeConnectorSetup(ConnectorDomainPair connDomainPair, String id, Map<String, String> properties) {
        try {
            deleteEntry(connDomainPair, id);
            ConnectorSetupBean setupBean = new ConnectorSetupBean(connDomainPair, id, properties);
            persistence.create(setupBean);
        } catch (PersistenceException pe) {
            throw new RuntimeException(pe);
        }
    }

    private void deleteEntry(ConnectorDomainPair connectorDomainPair, String id) throws PersistenceException {
        ConnectorSetupBean example = new ConnectorSetupBean(connectorDomainPair, id, null);
        List<ConnectorSetupBean> storedForThisId = persistence.query(example);
        for (ConnectorSetupBean old : storedForThisId) {
            persistence.delete(old);
        }
    }

    private ConnectorSetupBean getEntry(ConnectorDomainPair connectorDomainPair, String id) {
        ConnectorSetupBean example = new ConnectorSetupBean(connectorDomainPair, id, null);
        List<ConnectorSetupBean> storedForThisId = persistence.query(example);
        if (storedForThisId.isEmpty()) {
            return null;
        }
        return storedForThisId.get(0);
    }

    @Override
    public Map<String, String> loadConnectorSetup(ConnectorDomainPair connectorDomainPair, String id) {
        ConnectorSetupBean entry = getEntry(connectorDomainPair, id);
        if (entry == null) {
            return null;
        }
        return entry.getProperties();
    }

    @Override
    public Set<String> getStoredConnectors(ConnectorDomainPair connectorDomainPair) {
        ConnectorSetupBean example = new ConnectorSetupBean(connectorDomainPair, null, null);
        List<ConnectorSetupBean> beans = persistence.query(example);
        Set<String> result = new HashSet<String>(beans.size());
        for (ConnectorSetupBean bean : beans) {
            result.add(bean.getId());
        }
        return result;
    }

    @Override
    public void deleteConnectorSetup(ConnectorDomainPair connectorDomainPair, String id) {
        try {
            deleteEntry(connectorDomainPair, id);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void init() {
        this.persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }
}
