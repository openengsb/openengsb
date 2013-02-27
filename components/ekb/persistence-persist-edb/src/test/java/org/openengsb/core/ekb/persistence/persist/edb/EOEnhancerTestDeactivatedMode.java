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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.persistence.persist.edb.internal.EOMode;
import org.openengsb.core.ekb.persistence.persist.edb.models.EngineeringObjectModel;
import org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelA;

public class EOEnhancerTestDeactivatedMode extends AbstractEngineeringObjectEnhancerTest {
    
    public EOEnhancerTestDeactivatedMode() {
        super(EOMode.DEACTIVATED);
    }
    
    @Test
    public void testIfNormalObjectUpdateTriggersEOUpdate_shouldTriggerNothing() throws Exception {
        SourceModelA model = new SourceModelA();
        model.setNameA("updatedFirstObject");
        model.setId("objectA/reference/1");
        EKBCommit commit = getTestCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.onPreCommit(commit);
        int after = commit.getUpdates().size();
        assertThat(before, is(after));
    }
    
    @Test
    public void testIfEngineeringObjectUpdateAlsoUpdatesReferencedModel_shouldUpdateNothing()
        throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setInternalModelName("common/reference/2");
        model.setModelAId("objectA/reference/1");
        model.setNameA("updatedFirstObject");
        EKBCommit commit = getTestCommit().addUpdate(model);
        int before = commit.getUpdates().size();
        enhancer.onPreCommit(commit);
        int after = commit.getUpdates().size();
        SourceModelA modelA = null;
        for (OpenEngSBModel update : commit.getUpdates()) {
            if (update.retrieveModelName().equals(SourceModelA.class.getName())) {
                modelA = (SourceModelA) update;
            }
        }
        assertThat(before, is(after));
        assertThat(modelA, nullValue());
    }
}
