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
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.core.workflow.drools.model.GlobalConfiguration;
import org.openengsb.core.workflow.drools.model.RuleBaseConfiguration;
import org.openengsb.core.workflow.drools.model.RuleBaseElement;

public class RuleBaseElementPersistenceBackendServiceTest extends AbstractOpenEngSBTest {

    private RuleBaseElementPersistenceBackendService service;
    private URLCodec encoder;
    private String separator = RuleBaseElementPersistenceBackendService.SEPARATOR;
    private File storageFolder;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        encoder = new URLCodec();
        service = new RuleBaseElementPersistenceBackendService();
        storageFolder = temporaryFolder.newFolder("rules");
        service.setStorageFolderPath(storageFolder.getPath());
        service.init();
    }

    @Test
    public void testServiceSupportsConfigItem_shouldAcceptImportConfiguration() {
        assertTrue(service.supports(RuleBaseConfiguration.class));
        assertFalse(service.supports(GlobalConfiguration.class));
    }

    @Test
    public void testPersistRuleBaseElement_ShouldCreateFileAndLoad() throws EncoderException, IOException {
        RuleBaseElement element = new RuleBaseElement();
        element.setCode("code");
        element.setName("name");
        element.setPackageName("package.org");
        element.setType(RuleBaseElementType.Rule);

        RuleBaseConfiguration conf = new RuleBaseConfiguration(element);
        service.persist(conf);

        String expectedFilename =
            String.format("%s%s%s%s%s", element.getType(), separator, element.getName(), separator
                , encoder.encode(element.getPackageName()));
        File expectedTarget = new File(storageFolder, expectedFilename);
        assertTrue(expectedTarget.exists());
        String code = FileUtils.readFileToString(expectedTarget);
        assertEquals("code", code);

        List<ConfigItem<RuleBaseElement>> loaded = service.load(conf.getMetaData());
        assertEquals(1, loaded.size());
        RuleBaseElement loadedElement = loaded.get(0).getContent();
        assertEquals(element.getName(), loadedElement.getName());
        assertEquals(element.getCode(), loadedElement.getCode());
        assertEquals(element.getPackageName(), loadedElement.getPackageName());
        assertEquals(element.getType(), loadedElement.getType());

    }

    @Test
    public void testPersistRuleBaseElement_ShouldUpdateElement() throws EncoderException, IOException {
        RuleBaseElement element = new RuleBaseElement();
        element.setCode("code");
        element.setName("name");
        element.setPackageName("package.org");
        element.setType(RuleBaseElementType.Rule);
        RuleBaseConfiguration conf = new RuleBaseConfiguration(element);
        service.persist(conf);

        conf.getContent().setCode("new code");
        service.persist(conf);

        List<ConfigItem<RuleBaseElement>> loaded = service.load(conf.getMetaData());
        assertEquals(1, loaded.size());
        RuleBaseElement loadedElement = loaded.get(0).getContent();
        assertEquals("new code", loadedElement.getCode());
    }

    @Test
    public void testLoadRuleConfiguration_ShouldFilterForType() {
        RuleBaseElement element = new RuleBaseElement();
        element.setCode("code");
        element.setName("name");
        element.setPackageName("package.org");
        element.setType(RuleBaseElementType.Rule);
        RuleBaseConfiguration conf1 = new RuleBaseConfiguration(element);
        service.persist(conf1);

        element.setPackageName("org.openengsb");
        RuleBaseConfiguration conf2 = new RuleBaseConfiguration(element);
        service.persist(conf2);

        element.setType(RuleBaseElementType.Process);
        RuleBaseConfiguration conf3 = new RuleBaseConfiguration(element);
        service.persist(conf3);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(RuleBaseElement.META_RULE_TYPE, RuleBaseElementType.Rule.toString());

        List<ConfigItem<RuleBaseElement>> loadedList = service.load(metadata);
        assertEquals(2, loadedList.size());
        RuleBaseConfiguration loaded1 = (RuleBaseConfiguration) loadedList.get(0);
        RuleBaseConfiguration loaded2 = (RuleBaseConfiguration) loadedList.get(1);
        assertEquals(RuleBaseElementType.Rule, loaded1.getContent().getType());
        assertEquals(RuleBaseElementType.Rule, loaded2.getContent().getType());
    }

    @Test
    public void testLoadRuleConfiguration_ShouldLoadAll() {
        RuleBaseElement element = new RuleBaseElement();
        element.setCode("code");
        element.setName("name");
        element.setPackageName("package.org");
        element.setType(RuleBaseElementType.Rule);
        RuleBaseConfiguration conf1 = new RuleBaseConfiguration(element);
        service.persist(conf1);

        element.setPackageName("org.openengsb");
        RuleBaseConfiguration conf2 = new RuleBaseConfiguration(element);
        service.persist(conf2);

        element.setType(RuleBaseElementType.Process);
        RuleBaseConfiguration conf3 = new RuleBaseConfiguration(element);
        service.persist(conf3);

        List<ConfigItem<RuleBaseElement>> loadedList = service.load(new HashMap<String, String>());
        assertEquals(3, loadedList.size());
    }

    @Test
    public void testRemoveRuleConfiguration_ShouldRemoveFile() throws EncoderException {
        RuleBaseElement element = new RuleBaseElement();
        element.setCode("code");
        element.setName("name");
        element.setPackageName("package.org");
        element.setType(RuleBaseElementType.Rule);

        RuleBaseConfiguration conf = new RuleBaseConfiguration(element);
        service.persist(conf);

        String expectedFilename =
            String.format("%s%s%s%s%s", element.getType(), separator, element.getName(), separator
                , encoder.encode(element.getPackageName()));
        File expectedTarget = new File(storageFolder, expectedFilename);
        assertTrue(expectedTarget.exists());
        service.remove(conf.getMetaData());
        assertFalse(expectedTarget.exists());

        assertEquals(0, service.load(conf.getMetaData()).size());
    }

    @Test
    public void testRemoveRuleConfiguration_ShouldRemoveForMetadata() {
        RuleBaseElement element = new RuleBaseElement();
        element.setCode("code");
        element.setName("name");
        element.setPackageName("package.org");
        element.setType(RuleBaseElementType.Rule);
        RuleBaseConfiguration conf1 = new RuleBaseConfiguration(element);
        service.persist(conf1);

        element.setPackageName("org.openengsb");
        RuleBaseConfiguration conf2 = new RuleBaseConfiguration(element);
        service.persist(conf2);

        element.setType(RuleBaseElementType.Process);
        RuleBaseConfiguration conf3 = new RuleBaseConfiguration(element);
        service.persist(conf3);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(RuleBaseElement.META_RULE_TYPE, RuleBaseElementType.Rule.toString());

        service.remove(metadata);

        List<ConfigItem<RuleBaseElement>> remainingList = service.load(new HashMap<String, String>());
        assertEquals(1, remainingList.size());
        RuleBaseConfiguration remainingElement = (RuleBaseConfiguration) remainingList.get(0);
        assertEquals(RuleBaseElementType.Process, remainingElement.getContent().getType());

    }

}
