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
import static org.hamcrest.CoreMatchers.notNullValue;
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
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.common.AdvancedModelWrapper;
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
        Option[] options =
            new Option[]{
                new KarafDistributionConfigurationFilePutOption("/etc/org.openengsb.infrastructure.jpa", "url",
                    "jdbc:h2:mem:itests"),
                new KarafDistributionConfigurationFilePutOption("/etc/org.openengsb.infrastructure.jpa",
                    "driverClassName", "org.h2.jdbcx.JdbcDataSource"),
                new KarafDistributionConfigurationFilePutOption("/etc/org.openengsb.infrastructure.jpa", "username", ""),
                new KarafDistributionConfigurationFilePutOption("/etc/org.openengsb.infrastructure.jpa", "password", ""),
                new KarafDistributionConfigurationFilePutOption("/etc/org.openengsb.ekb", "modelUpdatePropagationMode",
                    "FULLY_ACTIVATED"),
                mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject(),
                editConfigurationFileExtend(FeaturesCfg.BOOT, ",openengsb-connector-example") };
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
        ContextHolder.get().setCurrentContextId("testcontext");
    }

    private ModelDescription getSourceModelADescription() {
        return new ModelDescription(SourceModelA.class, exampleDomainVersion.toString());
    }

    private ModelDescription getSourceModelBDescription() {
        return new ModelDescription(SourceModelB.class, exampleDomainVersion.toString());
    }

    private ModelDescription getEOModelDescription() {
        return new ModelDescription(EOModel.class, exampleDomainVersion.toString());
    }

    private List<TransformationDescription> generateTransformationDescriptions() {
        List<TransformationDescription> descriptions = new ArrayList<TransformationDescription>();
        TransformationDescription desc =
            new TransformationDescription(getSourceModelADescription(), getEOModelDescription());
        desc.forwardField("name", "nameA");
        desc.forwardField("shared", "shared");
        desc.setId("AtoEO");
        descriptions.add(desc);
        desc = new TransformationDescription(getSourceModelBDescription(), getEOModelDescription());
        desc.forwardField("name", "nameB");
        desc.forwardField("shared", "shared");
        desc.setId("BtoEO");
        descriptions.add(desc);
        desc = new TransformationDescription(getEOModelDescription(), getSourceModelADescription());
        desc.forwardField("nameA", "name");
        desc.forwardField("shared", "shared");
        desc.setId("EOtoA");
        descriptions.add(desc);
        desc = new TransformationDescription(getEOModelDescription(), getSourceModelBDescription());
        desc.forwardField("nameB", "name");
        desc.forwardField("shared", "shared");
        desc.setId("EOtoB");
        descriptions.add(desc);
        return descriptions;
    }

    @Test
    public void testIfEOModelIsRecognizedAsEngineeringObject_shouldWork() throws Exception {
        Class<?> eo = registry.loadModel(getEOModelDescription());
        assertThat(AdvancedModelWrapper.isEngineeringObjectClass(eo), is(true));
    }

    @Test
    public void testIfEngineeringObjectsAreInsertedCorrectly_shouldInsertObjectAndLoadReferencedValues()
        throws Exception {
        SourceModelA sourceA = new SourceModelA("sourceA/1", "sourceNameA", "shared");
        SourceModelB sourceB = new SourceModelB("sourceB/1", "sourceNameB", "shared");
        persist.commit(getTestEKBCommit().addInsert(sourceA).addInsert(sourceB));

        EOModel eo = new EOModel("eo/1", sourceA.getEdbId(), sourceB.getEdbId(), "shared");
        persist.commit(getTestEKBCommit().addInsert(eo));

        EOModel result = query.getModel(EOModel.class, getModelOid(eo.getEdbId()));
        String nameA = result.getNameA();
        String nameB = result.getNameB();

        assertThat(nameA, is(sourceA.getName()));
        assertThat(nameB, is(sourceB.getName()));
    }

    @Test
    public void testIfEOUpdateWorksCorrectly_shouldUpdateSourceModel() throws Exception {
        SourceModelA sourceA = new SourceModelA("sourceA/2", "sourceNameA", "shared");
        SourceModelB sourceB = new SourceModelB("sourceB/2", "sourceNameB", "shared");
        persist.commit(getTestEKBCommit().addInsert(sourceA).addInsert(sourceB));

        EOModel eo = new EOModel("eo/2", sourceA.getEdbId(), sourceB.getEdbId(), "shared");
        persist.commit(getTestEKBCommit().addInsert(eo));

        eo = query.getModel(EOModel.class, getModelOid(eo.getEdbId()));
        eo.setNameA("updatedNameA");
        eo.setShared("updatedShared");
        persist.commit(getTestEKBCommit().addUpdate(eo));

        sourceA = query.getModel(SourceModelA.class, getModelOid(sourceA.getEdbId()));
        sourceB = query.getModel(SourceModelB.class, getModelOid(sourceB.getEdbId()));
        assertThat(sourceA.getName(), is("updatedNameA"));
        assertThat(sourceA.getShared(), is("updatedShared"));
        assertThat(sourceB.getShared(), is("updatedShared"));
    }

    @Test
    public void testIfSourceUpdateWorksCorrectly_shouldUpdateEngineeringObject() throws Exception {
        SourceModelA sourceA = new SourceModelA("sourceA/3", "sourceNameA", "shared");
        SourceModelB sourceB = new SourceModelB("sourceB/3", "sourceNameB", "shared");
        persist.commit(getTestEKBCommit().addInsert(sourceA).addInsert(sourceB));

        EOModel eo = new EOModel("eo/3", sourceA.getEdbId(), sourceB.getEdbId(), "shared");
        persist.commit(getTestEKBCommit().addInsert(eo));

        sourceA = query.getModel(SourceModelA.class, getModelOid(sourceA.getEdbId()));
        sourceA.setName("updatedNameA");
        sourceA.setShared("updatedShared");
        persist.commit(getTestEKBCommit().addUpdate(sourceA));

        eo = query.getModel(EOModel.class, getModelOid(eo.getEdbId()));
        sourceB = query.getModel(SourceModelB.class, getModelOid(sourceB.getEdbId()));
        assertThat(eo.getNameA(), is("updatedNameA"));
        assertThat(eo.getShared(), is("updatedShared"));
        assertThat(sourceB.getShared(), is("updatedShared"));
    }

    @Test
    public void testIfEOUpdateWorksCorrectlyWithOnlyOneSource_shouldUpdateSourceModel() throws Exception {
        SourceModelA sourceA = new SourceModelA("sourceA/4", "sourceNameA", "shared");
        persist.commit(getTestEKBCommit().addInsert(sourceA));

        EOModel eo = new EOModel("eo/4", sourceA.getEdbId(), null, "shared");
        persist.commit(getTestEKBCommit().addInsert(eo));

        eo = query.getModel(EOModel.class, getModelOid(eo.getEdbId()));
        eo.setNameA("updatedNameA");
        eo.setShared("updatedShared");
        persist.commit(getTestEKBCommit().addUpdate(eo));

        SourceModelA result = query.getModel(SourceModelA.class, getModelOid(sourceA.getEdbId()));
        assertThat(result.getName(), is("updatedNameA"));
        assertThat(result.getShared(), is("updatedShared"));
    }

    @Test
    public void testDeleteSourceModel_doesNotDeleteLinkedModels() {
        SourceModelA sourceA = new SourceModelA("sourceA/5", "sourceNameA", "shared");
        SourceModelB sourceB = new SourceModelB("sourceB/5", "sourceNameB", "shared");
        persist.commit(getTestEKBCommit().addInsert(sourceA).addInsert(sourceB));

        EOModel eo = new EOModel("eo/5", sourceA.getEdbId(), sourceB.getEdbId(), "shared");
        persist.commit(getTestEKBCommit().addInsert(eo));

        SourceModelA source = query.getModel(SourceModelA.class, getModelOid(sourceA.getEdbId()));
        persist.commit(getTestEKBCommit().addDelete(source));

        eo = query.getModel(EOModel.class, getModelOid(eo.getEdbId()));
        sourceB = query.getModel(SourceModelB.class, getModelOid(sourceB.getEdbId()));
        assertThat(eo, notNullValue());
        assertThat(sourceB, notNullValue());
    }

    @Test
    public void testDeleteEOModel_doesNotDeleteLinkedModels() {
        SourceModelA sourceA = new SourceModelA("sourceA/6", "sourceNameA", "shared");
        SourceModelB sourceB = new SourceModelB("sourceB/6", "sourceNameB", "shared");
        persist.commit(getTestEKBCommit().addInsert(sourceA).addInsert(sourceB));

        EOModel eo = new EOModel("eo/6", sourceA.getEdbId(), sourceB.getEdbId(), "shared");
        persist.commit(getTestEKBCommit().addInsert(eo));

        eo = query.getModel(EOModel.class, getModelOid(eo.getEdbId()));
        persist.commit(getTestEKBCommit().addDelete(eo));

        sourceA = query.getModel(SourceModelA.class, getModelOid(sourceA.getEdbId()));
        sourceB = query.getModel(SourceModelB.class, getModelOid(sourceB.getEdbId()));
        assertThat(sourceA, notNullValue());
        assertThat(sourceB, notNullValue());
    }

    @Test
    public void testInsertEOModel_doesNotInsertLinkedModels() {
        EOModel eo = new EOModel("eo/7", "sourceA/7", "sourceB/7", "shared");
        eo.setNameA("sourceNameA");
        eo.setNameB("sourceNameB");
        persist.commit(getTestEKBCommit().addInsert(eo));

        List<EOModel> eos = query.queryForModels(EOModel.class, "oid:\"" + getModelOid("eo/7") + "\"");
        List<SourceModelB> sourceBs =
            query.queryForModels(SourceModelB.class, "oid:\"" + getModelOid("sourceB/7") + "\"");
        List<SourceModelA> sourceAs =
            query.queryForModels(SourceModelA.class, "oid:\"" + getModelOid("sourceA/7") + "\"");

        assertThat(eos.size(), is(1));
        assertThat(sourceBs.size(), is(0));
        assertThat(sourceAs.size(), is(0));
    }

    @Test
    public void testInsertSourceModel_doesNotInsertLinkedModels() {
        SourceModelA sourceA = new SourceModelA("sourceA/8", "sourceNameA", "shared");
        persist.commit(getTestEKBCommit().addInsert(sourceA));

        List<EOModel> eos = query.queryForModels(EOModel.class, "oid:\"" + getModelOid("eo/8") + "\"");
        List<SourceModelB> sourceBs =
            query.queryForModels(SourceModelB.class, "oid:\"" + getModelOid("sourceB/8") + "\"");
        List<SourceModelA> sourceAs =
            query.queryForModels(SourceModelA.class, "oid:\"" + getModelOid("sourceA/8") + "\"");

        assertThat(eos.size(), is(0));
        assertThat(sourceBs.size(), is(0));
        assertThat(sourceAs.size(), is(1));
    }
}
