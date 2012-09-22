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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.transformations.TransformationUtils;
import org.openengsb.core.ekb.api.ModelGraph;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.transformation.wonderland.internal.TransformationEngineService;
import org.openengsb.core.ekb.transformation.wonderland.models.ModelA;
import org.openengsb.core.ekb.transformation.wonderland.models.ModelB;

public class TransformationEngineServiceTest extends TransformationEngineTests {
    private TransformationEngineService service;
    private ModelRegistry registry;
    private ModelGraph graph;

    @Before
    public void init() {
        service = new TransformationEngineService();
        graph = mock(ModelGraph.class);
        registry = new TestModelRegistry();
        service.setModelRegistry(registry);
        service.setGraphDb(graph);
        graph.addModel(getModelADescription());
        graph.addModel(getModelBDescription());
        graph.addModel(getModelCDescription());
    }

    private TransformationDescription getDescriptionForModelAToModelB() {
        return new TransformationDescription(getModelADescription(), getModelBDescription());
    }

    private TransformationDescription getDescriptionForModelBToModelA() {
        return new TransformationDescription(getModelBDescription(), getModelADescription());
    }

    private ModelB transformModelAToModelB(ModelA model) {
        return (ModelB) service.performTransformation(getModelADescription(), getModelBDescription(), model);
    }

    private ModelA transformModelBToModelA(ModelB model) {
        return (ModelA) service.performTransformation(getModelBDescription(), getModelADescription(), model);
    }

    @Test
    public void testIfTransformationDescriptionsBuiltCorrectly_shouldWork() throws Exception {
        TransformationDescription desc1 = getDescriptionForModelAToModelB();
        desc1.setId("aTob");
        desc1.forwardField("bla", "blub");

        TransformationDescription desc2 = getDescriptionForModelBToModelA();
        desc2.setId("bToa");

        assertThat(desc1.getId(), is("aTob"));
        assertThat(desc1.getTransformingSteps().size(), is(1));
        assertThat(desc2.getId(), is("bToa"));
        assertThat(desc2.getSourceModel(), is(getModelBDescription()));
        assertThat(desc2.getTargetModel(), is(getModelADescription()));
    }

    @Test
    public void testSimpleForwardTransformations_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.forwardField("blubA", "blubB");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = transformModelAToModelB(model);

        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3"));
    }

    @Test
    public void testForwardTransformationsWithTemporaryField_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.forwardField("blubA", "temp.test");
        desc.forwardField("temp.test", "blubB");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = transformModelAToModelB(model);

        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3"));
    }

    @Test
    public void testMixedForwardTransformations_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.forwardField("idA", "testB");
        desc.forwardField("testA", "blubB");
        desc.forwardField("blubA", "idB");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getIdB(), is("test3"));
        assertThat(result.getTestB(), is("test1"));
        assertThat(result.getBlubB(), is("test2"));
    }

    @Test
    public void testConcatTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.concatField("blubB", "#", "blubA", "blaA");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");
        model.setBlaA("test4");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3#test4"));
    }

    @Test
    public void testSplitTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelBToModelA();
        desc.forwardField("idB", "idA");
        desc.forwardField("testB", "testA");
        desc.splitField("blubB", "blubA", "#", "0");
        desc.splitField("blubB", "blaA", "#", "1");
        installTransformation(desc);

        ModelB model = new ModelB();
        model.setIdB("test1");
        model.setTestB("test2");
        model.setBlubB("test3#test4");

        ModelA result = transformModelBToModelA(model);
        assertThat(result.getIdA(), is("test1"));
        assertThat(result.getTestA(), is("test2"));
        assertThat(result.getBlubA(), is("test3"));
        assertThat(result.getBlaA(), is("test4"));
    }

    @Test
    public void testSplitRegexTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelBToModelA();
        desc.splitRegexField("blubB", "blubA", "[^#]+", "0");
        desc.splitRegexField("blubB", "blaA", "[^#]+", "1");
        installTransformation(desc);

        ModelB model = new ModelB();
        model.setBlubB("test3#test4");

        ModelA result = transformModelBToModelA(model);
        assertThat(result.getBlubA(), is("test3"));
        assertThat(result.getBlaA(), is("test4"));
    }

    @Test
    public void testMapTransformation_shoulWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelBToModelA();
        desc.forwardField("idB", "idA");
        desc.forwardField("testB", "testA");
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("hello", "world");
        mapping.put("dog", "cat");
        desc.mapField("blubB", "blubA", mapping);
        installTransformation(desc);

        ModelB model1 = new ModelB();
        model1.setIdB("test1");
        model1.setTestB("test2");
        model1.setBlubB("hello");

        ModelB model2 = new ModelB();
        model2.setIdB("test1");
        model2.setTestB("test2");
        model2.setBlubB("dog");

        ModelA result1 = transformModelBToModelA(model1);
        ModelA result2 = transformModelBToModelA(model2);

        assertThat(result1.getBlubA(), is("world"));
        assertThat(result2.getBlubA(), is("cat"));
    }

    @Test
    public void testSubStringTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.substringField("idA", "idB", "0", "4");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("this is a test");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getIdB(), is("this"));
    }

    @Test
    public void testValueTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.forwardField("idA", "idB");
        desc.valueField("testB", "blub");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("this is a test");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getIdB(), is("this is a test"));
        assertThat(result.getTestB(), is("blub"));
    }

    @Test
    public void testLengthTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelBToModelA();
        desc.lengthField("testB", "testA", null);
        desc.lengthField("elements", "blubA", "size");
        installTransformation(desc);

        ModelB model = new ModelB();
        model.setTestB("Hello");
        List<String> elements = new ArrayList<String>();
        elements.add("A");
        elements.add("B");
        elements.add("C");
        model.setElements(elements);

        ModelA result = transformModelBToModelA(model);
        assertThat(result.getTestA(), is(model.getTestB().length() + ""));
        assertThat(result.getBlubA(), is(model.getElements().size() + ""));
    }

    @Test
    public void testTrimTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelBToModelA();
        desc.trimField("testB", "testA");
        installTransformation(desc);

        ModelB model = new ModelB();
        model.setTestB("       Hello      ");

        ModelA result = transformModelBToModelA(model);
        assertThat(result.getTestA(), is("Hello"));
    }

    @Test
    public void testToUpperTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.toUpperField("testA", "testB");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setTestA("hello");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getTestB(), is("HELLO"));
    }

    @Test
    public void testToLowerTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.toLowerField("testA", "testB");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setTestA("HELLO");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getTestB(), is("hello"));
    }

    @Test
    public void testReplaceTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.replaceField("testA", "testB", "test", "blub");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setTestA("testHellotest");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getTestB(), is("blubHelloblub"));
    }

    @Test
    public void testReverseTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.reverseField("testA", "testB");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setTestA("This is a teststring");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getTestB(), is("gnirtstset a si sihT"));
    }

    @Test
    public void testPadTransformation_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.padField("idA", "idB", "4", "0", "Start");
        desc.padField("testA", "testB", "7", "!", "End");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("1");
        model.setTestA("works?");

        ModelB result = transformModelAToModelB(model);
        assertThat(result.getIdB(), is("0001"));
        assertThat(result.getTestB(), is("works?!"));
    }

    @Test
    public void testRemoveLeadingText_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelBToModelA();
        desc.removeLeadingField("idB", "idA", "[#?]+", "3");
        desc.removeLeadingField("testB", "testA", "#+", "2");
        desc.removeLeadingField("blubB", "blubA", "[#?]+", "0");
        desc.removeLeadingField("blubB", "blaA", "#+", "3");
        installTransformation(desc);

        ModelB model = new ModelB();
        model.setIdB("#?##blub");
        model.setTestB("##blub");
        model.setBlubB("#?#?#?test");

        ModelA result = transformModelBToModelA(model);

        assertThat(result.getIdA(), is("#blub"));
        assertThat(result.getTestA(), is("blub"));
        assertThat(result.getBlubA(), is("test"));
        assertThat(result.getBlaA(), is("?#?#?test"));
    }

    @Test
    public void testInstantiate_shouldWork() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.instantiateField("idA", "intValue", Integer.class.getName(), "parseInt");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("42");

        ModelB result = transformModelAToModelB(model);

        assertThat(result.getIntValue(), is(42));
    }

    @Test
    public void testIfMergeIsWorking_shouldNotChangeUnusedField() throws Exception {
        TransformationDescription desc = getDescriptionForModelAToModelB();
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.forwardField("blubA", "temp.test");
        desc.forwardField("temp.test", "blubB");
        installTransformation(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = new ModelB();
        result.setUnused("this is a test");

        result = (ModelB) service.performTransformation(getModelADescription(), getModelBDescription(),
            model, result);

        assertThat(result.getUnused(), is("this is a test"));
    }

    @Test
    public void testReadTransformationMetaDataFromFile_shouldWork() throws Exception {
        File descriptionFile =
            new File(getClass().getClassLoader().getResource("testDescription.transformation").getFile());
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLFile(descriptionFile);
        assertThat(descriptions.get(0).getId(), is("transformModelAToModelB_1"));
        assertThat(descriptions.get(0).getSourceModel(), is(getModelADescription()));
        assertThat(descriptions.get(0).getTargetModel(), is(getModelBDescription()));
        assertThat(descriptions.get(1).getId(), is("transformModelBToModelA_1"));
        assertThat(descriptions.get(1).getSourceModel(), is(getModelBDescription()));
        assertThat(descriptions.get(1).getTargetModel(), is(getModelADescription()));
    }

    @Test
    public void testRetrievedTransformationsFromFile1_shouldWork() throws Exception {
        File descriptionFile =
            new File(getClass().getClassLoader().getResource("testDescription.transformation").getFile());
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLFile(descriptionFile);
        installTransformations(descriptions);

        ModelA modelA = new ModelA();
        modelA.setIdA("test1");
        modelA.setTestA("test2");
        modelA.setBlubA("test3");
        modelA.setBlaA("test4");

        ModelB modelB = new ModelB();
        modelB.setIdB("test1");
        modelB.setTestB("hello");
        modelB.setBlubB("test3#test4");

        ModelB resultB = transformModelAToModelB(modelA);
        ModelA resultA = transformModelBToModelA(modelB);

        assertThat(resultB.getIdB(), is("test1"));
        assertThat(resultB.getTestB(), is("test"));
        assertThat(resultB.getBlubB(), is("test3#test4"));

        assertThat(resultA.getIdA(), is("test1"));
        assertThat(resultA.getTestA(), is("world"));
        assertThat(resultA.getBlubA(), is("test3"));
        assertThat(resultA.getBlaA(), is("test4"));
    }

    @Test
    public void testRetrievedTransformationsFromFile2_shouldWork() throws Exception {
        File descriptionFile =
            new File(getClass().getClassLoader().getResource("testDescription2.transformation").getFile());
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLFile(descriptionFile);
        installTransformations(descriptions);

        ModelB modelB = new ModelB();
        modelB.setIdB("TEST");
        modelB.setTestB("test2");
        modelB.setBlubB("testHellotest");
        List<String> elements = new ArrayList<String>();
        elements.add("A");
        elements.add("B");
        elements.add("C");
        modelB.setElements(elements);

        ModelA modelA = new ModelA();
        modelA.setIdA("          Test         ");
        modelA.setTestA("Hello");
        modelA.setBlubA("testHellotest");

        ModelB resultB = transformModelAToModelB(modelA);
        ModelA resultA = transformModelBToModelA(modelB);

        assertThat(resultA.getIdA(), is(modelB.getIdB().toLowerCase()));
        assertThat(resultA.getTestA(), is(modelB.getTestB().toUpperCase()));
        assertThat(resultA.getBlubA(), is(modelB.getElements().size() + ""));
        assertThat(resultA.getBlaA(), is("Hello World"));

        assertThat(resultB.getIdB(), is("Test"));
        assertThat(resultB.getTestB(), is(modelA.getTestA().length() + ""));
        assertThat(resultB.getBlubB(), is("blubHelloblub"));
    }

    @Test
    public void testRetrievedTransformationsFromFile3_shouldWork() throws Exception {
        File descriptionFile =
            new File(getClass().getClassLoader().getResource("testDescription3.transformation").getFile());
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLFile(descriptionFile);
        installTransformations(descriptions);
        ModelB modelB = new ModelB();
        modelB.setIdB("??##??id");
        modelB.setTestB("hello");
        modelB.setBlubB("test3#test4");

        ModelA modelA = new ModelA();
        modelA.setIdA("1");
        modelA.setTestA("works?");

        ModelB resultB = transformModelAToModelB(modelA);
        ModelA resultA = transformModelBToModelA(modelB);

        assertThat(resultA.getIdA(), is("id"));
        assertThat(resultA.getTestA(), is("olleh"));
        assertThat(resultA.getBlubA(), is("test3"));
        assertThat(resultA.getBlaA(), is("test4"));

        assertThat(resultB.getIdB(), is("0001"));
        assertThat(resultB.getTestB(), is("works?!"));
        assertThat(resultB.getIntValue(), is(1));
    }

    private void installTransformation(TransformationDescription description) {
        service.saveDescription(description);
        when(graph.getTransformationPath(description.getSourceModel(), description.getTargetModel(),
            new ArrayList<String>())).thenReturn(Arrays.asList(description));
    }

    private void installTransformations(List<TransformationDescription> descriptions) {
        for (TransformationDescription description : descriptions) {
            installTransformation(description);
        }
    }
}
