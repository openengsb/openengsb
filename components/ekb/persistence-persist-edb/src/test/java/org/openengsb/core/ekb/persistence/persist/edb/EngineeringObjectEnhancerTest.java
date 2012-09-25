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
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.common.EDBConverter;
import org.openengsb.core.ekb.common.models.EngineeringObjectModel;
import org.openengsb.core.ekb.common.models.SourceModelA;
import org.openengsb.core.ekb.persistence.persist.edb.internal.EngineeringObjectEnhancer;

public class EngineeringObjectEnhancerTest {
    private EngineeringObjectEnhancer enhancer;

    @Before
    public void setup() {
        enhancer = new EngineeringObjectEnhancer();
        enhancer.setModelRegistry(new TestModelRegistry());
        enhancer.setTransformationEngine(new TestTransformationEngine());
        EngineeringDatabaseService edbService = new TestEngineeringDatabaseService();
        enhancer.setEdbService(edbService);
        enhancer.setEdbConverter(new EDBConverter(edbService));
    }

    @Test
    public void testIfEngineeringObjectModelInsertionWorks_shouldLoadTheValuesOfTheForeignKeys() throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setModelAId("objectA/reference/1");
        model.setModelBId("objectB/reference/1");
        EKBCommit commit = new EKBCommit().addInsert(model);
        enhancer.enhanceEKBCommit(commit);

        assertThat(model.getNameA(), is("firstObject"));
        assertThat(model.getNameB(), is("secondObject"));
    }
    
    @Test
    public void testIfNormalObjectUpdateTriggersEOUpdate_shouldUpdateAlsoEO() throws Exception {
        SourceModelA model = new SourceModelA();
        model.setNameA("updatedFirstObject");
        model.setId("objectA/reference/1");
        EKBCommit commit = new EKBCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.enhanceEKBCommit(commit);
        int after = commit.getUpdates().size();
        Object inserted = commit.getUpdates().get(commit.getUpdates().size()-1);
        EngineeringObjectModel result = (EngineeringObjectModel) inserted;
        assertThat(before < after, is(true));
        assertThat(result.getNameA(), is("updatedFirstObject"));
    }

}
