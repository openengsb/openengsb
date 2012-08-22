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
package org.openengsb.persistence.rulebase.filebackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.core.workflow.drools.model.GlobalConfiguration;
import org.openengsb.core.workflow.drools.model.GlobalDeclaration;
import org.openengsb.core.workflow.drools.model.ImportConfiguration;

public class GlobalDeclarationPersistenceBackendServiceTest extends AbstractOpenEngSBTest {

    private GlobalDeclarationPersistenceBackendService service;
    private File storageFile;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        service = new GlobalDeclarationPersistenceBackendService();
        storageFile = temporaryFolder.newFile("globals");
        service.setStorageFilePath(storageFile.getPath());
    }

    @Test
    public void testServiceSupportsConfigItem_shouldAcceptGlobalConfiguration() throws Exception {
        assertTrue(service.supports(GlobalConfiguration.class));
        assertFalse(service.supports(ImportConfiguration.class));
    }

    @Test
    public void testPersistGlobalConfiguration_shouldPersistAndLoadConfig() throws Exception {
        GlobalConfiguration conf = new GlobalConfiguration(new GlobalDeclaration("java.util.List", "list"));
        service.persist(conf);

        List<ConfigItem<GlobalDeclaration>> loaded = service.load(conf.getMetaData());
        assertEquals(1, loaded.size());
        GlobalConfiguration confLoaded = (GlobalConfiguration) loaded.get(0);
        assertTrue(confLoaded.getContent().equals(conf.getContent()));
    }

    @Test
    public void testPersistGlobalConfiguration_shouldCreateFileIfNoneExists() throws Exception {
        FileUtils.forceDelete(storageFile);
        assertFalse(storageFile.exists());
        GlobalConfiguration conf = new GlobalConfiguration(new GlobalDeclaration("java.util.List", "list"));
        service.persist(conf);
        assertTrue(storageFile.exists());
    }

    @Test
    public void testPersistGlobalConfiguration_shouldUpdatePreviousConfiguration() throws Exception {
        GlobalConfiguration conf = new GlobalConfiguration(new GlobalDeclaration("java.util.List", "list"));
        service.persist(conf);

        conf.getContent().setClassName("java.util.ArrayList");
        service.persist(conf);

        List<ConfigItem<GlobalDeclaration>> loaded = service.load(conf.getMetaData());
        assertEquals(1, loaded.size());

        GlobalConfiguration confLoaded = (GlobalConfiguration) loaded.get(0);
        assertEquals("java.util.ArrayList", confLoaded.getContent().getClassName());
    }

    @Test
    public void testLoadGlobalConfiguration_shouldOnlyLoadOneConfig() throws Exception {
        GlobalConfiguration conf1 = new GlobalConfiguration(new GlobalDeclaration("java.util.List", "list"));
        GlobalConfiguration conf2 = new GlobalConfiguration(new GlobalDeclaration("java.util.Map", "map"));
        GlobalConfiguration conf3 = new GlobalConfiguration(new GlobalDeclaration("java.util.Dictionary", "dict"));

        service.persist(conf1);
        service.persist(conf2);
        service.persist(conf3);

        List<ConfigItem<GlobalDeclaration>> loaded = service.load(conf2.getMetaData());
        assertEquals(1, loaded.size());
        GlobalConfiguration confLoaded = (GlobalConfiguration) loaded.get(0);
        assertEquals("java.util.Map", confLoaded.getContent().getClassName());
        assertEquals("map", confLoaded.getContent().getVariableName());
    }

    @Test
    public void testLoadGlobalConfiguration_shouldLoadAllConfig() throws Exception {
        GlobalConfiguration conf1 = new GlobalConfiguration(new GlobalDeclaration("java.util.List", "list"));
        GlobalConfiguration conf2 = new GlobalConfiguration(new GlobalDeclaration("java.util.Map", "map"));
        GlobalConfiguration conf3 = new GlobalConfiguration(new GlobalDeclaration("java.util.Dictionary", "dict"));

        service.persist(conf1);
        service.persist(conf2);
        service.persist(conf3);

        List<ConfigItem<GlobalDeclaration>> loaded = service.load(new HashMap<String, String>());
        assertEquals(3, loaded.size());
    }

    @Test
    public void testLoadGlobalConfigurationWithEmptyFile_shouldReturnEmptyList() throws Exception {
        FileUtils.forceDelete(storageFile);

        List<ConfigItem<GlobalDeclaration>> loaded = service.load(new HashMap<String, String>());
        assertEquals(0, loaded.size());
    }

    @Test
    public void testRemoveGlobalConfiguration_ShouldDeleteConfiguration() throws Exception {
        GlobalConfiguration conf1 = new GlobalConfiguration(new GlobalDeclaration("java.util.List", "list"));
        GlobalConfiguration conf2 = new GlobalConfiguration(new GlobalDeclaration("java.util.Map", "map"));

        service.persist(conf1);
        service.persist(conf2);

        service.remove(conf1.getMetaData());
        List<ConfigItem<GlobalDeclaration>> loaded = service.load(new HashMap<String, String>());
        assertEquals(1, loaded.size());
        GlobalConfiguration confLoaded = (GlobalConfiguration) loaded.get(0);
        assertEquals("java.util.Map", confLoaded.getContent().getClassName());
        assertEquals("map", confLoaded.getContent().getVariableName());
    }
}
