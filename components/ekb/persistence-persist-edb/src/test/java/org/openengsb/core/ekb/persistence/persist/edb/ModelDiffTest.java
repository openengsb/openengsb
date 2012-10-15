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

import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.persistence.persist.edb.internal.ModelDiff;
import org.openengsb.core.ekb.persistence.persist.edb.internal.ModelDiffEntry;
import org.openengsb.core.ekb.persistence.persist.edb.models.EngineeringObjectModel;
import org.openengsb.core.ekb.persistence.persist.edb.models.TestModel;

public class ModelDiffTest {

    @Test
    public void testIfModelAgentIsSet_shouldWork() throws Exception {
        TestModel model = new TestModel();
        assertThat("TestModel isn't enhanced. Maybe you forgot to set the java agent?",
            model instanceof OpenEngSBModel, is(true));
    }

    @Test
    public void testIfDifferenceCalculationWorks_shouldReturnTheCorrectAmountOfChanges() throws Exception {
        EngineeringObjectModel old = new EngineeringObjectModel();
        old.setInternalModelName("testModel");
        old.setNameA("nameA-old");
        old.setNameB("nameB-old");
        old.setModelAId("testreference1");
        old.setModelBId("testreference2");
        old.setTest("this is a teststring");
        EngineeringObjectModel updated = new EngineeringObjectModel();
        updated.setInternalModelName("testModel");
        updated.setNameA("nameA-new");
        updated.setNameB("nameB-new");
        updated.setModelAId("testreference1_1");
        updated.setModelBId("testreference2_1");
        ModelDiff diff = ModelDiff.createModelDiff((OpenEngSBModel) old, (OpenEngSBModel) updated);
        // The difference is 5 since also the null value of the test property is calculated as change since it was
        // before not null
        assertThat(diff.getDifferences().size(), is(5));
    }

    @Test
    public void testIfDifferenceCalculationWorks_shouldReturnTheCorrectChangee() throws Exception {
        EngineeringObjectModel old = new EngineeringObjectModel();
        old.setInternalModelName("testModel");
        old.setNameA("nameA-old");
        old.setNameB("nameB-old");
        old.setModelAId("testreference1");
        old.setModelBId("testreference2");
        old.setTest("this is a teststring");
        EngineeringObjectModel updated = new EngineeringObjectModel();
        updated.setInternalModelName("testModel");
        updated.setNameA("nameA-old");
        updated.setNameB("nameB-old");
        updated.setModelAId("testreference1");
        updated.setModelBId("testreference2");
        updated.setTest("this is a new teststring");
        ModelDiff diff = ModelDiff.createModelDiff((OpenEngSBModel) old, (OpenEngSBModel) updated);
        assertThat(diff.getDifferences().size(), is(1));
        ModelDiffEntry entry = diff.getDifferences().values().iterator().next();
        assertThat((String) entry.getBefore(), is("this is a teststring"));
        assertThat((String) entry.getAfter(), is("this is a new teststring"));
    }

    @Test
    public void testIfForeignKeyCheckWorks_shouldSayThatAValueWasChanged() throws Exception {
        EngineeringObjectModel old = new EngineeringObjectModel();
        old.setInternalModelName("testModel");
        old.setModelAId("testreference1");
        old.setModelBId("testreference2");
        EngineeringObjectModel updated = new EngineeringObjectModel();
        updated.setInternalModelName("testModel");
        updated.setModelAId("testreference1_1");
        updated.setModelBId("testreference2");
        ModelDiff diff = ModelDiff.createModelDiff((OpenEngSBModel) old, (OpenEngSBModel) updated);
        assertThat(diff.getDifferences().size(), is(1));
        assertThat(diff.isForeignKeyChanged(), is(true));
        assertThat(diff.isValueChanged(), is(false));
    }

    @Test
    public void testIfValueChangedCheckWorks_shouldSayThatForeignKeyWasChanged() throws Exception {
        EngineeringObjectModel old = new EngineeringObjectModel();
        old.setInternalModelName("testModel");
        old.setNameA("hello");
        old.setModelAId("testreference1");
        old.setModelBId("testreference2");
        EngineeringObjectModel updated = new EngineeringObjectModel();
        updated.setInternalModelName("testModel");
        updated.setNameA("world");
        updated.setModelAId("testreference1");
        updated.setModelBId("testreference2");
        ModelDiff diff = ModelDiff.createModelDiff((OpenEngSBModel) old, (OpenEngSBModel) updated);
        assertThat(diff.getDifferences().size(), is(1));
        assertThat(diff.isForeignKeyChanged(), is(false));
        assertThat(diff.isValueChanged(), is(true));
    }
}
