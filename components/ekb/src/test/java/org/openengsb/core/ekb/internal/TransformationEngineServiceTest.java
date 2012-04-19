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

package org.openengsb.core.ekb.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.ekb.transformation.TransformationDescription;
import org.openengsb.core.common.transformations.TransformationUtils;
import org.openengsb.core.ekb.internal.models.ModelA;
import org.openengsb.core.ekb.internal.models.ModelB;

public class TransformationEngineServiceTest {
    private TransformationEngineService service;

    @Before
    public void init() {
        service = new TransformationEngineService();
    }

    @Test
    public void testSimpleForwardTransformations_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.forwardField("blubA", "blubB");
        service.saveDescription(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = service.performTransformation(ModelA.class, ModelB.class, model);
        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3"));
    }
    
    @Test
    public void testForwardTransformationsWithTemporaryField_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.forwardField("blubA", "temp.test");
        desc.forwardField("temp.test", "blubB");
        service.saveDescription(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = service.performTransformation(ModelA.class, ModelB.class, model);
        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3"));
    }

    @Test
    public void testMixedForwardTransformations_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "testB");
        desc.forwardField("testA", "blubB");
        desc.forwardField("blubA", "idB");
        service.saveDescription(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = service.performTransformation(ModelA.class, ModelB.class, model);
        assertThat(result.getIdB(), is("test3"));
        assertThat(result.getTestB(), is("test1"));
        assertThat(result.getBlubB(), is("test2"));
    }

    @Test
    public void testConcatTransformation_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.concatField("blubB", "#", "blubA", "blaA");
        service.saveDescription(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");
        model.setBlaA("test4");

        ModelB result = service.performTransformation(ModelA.class, ModelB.class, model);
        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3#test4"));
    }

    @Test
    public void testSplitTransformation_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelB.class, ModelA.class);
        desc.forwardField("idB", "idA");
        desc.forwardField("testB", "testA");
        desc.splitField("blubB", "blubA", "#", "0");
        desc.splitField("blubB", "blaA", "#", "1");
        service.saveDescription(desc);

        ModelB model = new ModelB();
        model.setIdB("test1");
        model.setTestB("test2");
        model.setBlubB("test3#test4");

        ModelA result = service.performTransformation(ModelB.class, ModelA.class, model);
        assertThat(result.getIdA(), is("test1"));
        assertThat(result.getTestA(), is("test2"));
        assertThat(result.getBlubA(), is("test3"));
        assertThat(result.getBlaA(), is("test4"));
    }
    
    @Test
    public void testMapTransformation_shoulWork() {
        TransformationDescription desc = new TransformationDescription(ModelB.class, ModelA.class);
        desc.forwardField("idB", "idA");
        desc.forwardField("testB", "testA");
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("hello", "world");
        mapping.put("dog", "cat");
        desc.mapField("blubB", "blubA", mapping);
        service.saveDescription(desc);
        
        ModelB model1 = new ModelB();
        model1.setIdB("test1");
        model1.setTestB("test2");
        model1.setBlubB("hello");
        
        ModelB model2 = new ModelB();
        model2.setIdB("test1");
        model2.setTestB("test2");
        model2.setBlubB("dog");
        
        ModelA result1 = service.performTransformation(ModelB.class, ModelA.class, model1);
        ModelA result2 = service.performTransformation(ModelB.class, ModelA.class, model2);
        
        assertThat(result1.getBlubA(), is("world"));
        assertThat(result2.getBlubA(), is("cat"));
    }
    
    @Test
    public void testSubStringTransformation_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelA.class, ModelB.class);
        desc.substringField("idA", "idB", "0", "4");
        service.saveDescription(desc);
        
        ModelA model = new ModelA();
        model.setIdA("this is a test");
        
        ModelB result = service.performTransformation(ModelA.class, ModelB.class, model);
        assertThat(result.getIdB(), is("this"));
    }
    
    @Test
    public void testValueTransformation_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "idB");
        desc.valueField("testB", "blub");
        service.saveDescription(desc);
        
        ModelA model = new ModelA();
        model.setIdA("this is a test");
        
        ModelB result = service.performTransformation(ModelA.class, ModelB.class, model);
        assertThat(result.getIdB(), is("this is a test"));
        assertThat(result.getTestB(), is("blub"));
    }
    
    @Test
    public void testLengthTransformation_shouldWork() {
        TransformationDescription desc = new TransformationDescription(ModelB.class, ModelA.class);
        desc.lengthField("testB", "testA", null);
        desc.lengthField("elements", "blubA", "size");
        service.saveDescription(desc);
        
        ModelB model = new ModelB();
        model.setTestB("Hallo");
        List<String> elements = new ArrayList<String>();
        elements.add("A");
        elements.add("B");
        elements.add("C");
        model.setElements(elements);
        
        ModelA result = service.performTransformation(ModelB.class, ModelA.class, model);
        assertThat(result.getTestA(), is(model.getTestB().length() + ""));
        assertThat(result.getBlubA(), is(model.getElements().size() + ""));
    }

    @Test
    public void testRetrievedTransformationsFromFile1_shouldWork() {
        File descriptionFile = new File(getClass().getClassLoader().getResource("testDescription.xml").getFile());
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLFile(descriptionFile);
        service.saveDescriptions(descriptions);

        ModelA modelA = new ModelA();
        modelA.setIdA("test1");
        modelA.setTestA("test2");
        modelA.setBlubA("test3");
        modelA.setBlaA("test4");
        
        ModelB modelB = new ModelB();
        modelB.setIdB("test1");
        modelB.setTestB("hello");
        modelB.setBlubB("test3#test4");

        ModelB resultB = service.performTransformation(ModelA.class, ModelB.class, modelA);
        ModelA resultA = service.performTransformation(ModelB.class, ModelA.class, modelB);
        
        assertThat(resultB.getIdB(), is("test1"));
        assertThat(resultB.getTestB(), is("test"));
        assertThat(resultB.getBlubB(), is("test3#test4"));
        
        assertThat(resultA.getIdA(), is("test1"));
        assertThat(resultA.getTestA(), is("world"));
        assertThat(resultA.getBlubA(), is("test3"));
        assertThat(resultA.getBlaA(), is("test4"));
    }
    
    @Test
    public void testRetrievedTransformationsFromFile2_shouldWork() {
        File descriptionFile = new File(getClass().getClassLoader().getResource("testDescription2.xml").getFile());
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLFile(descriptionFile);
        service.saveDescriptions(descriptions);
        ModelB modelB = new ModelB();
        modelB.setIdB("test1");
        modelB.setTestB("test2");
        modelB.setBlubB("test3");
        List<String> elements = new ArrayList<String>();
        elements.add("A");
        elements.add("B");
        elements.add("C");
        modelB.setElements(elements);
        
        ModelA modelA = new ModelA();
        modelA.setTestA("Hallo");
        
        ModelA resultA = service.performTransformation(ModelB.class, ModelA.class, modelB);
        ModelB resultB = service.performTransformation(ModelA.class, ModelB.class, modelA);
        
        assertThat(resultA.getIdA(), is(modelB.getIdB()));
        assertThat(resultA.getTestA(), is(modelB.getTestB()));
        assertThat(resultA.getBlubA(), is(modelB.getElements().size() + ""));
        assertThat(resultA.getBlaA(), is("Hello World"));
        
        assertThat(resultB.getTestB(), is(modelA.getTestA().length() + ""));
    }
}
