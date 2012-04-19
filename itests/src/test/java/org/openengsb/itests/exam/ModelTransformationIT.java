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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.ekb.TransformationEngine;
import org.openengsb.core.api.ekb.transformation.TransformationDescription;
import org.openengsb.core.common.transformations.TransformationUtils;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class ModelTransformationIT extends AbstractPreConfiguredExamTestHelper {
    private TransformationEngine transformationEngine;

    @Before
    public void setup() throws Exception {
        transformationEngine = getOsgiService(TransformationEngine.class);
    }

    @Test
    public void testIfServiceIsFound_shouldWork() throws Exception {
        assertThat(transformationEngine, notNullValue());
    }

    @Test
    public void testIfTransformationWorks_shouldWork() throws Exception {
        TransformationDescription description =
            new TransformationDescription(ExampleRequestModel.class, ExampleResponseModel.class);
        description.concatField("result", "-", "name", "id");
        transformationEngine.saveDescription(description);

        ExampleRequestModel modelA = ModelUtils.createEmptyModelObject(ExampleRequestModel.class);
        modelA.setName("test");
        modelA.setId(42);

        ExampleResponseModel modelB =
            transformationEngine.performTransformation(ExampleRequestModel.class, ExampleResponseModel.class, modelA);

        assertThat(modelB.getResult(), is("test-42"));
    }

    @Test
    public void testIfTransformationsFromFileWork_shouldWork() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("transformations/testDescription.xml");
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLInputStream(stream);
        transformationEngine.saveDescriptions(descriptions);

        ExampleResponseModel modelA = ModelUtils.createEmptyModelObject(ExampleResponseModel.class);
        modelA.setResult("test-42");

        ExampleRequestModel modelB =
            transformationEngine.performTransformation(ExampleResponseModel.class, ExampleRequestModel.class,
                modelA);

        assertThat(modelB.getName(), is("test"));
    }

}
