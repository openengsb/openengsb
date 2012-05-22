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

package org.openengsb.core.services.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.RuleConfiguration;
import org.openengsb.core.persistence.internal.CorePersistenceServiceBackend;
import org.openengsb.core.persistence.internal.DefaultPersistenceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class CorePersistenceServiceBackendTest {

    private CorePersistenceServiceBackend<String> corePersistenceServiceBackend;

    @Before
    public void setUp() throws Exception {
        File dbDirectory = new File("target/data");
        if (dbDirectory.exists()) {
            FileUtils.forceDelete(dbDirectory);
        }
        corePersistenceServiceBackend = setupCorePersistenceService();
    }

    @Test
    public void testQuery_shouldFindPersistedFile() throws Exception {
        HashMap<String, String> meta =
            createHashMap(new KeyValuePair("test1", "test1"), new KeyValuePair("test2", "test2"));
        RuleConfiguration ruleConfiguration = new RuleConfiguration(meta, "rule");
        corePersistenceServiceBackend.persist(ruleConfiguration);
        List<ConfigItem<String>> result = corePersistenceServiceBackend.load(meta);

        assertThat(result, notNullValue());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getContent().toString(), is("rule"));
        assertThat(result.get(0), instanceOf(RuleConfiguration.class));
    }

    @Test
    public void testRemove_shouldRemoveEntries() throws Exception {
        HashMap<String, String> meta1 =
            createHashMap(new KeyValuePair("test1", "test1"), new KeyValuePair("test2", "test2"));
        HashMap<String, String> meta2 = createHashMap(new KeyValuePair("test3", "test3"));
        RuleConfiguration ruleConfiguration = new RuleConfiguration(meta1, "rule");
        RuleConfiguration ruleConfiguration2 = new RuleConfiguration(meta2, "rule");
        corePersistenceServiceBackend.persist(ruleConfiguration);
        corePersistenceServiceBackend.persist(ruleConfiguration2);
        corePersistenceServiceBackend.remove(meta2);
        List<ConfigItem<String>> result = corePersistenceServiceBackend.load(meta1);

        assertThat(result, notNullValue());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getContent().toString(), is("rule"));
        assertThat(result.get(0), instanceOf(RuleConfiguration.class));
    }

    @Test
    public void testRemoveWithCommonEntry_shouldRemoveBothEntries() throws Exception {
        HashMap<String, String> meta1 =
            createHashMap(new KeyValuePair("test1", "test1"), new KeyValuePair("test2", "test2"));
        RuleConfiguration ruleConfiguration = new RuleConfiguration(meta1, "rule");
        HashMap<String, String> meta2 =
            createHashMap(new KeyValuePair("test1", "test1"), new KeyValuePair("test3", "test3"));
        RuleConfiguration ruleConfiguration2 = new RuleConfiguration(meta2, "rule");
        corePersistenceServiceBackend.persist(ruleConfiguration);
        corePersistenceServiceBackend.persist(ruleConfiguration2);
        HashMap<String, String> query = createHashMap(new KeyValuePair("test1", "test1"));
        corePersistenceServiceBackend.remove(query);
        List<ConfigItem<String>> result = corePersistenceServiceBackend.load(query);

        assertThat(result, notNullValue());
        assertThat(result.size(), is(0));
    }

    @Test
    public void testPersistTwice_shouldUpdateEntry() throws Exception {
        HashMap<String, String> meta =
            createHashMap(new KeyValuePair("test1", "test1"), new KeyValuePair("test2", "test2"));
        RuleConfiguration ruleConfiguration = new RuleConfiguration(meta, "rule");
        corePersistenceServiceBackend.persist(ruleConfiguration);
        ruleConfiguration.setContent("difference");
        corePersistenceServiceBackend.persist(ruleConfiguration);

        List<ConfigItem<String>> list = corePersistenceServiceBackend.load(meta);
        assertThat(list.size(), is(1));
        assertThat((RuleConfiguration) list.get(0), is(ruleConfiguration));
    }

    private HashMap<String, String> createHashMap(KeyValuePair... keyValuePairs) {
        HashMap<String, String> meta = new HashMap<String, String>();
        for (KeyValuePair keyValuePair : keyValuePairs) {
            meta.put(keyValuePair.key, keyValuePair.value);
        }
        return meta;
    }

    private CorePersistenceServiceBackend<String> setupCorePersistenceService() {
        Bundle bundleMock = mock(Bundle.class);
        when(bundleMock.getSymbolicName()).thenReturn("db");
        BundleContext bundleContextMock = mock(BundleContext.class);
        when(bundleContextMock.getBundle()).thenReturn(bundleMock);

        CorePersistenceServiceBackend<String> corePersistenceServiceBackend =
            new CorePersistenceServiceBackend<String>();
        DefaultPersistenceManager persistenceManager = new DefaultPersistenceManager();
        persistenceManager.setPersistenceRootDir("target/data");
        corePersistenceServiceBackend.setPersistenceManager(persistenceManager);
        corePersistenceServiceBackend.setBundleContext(bundleContextMock);
        corePersistenceServiceBackend.init();
        return corePersistenceServiceBackend;
    }

    @After
    public void tearDown() throws Exception {
        File dbDirectory = new File("target/data");
        if (!dbDirectory.exists()) {
            return;
        }
        FileUtils.forceDelete(dbDirectory);
    }

    private static class KeyValuePair {
        String key;
        String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }

}
