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

package org.openengsb.core.ekb.graph.orient;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.graph.orient.internal.OrientModelGraph;
import org.openengsb.core.ekb.graph.orient.models.ModelA;
import org.openengsb.core.ekb.graph.orient.models.ModelB;
import org.openengsb.core.ekb.graph.orient.models.ModelC;
import org.osgi.framework.Version;

public class OrientModelGraphTest {
    private static OrientModelGraph graph;

    @BeforeClass
    public static void init() {
        graph = new OrientModelGraph();
    }

    @Before
    public void start() {
        graph.cleanDatabase();
        graph.addModel(getModelADescription());
        graph.addModel(getModelBDescription());
        graph.addModel(getModelCDescription());
    }

    @AfterClass
    public static void shutdown() {
        graph.shutdown();
    }

    private static ModelDescription getModelADescription() {
        return new ModelDescription(ModelA.class, new Version(1, 0, 0));
    }

    private static ModelDescription getModelBDescription() {
        return new ModelDescription(ModelB.class, new Version(1, 0, 0));
    }

    private static ModelDescription getModelCDescription() {
        return new ModelDescription(ModelC.class, new Version(1, 0, 0));
    }

    private TransformationDescription getDescriptionForModelAToModelB() {
        return new TransformationDescription(getModelADescription(), getModelBDescription());
    }

    private TransformationDescription getDescriptionForModelBToModelC() {
        return new TransformationDescription(getModelBDescription(), getModelCDescription());
    }

    private TransformationDescription getDescriptionForModelAToModelC() {
        return new TransformationDescription(getModelADescription(), getModelCDescription());
    }

    @Test
    public void testIsTransformationPossible_shouldFindPath() throws Exception {
        TransformationDescription description = getDescriptionForModelAToModelB();
        description.setId("test");
        graph.addTransformation(description);
        boolean possible = graph.isTransformationPossible(getModelADescription(), getModelBDescription(), null);
        assertThat(possible, is(true));
    }

    @Test
    public void testIsTransformationPossiblePath_shouldFindPath() throws Exception {
        TransformationDescription description = getDescriptionForModelAToModelB();
        graph.addTransformation(description);
        TransformationDescription description2 = getDescriptionForModelBToModelC();
        graph.addTransformation(description2);
        boolean possible = graph.isTransformationPossible(getModelADescription(), getModelBDescription(), null);
        assertThat(possible, is(true));
    }

    @Test
    public void testFindTransformationPathWithIDs_shouldFindDifferentPaths() throws Exception {
        TransformationDescription description1 = getDescriptionForModelAToModelB();
        description1.setId("test1");
        graph.addTransformation(description1);
        TransformationDescription description2 = getDescriptionForModelAToModelB();
        description2.setId("test2");
        graph.addTransformation(description2);
        TransformationDescription description3 = getDescriptionForModelBToModelC();
        graph.addTransformation(description3);

        List<TransformationDescription> path1 =
            graph.getTransformationPath(getModelADescription(), getModelCDescription(), Arrays.asList("test1"));
        List<TransformationDescription> path2 =
            graph.getTransformationPath(getModelADescription(), getModelCDescription(), Arrays.asList("test2"));

        assertThat(path1.get(0).getId(), is("test1"));
        assertThat(path2.get(0).getId(), is("test2"));
    }
    
    @Test
    public void testIfModelDeactivationWorks_shouldWork() throws Exception {
        graph.removeModel(getModelADescription());
        assertThat(graph.isModelActive(getModelADescription()), is(false));
        graph.addModel(getModelADescription());
        assertThat(graph.isModelActive(getModelADescription()), is(true));
    }
    
    @Test
    public void testIfDeactivatedModelsAreNotUsed_shouldIgnoreInactiveModels() throws Exception {
        TransformationDescription description = getDescriptionForModelAToModelB();
        description.setId("test1");
        graph.addTransformation(description);
        description = getDescriptionForModelBToModelC();
        description.setId("test2");
        graph.addTransformation(description);
        boolean possible1 = graph.isTransformationPossible(getModelADescription(), getModelCDescription(), null);
        graph.removeModel(getModelBDescription());
        boolean possible2 = graph.isTransformationPossible(getModelADescription(), getModelCDescription(), null);
        description = getDescriptionForModelAToModelC();
        description.setId("test3");
        graph.addTransformation(description);
        boolean possible3 = graph.isTransformationPossible(getModelADescription(), getModelCDescription(), null);
        assertThat(possible1, is(true));
        assertThat(possible2, is(false));
        assertThat(possible3, is(true));
    }
    
    @Test
    public void testIfLoadingByFilenameWorks_shouldLoadByFilename() throws Exception {
        TransformationDescription description = getDescriptionForModelAToModelB();
        description.setId("test1");
        graph.addTransformation(description);
        description = getDescriptionForModelBToModelC();
        description.setId("test2");
        graph.addTransformation(description);
        description = getDescriptionForModelAToModelB();
        description.setId("test3");
        description.setFileName("testfile");
        graph.addTransformation(description);
        description = getDescriptionForModelBToModelC();
        description.setId("test4");
        graph.addTransformation(description);
        
        List<TransformationDescription> result = graph.getTransformationsPerFileName("testfile");
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getId(), is("test3"));
    }
    
    @Test
    public void testIfTransformationDeletionWorks_shouldDeleteTransformationDescription() throws Exception {
        TransformationDescription description = getDescriptionForModelAToModelB();
        description.setId("test1");
        graph.addTransformation(description);
        boolean possible1 = graph.isTransformationPossible(getModelADescription(), getModelBDescription(), null);
        graph.removeTransformation(description);
        boolean possible2 = graph.isTransformationPossible(getModelADescription(), getModelBDescription(), null);
        assertThat(possible1, is(true));
        assertThat(possible2, is(false));
    }

    // TODO: fill this class with tests
}
