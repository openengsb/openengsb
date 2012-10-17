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

package org.openengsb.core.ekb.persistence.persist.edb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.common.EDBConverter;
import org.openengsb.core.ekb.persistence.persist.edb.internal.EngineeringObjectEnhancer;
import org.openengsb.core.ekb.persistence.persist.edb.models.EngineeringObjectModel;
import org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelA;
import org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelB;

public class EngineeringObjectEnhancerTest {
    private EngineeringObjectEnhancer enhancer;

    @Before
    public void setup() {
        EngineeringDatabaseService edbService = new TestEngineeringDatabaseService();
        EDBConverter edbConverter = new EDBConverter(edbService);
        TransformationEngine transformationEngine = new TestTransformationEngine();
        ModelRegistry modelRegistry = new TestModelRegistry();
        enhancer = new EngineeringObjectEnhancer(edbService, edbConverter, transformationEngine, modelRegistry);
    }

    @Test
    public void testIfEngineeringObjectModelInsertionWorks_shouldLoadTheValuesOfTheForeignKeys() throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setModelAId("test/test/objectA/reference/1");
        model.setModelBId("test/test/objectB/reference/1");
        EKBCommit commit = getTestCommit().addInsert(model);
        enhancer.enhanceEKBCommit(commit);

        assertThat(model.getNameA(), is("firstObject"));
        assertThat(model.getNameB(), is("secondObject"));
    }

    @Test
    public void testIfNormalObjectUpdateTriggersEOUpdate_shouldUpdateAlsoEO() throws Exception {
        SourceModelA model = new SourceModelA();
        model.setNameA("updatedFirstObject");
        model.setId("objectA/reference/1");
        EKBCommit commit = getTestCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        Object inserted = commit.getUpdates().get(commit.getUpdates().size() - 1);
        EngineeringObjectModel result = (EngineeringObjectModel) inserted;
        assertThat(before < after, is(true));
        assertThat(result.getNameA(), is("updatedFirstObject"));
    }

    @Test
    public void testIfDoubleNormalObjectUpdateTriggersEOUpdate_shouldUpdateAlsoEO() throws Exception {
        SourceModelA modelA = new SourceModelA();
        modelA.setNameA("updatedFirstObject");
        modelA.setId("objectA/reference/1");
        SourceModelB modelb = new SourceModelB();
        modelb.setNameB("updatedSecondObject");
        modelb.setId("objectB/reference/1");
        EKBCommit commit = getTestCommit().addUpdate(modelA).addUpdate(modelb);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        Object inserted = commit.getUpdates().get(commit.getUpdates().size() - 1);
        EngineeringObjectModel result = (EngineeringObjectModel) inserted;
        assertThat(before < after, is(true));
        assertThat(result.getNameA(), is("updatedFirstObject"));
        assertThat(result.getNameB(), is("updatedSecondObject"));
    }

    @Test(expected = EKBException.class)
    public void testIfTheEngineeringObjectUpdateIsCheckedCorrectly_shouldNotAllowReferenceAndValueChange()
        throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setInternalModelName("common/reference/1");
        model.setModelAId("test/test/objectA/reference/2");
        model.setNameA("teststring");
        EKBCommit commit = getTestCommit().addUpdate(model);
        enhancer.enhanceEKBCommit(commit);
    }

    @Test
    public void testIfTheEngineeringObjectReferenceUpdateWorks_shouldLoadOtherModelAndMergeIt() throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setInternalModelName("common/reference/1");
        model.setModelAId("test/test/objectA/reference/2");
        model.setModelBId("test/test/objectB/reference/1");
        model.setNameA("firstObject");
        model.setNameB("secondObject");
        EKBCommit commit = getTestCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        Object inserted = commit.getUpdates().get(commit.getUpdates().size() - 1);
        EngineeringObjectModel result = (EngineeringObjectModel) inserted;
        assertThat(before == after, is(true));
        assertThat(result.getNameA(), is("updatedFirstObject"));
        assertThat(result.getNameB(), is("secondObject"));
    }

    @Test
    public void testIfTheEngineeringObjectReferencesUpdateWorks_shouldLoadOtherModelAndMergeIt()
        throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setInternalModelName("common/reference/1");
        model.setModelAId("test/test/objectA/reference/2");
        model.setModelBId("test/test/objectB/reference/2");
        model.setNameA("firstObject");
        model.setNameB("secondObject");
        EKBCommit commit = getTestCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        Object inserted = commit.getUpdates().get(commit.getUpdates().size() - 1);
        EngineeringObjectModel result = (EngineeringObjectModel) inserted;
        assertThat(before == after, is(true));
        assertThat(result.getNameA(), is("updatedFirstObject"));
        assertThat(result.getNameB(), is("updatedSecondObject"));
    }

    @Test
    public void testIfEngineeringObjectUpdateAlsoUpdatesReferencedModels_shouldUpdateAllNeededModels()
        throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setInternalModelName("common/reference/1");
        model.setModelAId("test/test/objectA/reference/1");
        model.setModelBId("test/test/objectB/reference/1");
        model.setNameA("updatedFirstObject");
        model.setNameB("updatedSecondObject");
        EKBCommit commit = getTestCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        assertThat(after - before == 2, is(true));
        SourceModelA modelA = null;
        SourceModelB modelB = null;
        for (OpenEngSBModel update : commit.getUpdates()) {
            if (update.retrieveModelName().equals(SourceModelA.class.getName())) {
                modelA = (SourceModelA) update;
            }
            if (update.retrieveModelName().equals(SourceModelB.class.getName())) {
                modelB = (SourceModelB) update;
            }
        }
        assertThat(modelA.getNameA(), is("updatedFirstObject"));
        assertThat(modelB.getNameB(), is("updatedSecondObject"));
    }

    @Test
    public void testIfEOObjectWithNoReferenceCanBeCommitted_shouldWork() throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setInternalModelName("common/reference/1");
        EKBCommit commit = getTestCommit().addInsert(model);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        assertThat(after, is(before));
    }

    @Test
    public void testIfEngineeringObjectUpdateAlsoUpdatesReferencedModel_shouldUpdateNeededModel()
        throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setInternalModelName("common/reference/2");
        model.setModelAId("test/test/objectA/reference/1");
        model.setNameA("updatedFirstObject");
        EKBCommit commit = getTestCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        assertThat(after - before == 1, is(true));
        SourceModelA modelA = null;
        for (OpenEngSBModel update : commit.getUpdates()) {
            if (update.retrieveModelName().equals(SourceModelA.class.getName())) {
                modelA = (SourceModelA) update;
            }
        }
        assertThat(modelA.getNameA(), is("updatedFirstObject"));
    }

    private EKBCommit getTestCommit() {
        return new EKBCommit().setDomainId("test").setConnectorId("test");
    }
}
