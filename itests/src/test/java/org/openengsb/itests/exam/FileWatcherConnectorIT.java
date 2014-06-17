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
import org.openengsb.core.ekb.api.EKBService;
import org.openengsb.core.ekb.api.SingleModelQuery;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.model.SourceModelA;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class FileWatcherConnectorIT extends AbstractPreConfiguredExamTestHelper {

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
        ConnectorManager connectorManager = getOsgiService(ConnectorManager.class);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("watchfile", watchfile.getAbsolutePath());
        attributes.put("serializer", "org.openengsb.connector.virtual.filewatcher.internal.CSVParser");
        attributes.put("mixin.1", EventSupport.class.getName());
        attributes.put("modelType", "org.openengsb.domain.example.model.SourceModelA");
        Map<String, Object> properties = new HashMap<String, Object>();
        ConnectorDescription connectorDescription = new ConnectorDescription("example", "filewatcher", attributes,
                properties);
        connectorManager.create(connectorDescription);
        getOsgiService(ExampleDomain.class, 30000L);
        Thread.sleep(1500);
        FileUtils.write(watchfile, "\"foo\",\"bar\"");
        Thread.sleep(1500);
        EKBService ekbService = getOsgiService(EKBService.class);
        List<SourceModelA> sourceModelAs = ekbService.query(new SingleModelQuery(SourceModelA.class));
        assertThat(sourceModelAs.size(), is(1));
        assertThat(sourceModelAs.get(0).getEdbId(), is("foo"));
        assertThat(sourceModelAs.get(0).getName(), is("bar"));
    }

}
