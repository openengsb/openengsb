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

package org.openengsb.persistence.context.filebackend;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ContextConfiguration;
import org.openengsb.core.api.model.ContextId;

public class ContextFilePersistenceServiceTest {

    private ContextFilePersistenceService persistenceService;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        temporaryFolder.newFile("context1.context");
        temporaryFolder.newFile("context2.context");
        temporaryFolder.newFile("context3.context");
        temporaryFolder.newFile("unknown.file");

        persistenceService = new ContextFilePersistenceService();
        persistenceService.setStorageFolderPath(temporaryFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void testLoad_shouldGetAllFiles() throws Exception {
        Map<String, String> metaData = Collections.emptyMap();

        List<ConfigItem<Map<String, String>>> items = persistenceService.load(metaData);

        assertThat(items.size(), is(3));
    }

    @Test
    public void testFilteredLoad_shouldReturnConfigurationWithCorrespindingMetaData() throws Exception {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put(ContextId.META_KEY_ID, "context2");

        List<ConfigItem<Map<String, String>>> items = persistenceService.load(metaData);

        assertThat(items.size(), is(1));
        ConfigItem<?> loadedConfiguration = items.get(0);
        assertThat(loadedConfiguration.getMetaData().get(ContextId.META_KEY_ID), is("context2"));
    }

    @Test
    public void testContextFilePersistenceService_shouldSupportContext() throws Exception {
        assertThat(persistenceService.supports(ContextConfiguration.class), is(true));
    }

    @Test
    public void testContextFilePersistenceService_shouldNotSupportUnknownConfigItemType() throws Exception {
        assertThat(persistenceService.supports(UnknownConfigItem.class), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPersistingUnknownItemType_shouldThrowException() throws Exception {
        persistenceService.persist(new UnknownConfigItem());
    }

    @Test
    public void testPersistingContext_shouldCreateNewFile() throws Exception {
        File configFileThatShouldBeCreated = new File(temporaryFolder.getRoot(), "contextFoo.context");
        Integer filesBefore = countFilesInTempFolder();

        persistenceService.persist(getEmptyContextConfigurationWithId("contextFoo"));
        Integer filesAfter = countFilesInTempFolder();

        assertThat(filesAfter, is(filesBefore + 1));
        assertThat(configFileThatShouldBeCreated.exists(), is(true));
    }

    @Test
    public void testRemovingConfiguration_shouldDeleteFile() throws Exception {
        File configFileThatShouldBeDeleted = new File(temporaryFolder.getRoot(), "context3.context");

        persistenceService.remove(getMetaDataWithContextId("context3"));
        assertThat(configFileThatShouldBeDeleted.exists(), is(false));
    }

    private int countFilesInTempFolder() {
        return temporaryFolder.getRoot().list().length;
    }

    private ContextConfiguration getEmptyContextConfigurationWithId(String contextId) {
        Map<String, String> metaData = getMetaDataWithContextId(contextId);
        return new ContextConfiguration(metaData, null);
    }

    private Map<String, String> getMetaDataWithContextId(String contextId) {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put(ContextId.META_KEY_ID, contextId);
        return metaData;
    }

    @SuppressWarnings("serial")
    private class UnknownConfigItem extends ConfigItem<Map<String, String>> {
    }
}
