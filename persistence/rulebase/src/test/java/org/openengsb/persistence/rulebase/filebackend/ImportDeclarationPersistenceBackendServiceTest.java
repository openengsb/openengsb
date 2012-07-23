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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.core.workflow.model.GlobalConfiguration;
import org.openengsb.core.workflow.model.ImportConfiguration;
import org.openengsb.core.workflow.model.ImportDeclaration;

public class ImportDeclarationPersistenceBackendServiceTest extends AbstractOpenEngSBTest {

    private ImportDeclarationPersistenceBackendService service;
    private File storageFile;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        service = new ImportDeclarationPersistenceBackendService();
        storageFile = temporaryFolder.newFile("globals");
        service.setStorageFilePath(storageFile.getPath());
    }

    @Test
    public void testServiceSupportsConfigItem_shouldAcceptImportConfiguration() {
        assertTrue(service.supports(ImportConfiguration.class));
        assertFalse(service.supports(GlobalConfiguration.class));
    }

    @Test
    public void testPersistImportConfiguration_shouldPersistAndLoadConfig() {
        ImportConfiguration conf = new ImportConfiguration(new ImportDeclaration("java.util.List"));
        service.persist(conf);

        List<ConfigItem<ImportDeclaration>> loaded = service.load(conf.getMetaData());
        assertEquals(1, loaded.size());
        ImportConfiguration confLoaded = (ImportConfiguration) loaded.get(0);
        assertTrue(confLoaded.getContent().equals(conf.getContent()));
    }

    @Test
    public void testPersistImportConfiguration_shouldCreateFileIfNoneExists() throws IOException {
        FileUtils.forceDelete(storageFile);
        assertFalse(storageFile.exists());
        ImportConfiguration conf = new ImportConfiguration(new ImportDeclaration("java.util.List"));
        service.persist(conf);
        assertTrue(storageFile.exists());
    }

    @Test
    public void testLoadImportConfiguration_shouldOnlyLoadOneConfig() {
        ImportConfiguration conf1 = new ImportConfiguration(new ImportDeclaration("java.util.List"));
        ImportConfiguration conf2 = new ImportConfiguration(new ImportDeclaration("java.util.Map"));
        ImportConfiguration conf3 = new ImportConfiguration(new ImportDeclaration("java.util.Dictionary"));

        service.persist(conf1);
        service.persist(conf2);
        service.persist(conf3);

        List<ConfigItem<ImportDeclaration>> loaded = service.load(conf2.getMetaData());
        assertEquals(1, loaded.size());
        ImportConfiguration confLoaded = (ImportConfiguration) loaded.get(0);
        assertEquals("java.util.Map", confLoaded.getContent().getClassName());
    }

    @Test
    public void testLoadImportConfiguration_shouldLoadAllConfig() {
        ImportConfiguration conf1 = new ImportConfiguration(new ImportDeclaration("java.util.List"));
        ImportConfiguration conf2 = new ImportConfiguration(new ImportDeclaration("java.util.Map"));
        ImportConfiguration conf3 = new ImportConfiguration(new ImportDeclaration("java.util.Dictionary"));

        service.persist(conf1);
        service.persist(conf2);
        service.persist(conf3);

        List<ConfigItem<ImportDeclaration>> loaded = service.load(new HashMap<String, String>());
        assertEquals(3, loaded.size());
    }

    @Test
    public void testLoadImportConfigurationWithEmptyFile_shouldReturnEmptyList() throws IOException {
        FileUtils.forceDelete(storageFile);

        List<ConfigItem<ImportDeclaration>> loaded = service.load(new HashMap<String, String>());
        assertEquals(0, loaded.size());
    }

    @Test
    public void testRemoveImportConfiguration_ShouldDeleteConfiguration() {
        ImportConfiguration conf1 = new ImportConfiguration(new ImportDeclaration("java.util.List"));
        ImportConfiguration conf2 = new ImportConfiguration(new ImportDeclaration("java.util.Map"));

        service.persist(conf1);
        service.persist(conf2);

        service.remove(conf1.getMetaData());
        List<ConfigItem<ImportDeclaration>> loaded = service.load(new HashMap<String, String>());
        assertEquals(1, loaded.size());
        ImportConfiguration confLoaded = (ImportConfiguration) loaded.get(0);
        assertEquals("java.util.Map", confLoaded.getContent().getClassName());
    }

}
