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

package org.openengsb.core.common.connectorsetupstore;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceManager;
import org.openengsb.core.persistence.PersistenceService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class PersistenceConnectorSetupStoreTest {

    private PersistenceConnectorSetupStore store;
    private PersistenceService persistence;

    @Before
    public void setUp() throws PersistenceException {
        store = new PersistenceConnectorSetupStore();
        persistence = Mockito.mock(PersistenceService.class);
        PersistenceManager managerMock = Mockito.mock(PersistenceManager.class);
        Mockito.when(managerMock.getPersistenceForBundle(Mockito.any(Bundle.class))).thenReturn(persistence);
        store.setPersistenceManager(managerMock);
        store.setBundleContext(Mockito.mock(BundleContext.class));
        store.init();
        List<ConnectorSetupBean> result = new ArrayList<ConnectorSetupBean>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        ConnectorSetupBean setupBean = new ConnectorSetupBean("test", "42", map);
        ConnectorSetupBean setupBean2 = new ConnectorSetupBean("test", null, map);
        result.add(setupBean);
        Mockito.when(persistence.query(Mockito.refEq(setupBean, "properties"))).thenReturn(result);
        Mockito.when(persistence.query(Mockito.refEq(setupBean2, "properties"))).thenReturn(result);
    }

    @Test
    public void storeSetupNew_shouldWork() throws PersistenceException {
        store.storeConnectorSetup("test", "53", new HashMap<String, String>());
        ConnectorSetupBean setupBean = new ConnectorSetupBean("test", "53", new HashMap<String, String>());
        Mockito.verify(persistence).create(Mockito.refEq(setupBean, "properties"));
    }

    @Test
    public void storeSetupExsiting_shouldOverrideOldSetup() throws PersistenceException {
        store.storeConnectorSetup("test", "42", new HashMap<String, String>());
        ConnectorSetupBean setupBean = new ConnectorSetupBean("test", "42", new HashMap<String, String>());
        Mockito.verify(persistence).delete(Mockito.refEq(setupBean, "properties"));
        Mockito.verify(persistence).create(Mockito.refEq(setupBean, "properties"));
    }

    @Test
    public void deleteSetup_shouldWork() throws PersistenceException {
        store.deleteConnectorSetup("test", "42");
        ConnectorSetupBean setupBean = new ConnectorSetupBean("test", "42", new HashMap<String, String>());
        Mockito.verify(persistence).delete(Mockito.refEq(setupBean, "properties"));
    }

    @Test
    public void deleteSetupTwice_shouldIgnoreSecondCall() {
        store.deleteConnectorSetup("test", "42");
        store.deleteConnectorSetup("test", "42");
    }

    @Test
    public void loadSetupExisting_shouldWork() {
        Map<String, String> setup = store.loadConnectorSetup("test", "42");
        assertThat(setup.size(), is(1));
        assertThat(setup.get("foo"), is("bar"));
    }

    @Test
    public void loadSetupNonExisting_shouldReturnNull() {
        Map<String, String> setup = store.loadConnectorSetup("test", "43");
        assertThat(setup, nullValue());
    }

    @Test
    public void getStoredConnectors_shouldReturnAllConnectors() {
        Set<String> connectors = store.getStoredConnectors("test");
        assertThat(connectors.size(), is(1));
        assertThat(connectors.iterator().next(), is("42"));
    }

    @Test
    public void getStoredConnectorsNonStored_shouldReturnEmptySet() {
        Set<String> connectors = store.getStoredConnectors("foo");
        assertThat(connectors.isEmpty(), is(true));
    }

}
