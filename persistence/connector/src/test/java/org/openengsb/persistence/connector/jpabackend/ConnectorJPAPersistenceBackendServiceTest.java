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
package org.openengsb.persistence.connector.jpabackend;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;

public class ConnectorJPAPersistenceBackendServiceTest {

    private ConnectorJPAPersistenceBackendService service;
    private EntityManager em;

    @Before
    public void setUp() {
        service = new ConnectorJPAPersistenceBackendService();
        em = Persistence.createEntityManagerFactory("connector-test").createEntityManager();
        service.setEntityManager(em);
    }

    @Test
    public void testServiceSupportsConfigItem_shouldOnlyExceptConnectorConfiguration() {
        assertTrue(service.supports(ConnectorConfiguration.class));
    }

    @Test
    public void testServiceInsertConfigurationWithBasicType_shouldInsertAndLoadConfiguration()
        throws InvalidConfigurationException, PersistenceException {
        ConnectorId id1 = new ConnectorId("domain1", "connector1", "instance1");

        ConnectorDescription desc1 = new ConnectorDescription();
        desc1.getAttributes().put("attr1", "attr1");
        desc1.getProperties().put("the answer to live", 42);
        desc1.getProperties().put("basic", "value");
        ConnectorConfiguration conf1 = new ConnectorConfiguration(id1.toMetaData(), desc1);
        persist(conf1);

        List<ConfigItem<ConnectorDescription>> loaded = service.load(id1.toMetaData());
        assertThat(loaded.size(), is(1));
        ConnectorConfiguration conf = (ConnectorConfiguration) loaded.get(0);
        assertEquals(conf.getConnectorId(), id1);

        ConnectorDescription loadedDesc = conf.getContent();
        assertEquals(desc1.getAttributes(), loadedDesc.getAttributes());
        assertEquals(42, loadedDesc.getProperties().get("the answer to live"));
        assertEquals("value", loadedDesc.getProperties().get("basic"));
    }

    @Test
    public void testServiceInsertConfigurationWithArray_shouldInsertAndLoadConfiguration()
        throws InvalidConfigurationException, PersistenceException {
        ConnectorId id2 = new ConnectorId("domain2", "connector2", "instance2");

        ConnectorDescription desc2 = new ConnectorDescription();
        desc2.getAttributes().put("attr2", "attr2");
        desc2.getAttributes().put("foo", "bar");
        int[] property = new int[]{ 5, 10, 15, 25 };
        String[] property2 = new String[]{ "a", "b", "c" };
        desc2.getProperties().put("prop1", property);
        desc2.getProperties().put("prop2", property2);
        ConnectorConfiguration conf2 = new ConnectorConfiguration(id2.toMetaData(), desc2);
        persist(conf2);

        List<ConfigItem<ConnectorDescription>> loaded = service.load(id2.toMetaData());
        assertThat(loaded.size(), is(1));
        ConnectorConfiguration conf = (ConnectorConfiguration) loaded.get(0);
        assertEquals(conf.getConnectorId(), id2);

        ConnectorDescription loadedDesc = conf.getContent();
        assertEquals(desc2.getAttributes(), loadedDesc.getAttributes());
        assertTrue(Arrays.equals(property, (int[]) loadedDesc.getProperties().get("prop1")));
        assertTrue(Arrays.equals(property2, (String[]) loadedDesc.getProperties().get("prop2")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testServiceInsertConfigurationWithCollection_shouldInsertAndLoadConfigurations()
        throws InvalidConfigurationException, PersistenceException {

        ConnectorId id3 = new ConnectorId("domain3", "connector3", "instance3");

        ConnectorDescription desc3 = new ConnectorDescription();
        desc3.getAttributes().put("attr3", "attr3");
        Set<String> stringSet = new HashSet<String>();
        stringSet.add("foo");
        stringSet.add("bar");
        desc3.getProperties().put("prop1", stringSet);
        List<Integer> intList = new LinkedList<Integer>();
        intList.add(5);
        intList.add(33);
        desc3.getProperties().put("prop2", intList);
        ConnectorConfiguration conf3 = new ConnectorConfiguration(id3.toMetaData(), desc3);
        persist(conf3);

        List<ConfigItem<ConnectorDescription>> loaded = service.load(id3.toMetaData());
        assertThat(loaded.size(), is(1));
        ConnectorConfiguration conf = (ConnectorConfiguration) loaded.get(0);
        assertEquals(conf.getConnectorId(), id3);

        ConnectorDescription loadedDesc = conf.getContent();
        assertEquals(desc3.getAttributes(), loadedDesc.getAttributes());

        HashSet<String> loadedSet = (HashSet<String>) loadedDesc.getProperties().get("prop1");
        assertThat(loadedSet.size(), is(2));
        assertTrue(loadedSet.containsAll(stringSet));

        LinkedList<Integer> loadedList = (LinkedList<Integer>) loadedDesc.getProperties().get("prop2");
        assertThat(loadedList.size(), is(2));
        assertTrue(loadedList.equals(intList));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testServiceUpdateConfiguration_shouldUpdateConfiguration()
        throws InvalidConfigurationException, PersistenceException {

        ConnectorId id = new ConnectorId("domain", "connector", "instance");

        ConnectorDescription desc = new ConnectorDescription();
        desc.getAttributes().put("attr", "attr");
        Set<String> stringSet = new HashSet<String>();
        stringSet.add("foo");
        stringSet.add("bar");
        desc.getProperties().put("prop", stringSet);
        ConnectorConfiguration conf = new ConnectorConfiguration(id.toMetaData(), desc);
        persist(conf);

        List<Integer> intList = new ArrayList<Integer>();
        intList.add(42);
        desc.getProperties().put("prop", intList);
        persist(conf);

        List<ConfigItem<ConnectorDescription>> loaded = service.load(id.toMetaData());
        assertThat(loaded.size(), is(1));
        ConnectorConfiguration loadedConf = (ConnectorConfiguration) loaded.get(0);
        assertEquals(conf.getConnectorId(), id);

        ConnectorDescription loadedDesc = loadedConf.getContent();
        assertEquals(desc.getAttributes(), loadedDesc.getAttributes());
        assertThat(loadedDesc.getProperties().size(), is(1));
        ArrayList<Integer> loadedList = (ArrayList<Integer>) loadedDesc.getProperties().get("prop");
        assertThat(loadedList.size(), is(1));
        assertTrue(loadedList.equals(intList));
    }

    @Test
    public void testServiceRemoveConfiguration_shouldDeleteConfiguration() throws InvalidConfigurationException,
        PersistenceException {
        ConnectorId id = new ConnectorId("domain", "connector", "instance");

        ConnectorDescription desc = new ConnectorDescription();
        desc.getAttributes().put("attr", "attr");
        Set<String> stringSet = new HashSet<String>();
        stringSet.add("foo");
        stringSet.add("bar");
        desc.getProperties().put("prop", stringSet);
        ConnectorConfiguration conf = new ConnectorConfiguration(id.toMetaData(), desc);

        persist(conf);
        List<ConfigItem<ConnectorDescription>> loaded = service.load(id.toMetaData());
        assertThat(loaded.size(), is(1));

        remove(id);
        loaded = service.load(id.toMetaData());
        assertThat(loaded.size(), is(0));
    }

    @After
    public void tearDown() {
        em.close();
    }

    private void persist(ConnectorConfiguration conf) throws InvalidConfigurationException, PersistenceException {
        em.getTransaction().begin();
        service.persist(conf);
        em.getTransaction().commit();
    }

    private void remove(ConnectorId id) throws PersistenceException {
        em.getTransaction().begin();
        service.remove(id.toMetaData());
        em.getTransaction().commit();
    }
}
