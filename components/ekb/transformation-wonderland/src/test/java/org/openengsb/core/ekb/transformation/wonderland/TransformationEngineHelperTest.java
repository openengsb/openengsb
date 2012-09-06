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

package org.openengsb.core.ekb.transformation.wonderland;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.transformation.wonderland.internal.TransformationEngineHelper;

public class TransformationEngineHelperTest extends TransformationEngineTests {
    private static TransformationEngineHelper helper;

    @BeforeClass
    public static void initiate() {
        helper = new TransformationEngineHelper(new TestModelRegistry());
    }

    @Test
    public void testIfSimplePropertyConnectionWorks_shouldWork() throws Exception {
        TransformationDescription description =
            new TransformationDescription(getModelADescription(), getModelBDescription());
        description.forwardField("idA", "idB");
        description.forwardField("idA", "testB");
        description.forwardField("testA", "testB");
        Map<String, Set<String>> result = helper.getPropertyConnections(description);
        assertThat(result.get("idA").contains("idB"), is(true));
        assertThat(result.get("idA").contains("testB"), is(true));
        assertThat(result.get("idA").size(), is(2));
        assertThat(result.get("testA").contains("testB"), is(true));
        assertThat(result.get("testA").size(), is(1));
    }

    @Test
    public void testIfComplexPropertyConnectionWorks_shouldWork() throws Exception {
        TransformationDescription description =
            new TransformationDescription(getModelADescription(), getModelBDescription());
        description.concatField("testB", "#", "idA", "blubA", "blaA");
        description.mapField("idA", "idB", new HashMap<String, String>());
        description.valueField("blubB", "42");
        description.concatField("blubB", ".", "testA", "blubA");
        Map<String, Set<String>> result = helper.getPropertyConnections(description);
        assertThat(result.get("idA").contains("idB"), is(true));
        assertThat(result.get("idA").contains("testB"), is(true));
        assertThat(result.get("idA").size(), is(2));
        assertThat(result.get("testA").contains("blubB"), is(true));
        assertThat(result.get("testA").size(), is(1));
        assertThat(result.get("blaA").contains("testB"), is(true));
        assertThat(result.get("blaA").size(), is(1));
        assertThat(result.get("blubA").contains("blubB"), is(true));
        assertThat(result.get("blubA").contains("testB"), is(true));
        assertThat(result.get("blubA").size(), is(2));
    }

    @Test
    public void testIfSimplePropertyConnectionWorksWithTemporaryFields_shouldWork() throws Exception {
        TransformationDescription description =
            new TransformationDescription(getModelADescription(), getModelBDescription());
        description.forwardField("idA", "temp.A");
        description.forwardField("temp.A", "temp.B");
        description.forwardField("temp.B", "temp.C");
        description.forwardField("temp.C", "temp.D");
        description.forwardField("temp.D", "temp.E");
        description.forwardField("temp.E", "temp.F");
        description.forwardField("temp.F", "idB");
        Map<String, Set<String>> result = helper.getPropertyConnections(description);
        
        assertThat(result.get("idA").contains("idB"), is(true));
        assertThat(result.get("idA").size(), is(1));
    }
    
    @Test
    public void testIfComplexPropertyConnectionWorksWithTemporaryFields_shouldWork() throws Exception {
        TransformationDescription description =
            new TransformationDescription(getModelADescription(), getModelBDescription());
        description.forwardField("idA", "temp.id");
        description.forwardField("temp.id", "temp.id+");
        description.forwardField("blubA", "temp.blub");
        description.concatField("temp.test", "#", "temp.id+", "temp.blub", "blaA");
        description.forwardField("temp.test", "testB");
        description.mapField("idA", "idB", new HashMap<String, String>());
        description.valueField("blubB", "42");
        description.concatField("blubB", ".", "testA", "temp.blub");
        Map<String, Set<String>> result = helper.getPropertyConnections(description);
        assertThat(result.get("idA").contains("idB"), is(true));
        assertThat(result.get("idA").contains("testB"), is(true));
        assertThat(result.get("idA").size(), is(2));
        assertThat(result.get("testA").contains("blubB"), is(true));
        assertThat(result.get("testA").size(), is(1));
        assertThat(result.get("blaA").contains("testB"), is(true));
        assertThat(result.get("blaA").size(), is(1));
        assertThat(result.get("blubA").contains("blubB"), is(true));
        assertThat(result.get("blubA").contains("testB"), is(true));
        assertThat(result.get("blubA").size(), is(2));
    }
}
