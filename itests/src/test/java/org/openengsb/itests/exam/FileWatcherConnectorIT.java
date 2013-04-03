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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.karaf.features.FeaturesService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.EventSupport;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.model.SourceModelA;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.domain.example.model.SourceModelB;
import org.openengsb.core.api.model.ModelDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class FileWatcherConnectorIT extends AbstractPreConfiguredExamTestHelper {
    Logger LOGGER = LoggerFactory.getLogger(FileWatcherConnectorIT.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Inject
    private FeaturesService featuresService;

    @Before
    public void setUp() throws Exception {
        featuresService.installFeature("openengsb-connector-filewatcher");
    }

    @Test
    public void testServiceDoesNotExist_shouldNotFindExampleDomain() throws Exception {
        File watchfile = tempFolder.newFile("testfile.csv");

        registerConnector(SourceModelA.class.getName(), watchfile);
        getOsgiService(ExampleDomain.class, 30000L);
        Thread.sleep(1500);
        FileUtils.write(watchfile, "\"foo\",\"bar\"");
        Thread.sleep(1500);
        QueryInterface queryInterface = getOsgiService(QueryInterface.class);
        List<SourceModelA> sourceModelAs = queryInterface.queryForActiveModels(SourceModelA.class);
        assertThat(sourceModelAs.size(), is(1));
        assertThat(sourceModelAs.get(0).getEdbId(), is("foo"));
        assertThat(sourceModelAs.get(0).getName(), is("bar"));
    }

    @Test
    public void testModelTransformationFromAtoB() throws Exception {
        TransformationEngine transformationEngine = getOsgiService(TransformationEngine.class);
        ModelDescription modelDescriptionA =
            new ModelDescription(SourceModelA.class, getExampleDomainVersion().toString());
        ModelDescription modelDescriptionB =
            new ModelDescription(SourceModelB.class, getExampleDomainVersion().toString());
        TransformationDescription transfDescription =
            new TransformationDescription(modelDescriptionA, modelDescriptionB);

        transfDescription.forwardField("edbId", "edbId");
        transfDescription.forwardField("name", "name");
        transformationEngine.saveDescription(transfDescription);

        File watchfile1 = tempFolder.newFile("file1.csv");
        File watchfile2 = tempFolder.newFile("file2.csv");

        registerConnector(SourceModelA.class.getName(), watchfile1);
        registerConnector(SourceModelB.class.getName(), watchfile2);

        getOsgiService(ExampleDomain.class, 30000L);
        Thread.sleep(5000);
        FileUtils.write(watchfile1, "foo,bar");
        Thread.sleep(2000);

        String fileContents = FileUtils.readFileToString(watchfile2).trim();

        assertThat(fileContents, is("foo,bar"));
    }

    private void registerConnector(String modelName, File watchfile) {
        ConnectorManager connectorManager = getOsgiService(ConnectorManager.class);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("watchfile", watchfile.getAbsolutePath());
        attributes.put("serializer", "org.openengsb.connector.virtual.filewatcher.internal.CSVParser");
        attributes.put("mixin.1", EventSupport.class.getName());
        attributes.put("modelType", modelName);
        Map<String, Object> properties = new HashMap<String, Object>();
        ConnectorDescription connectorDescription =
            new ConnectorDescription("example", "filewatcher", attributes, properties);
        connectorManager.create(connectorDescription);
    }

}
