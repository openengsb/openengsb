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

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.apache.karaf.tooling.exam.options.configs.FeaturesCfg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.util.ModelUtils;
import org.openengsb.domain.example.model.EOModel;
import org.openengsb.domain.example.model.SourceModelA;
import org.openengsb.domain.example.model.SourceModelB;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

@RunWith(JUnit4TestRunner.class)
public class EngineeringObjectIT extends AbstractModelUsingExamTestHelper {
    private QueryInterface query;
    private PersistInterface persist;
    private ModelRegistry registry;
    private boolean initialized = false;
    private Version exampleDomainVersion = null;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[]{
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "url", "jdbc:h2:mem:itests"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "driverClassName", "org.h2.jdbcx.JdbcDataSource"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "username", ""),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "password", ""),
            mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject(),
            editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-connector-example")            
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        if (initialized) {
            return;
        }
        Bundle b = getInstalledBundle("org.openengsb.domain.example");
        exampleDomainVersion = b.getVersion();
        query = getOsgiService(QueryInterface.class);
        persist = getOsgiService(PersistInterface.class);
        registry = getOsgiService(ModelRegistry.class);
        TransformationEngine engine = getOsgiService(TransformationEngine.class);
        List<TransformationDescription> descriptions = generateTransformationDescriptions();
        engine.saveDescriptions(descriptions);
        initialized = true;
    }
    
    private ModelDescription getSourceModelADescription() {
        return new ModelDescription(SourceModelA.class, exampleDomainVersion);
    }
    
    private ModelDescription getSourceModelBDescription() {
        return new ModelDescription(SourceModelB.class, exampleDomainVersion);
    }
    
    private ModelDescription getEOModelDescription() {
        return new ModelDescription(EOModel.class, exampleDomainVersion);
    }

    private List<TransformationDescription> generateTransformationDescriptions() {
        List<TransformationDescription> descriptions = new ArrayList<TransformationDescription>();
        TransformationDescription desc =
            new TransformationDescription(getSourceModelADescription(), getEOModelDescription());
        desc.forwardField("name", "nameA");
        desc.setId("AtoEO");
        descriptions.add(desc);
        desc = new TransformationDescription(getSourceModelBDescription(), getEOModelDescription());
        desc.forwardField("name", "nameB");
        desc.setId("BtoEO");
        descriptions.add(desc);
        desc = new TransformationDescription(getEOModelDescription(), getSourceModelADescription());
        desc.forwardField("nameA", "name");
        desc.setId("EOtoA");
        descriptions.add(desc);
        desc = new TransformationDescription(getEOModelDescription(), getSourceModelBDescription());
        desc.forwardField("nameB", "name");
        desc.setId("EOtoB");
        descriptions.add(desc);
        return descriptions;
    }

    @Test
    public void testIfEOModelIsRecognizedAsEngineeringObject_shouldWork() throws Exception {
        Class<?> eo = registry.loadModel(getEOModelDescription());
        assertThat(ModelUtils.isEngineeringObjectClass(eo), is(true));
    }
    
    @Test
    public void testIfEngineeringObjectsAreInsertedCorrectly_shouldInsertObjectAndLoadReferencedValues()
        throws Exception {
        SourceModelA sourceA = new SourceModelA();
        sourceA.setEdbId("sourceA/1");
        sourceA.setName("sourceNameA");
        SourceModelB sourceB = new SourceModelB();
        sourceB.setEdbId("sourceB/1");
        sourceB.setName("sourceNameB");
        EKBCommit commit = getTestEKBCommit().addInsert(sourceA).addInsert(sourceB);
        persist.commit(commit);

        EOModel eo = new EOModel();
        eo.setEdbId("eo/1");
        eo.setRefModelA("testdomain/testconnector/sourceA/1");
        eo.setRefModelB("testdomain/testconnector/sourceB/1");
        commit = getTestEKBCommit().addInsert(eo);
        persist.commit(commit);

        EOModel result = query.getModel(EOModel.class, "testdomain/testconnector/eo/1");
        String nameA = result.getNameA();
        String nameB = result.getNameB();

        assertThat(nameA, is(sourceA.getName()));
        assertThat(nameB, is(sourceB.getName()));
    }

    @Test
    public void testIfEOUpdateWorksCorrectly_shouldUpdateSourceModel() throws Exception {
        SourceModelA sourceA = new SourceModelA();
        sourceA.setEdbId("sourceA/2");
        sourceA.setName("sourceNameA");
        SourceModelB sourceB = new SourceModelB();
        sourceB.setEdbId("sourceB/2");
        sourceB.setName("sourceNameB");
        EKBCommit commit = getTestEKBCommit().addInsert(sourceA).addInsert(sourceB);
        persist.commit(commit);
        
        EOModel eo = new EOModel();
        eo.setEdbId("eo/2");
        eo.setRefModelA("testdomain/testconnector/sourceA/2");
        eo.setRefModelB("testdomain/testconnector/sourceB/2");
        commit = getTestEKBCommit().addInsert(eo);
        persist.commit(commit);

        eo = query.getModel(EOModel.class, "testdomain/testconnector/eo/2");
        eo.setNameA("updatedNameA");
        commit = getTestEKBCommit().addUpdate(eo);
        persist.commit(commit);

        SourceModelA result = query.getModel(SourceModelA.class, "testdomain/testconnector/sourceA/2");
        assertThat(result.getName(), is("updatedNameA"));
    }
    
    @Test
    public void testIfSourceUpdateWorksCorrectly_shouldUpdateEngineeringObject() throws Exception {
        SourceModelA sourceA = new SourceModelA();
        sourceA.setEdbId("sourceA/3");
        sourceA.setName("sourceNameA");
        SourceModelB sourceB = new SourceModelB();
        sourceB.setEdbId("sourceB/3");
        sourceB.setName("sourceNameB");
        EKBCommit commit = getTestEKBCommit().addInsert(sourceA).addInsert(sourceB);
        persist.commit(commit);

        EOModel eo = new EOModel();
        eo.setEdbId("eo/3");
        eo.setRefModelA("testdomain/testconnector/sourceA/3");
        eo.setRefModelB("testdomain/testconnector/sourceB/3");
        commit = getTestEKBCommit().addInsert(eo);
        persist.commit(commit);
        
        SourceModelA source = query.getModel(SourceModelA.class, "testdomain/testconnector/sourceA/3");
        source.setName("updatedNameA");
        commit = getTestEKBCommit().addUpdate(source);
        persist.commit(commit);

        eo = query.getModel(EOModel.class, "testdomain/testconnector/eo/3");        
        assertThat(eo.getNameA(), is("updatedNameA"));
    }
    
    @Test
    public void testIfEOUpdateWorksCorrectlyWithOnlyOneSource_shouldUpdateSourceModel() throws Exception {
        SourceModelA sourceA = new SourceModelA();
        sourceA.setEdbId("sourceA/4");
        sourceA.setName("sourceNameA");
        EKBCommit commit = getTestEKBCommit().addInsert(sourceA);
        persist.commit(commit);
        
        EOModel eo = new EOModel();
        eo.setEdbId("eo/4");
        eo.setRefModelA("testdomain/testconnector/sourceA/4");
        commit = getTestEKBCommit().addInsert(eo);
        persist.commit(commit);

        eo = query.getModel(EOModel.class, "testdomain/testconnector/eo/4");
        eo.setNameA("updatedNameA");
        commit = getTestEKBCommit().addUpdate(eo);
        persist.commit(commit);

        SourceModelA result = query.getModel(SourceModelA.class, "testdomain/testconnector/sourceA/4");
        assertThat(result.getName(), is("updatedNameA"));
    }

    private EKBCommit getTestEKBCommit() {
        EKBCommit commit = new EKBCommit().setDomainId("testdomain").setConnectorId("testconnector");
        commit.setInstanceId("testinstance");
        return commit;
    }
}
