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
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.util.ModelUtils;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class EngineeringObjectIT extends AbstractModelUsingExamTestHelper {
    private QueryInterface query;
    private PersistInterface persist;
    private ModelRegistry registry;
    private boolean initialized = false;

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
            mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject()
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        if (initialized) {
            return;
        }
        query = getOsgiService(QueryInterface.class);
        persist = getOsgiService(PersistInterface.class);
        registry = getOsgiService(ModelRegistry.class);
        registerModelProvider();
        TransformationEngine engine = getOsgiService(TransformationEngine.class);
        List<TransformationDescription> descriptions = generateTransformationDescriptions();
        engine.saveDescriptions(descriptions);
        initialized = true;
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
        Object sourceA = getSourceModelA().newInstance();
        setProperty(sourceA, "setEdbId", "sourceA/1");
        setProperty(sourceA, "setName", "sourceNameA");
        Object sourceB = getSourceModelB().newInstance();
        setProperty(sourceB, "setEdbId", "sourceB/1");
        setProperty(sourceB, "setName", "sourceNameB");
        EKBCommit commit = getTestEKBCommit().addInsert(sourceA).addInsert(sourceB);
        persist.commit(commit);

        Object eo = getEOModel().newInstance();
        setProperty(eo, "setEdbId", "eo/1");
        setProperty(eo, "setRefModelA", "testdomain/testconnector/sourceA/1");
        setProperty(eo, "setRefModelB", "testdomain/testconnector/sourceB/1");
        commit = getTestEKBCommit().addInsert(eo);
        persist.commit(commit);

        Object result = query.getModel(getEOModel(), "testdomain/testconnector/eo/1");
        Object nameA = getProperty(result, "getNameA");
        Object nameB = getProperty(result, "getNameB");

        assertThat(nameA, is(getProperty(sourceA, "getName")));
        assertThat(nameB, is(getProperty(sourceB, "getName")));
    }

    @Test
    public void testIfEOUpdateWorksCorrectly_shouldUpdateSourceModel() throws Exception {
        Object sourceA = getSourceModelA().newInstance();
        setProperty(sourceA, "setEdbId", "sourceA/2");
        setProperty(sourceA, "setName", "sourceNameA");
        Object sourceB = getSourceModelB().newInstance();
        setProperty(sourceB, "setEdbId", "sourceB/2");
        setProperty(sourceB, "setName", "sourceNameB");
        EKBCommit commit = getTestEKBCommit().addInsert(sourceA).addInsert(sourceB);
        persist.commit(commit);

        Object eo = getEOModel().newInstance();
        setProperty(eo, "setEdbId", "eo/2");
        setProperty(eo, "setRefModelA", "testdomain/testconnector/sourceA/2");
        setProperty(eo, "setRefModelB", "testdomain/testconnector/sourceB/2");
        commit = getTestEKBCommit().addInsert(eo);
        persist.commit(commit);

        eo = query.getModel(getEOModel(), "testdomain/testconnector/eo/2");
        setProperty(eo, "setNameA", "updatedNameA");
        commit = getTestEKBCommit().addUpdate(eo);
        persist.commit(commit);

        Object result = query.getModel(getSourceModelA(), "testdomain/testconnector/sourceA/2");
        assertThat((String) getProperty(result, "getName"), is("updatedNameA"));
    }
    
    @Test
    public void testIfSourceUpdateWorksCorrectly_shouldUpdateEngineeringObject() throws Exception {
        Object sourceA = getSourceModelA().newInstance();
        setProperty(sourceA, "setEdbId", "sourceA/3");
        setProperty(sourceA, "setName", "sourceNameA");
        Object sourceB = getSourceModelB().newInstance();
        setProperty(sourceB, "setEdbId", "sourceB/3");
        setProperty(sourceB, "setName", "sourceNameB");
        EKBCommit commit = getTestEKBCommit().addInsert(sourceA).addInsert(sourceB);
        persist.commit(commit);

        Object eo = getEOModel().newInstance();
        setProperty(eo, "setEdbId", "eo/3");
        setProperty(eo, "setRefModelA", "testdomain/testconnector/sourceA/3");
        setProperty(eo, "setRefModelB", "testdomain/testconnector/sourceB/3");
        commit = getTestEKBCommit().addInsert(eo);
        persist.commit(commit);
        
        Object source = query.getModel(getSourceModelA(), "testdomain/testconnector/sourceA/3");
        setProperty(source, "setName", "updatedNameA");
        commit = getTestEKBCommit().addUpdate(source);
        persist.commit(commit);

        eo = query.getModel(getEOModel(), "testdomain/testconnector/eo/3");        
        assertThat((String) getProperty(eo, "getNameA"), is("updatedNameA"));
    }

    private EKBCommit getTestEKBCommit() {
        EKBCommit commit = new EKBCommit().setDomainId("testdomain").setConnectorId("testconnector");
        commit.setInstanceId("testinstance");
        return commit;
    }
}
