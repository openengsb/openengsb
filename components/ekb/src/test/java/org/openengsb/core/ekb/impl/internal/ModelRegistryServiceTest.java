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

package org.openengsb.core.ekb.impl.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.impl.internal.graph.ModelGraph;
import org.openengsb.core.ekb.impl.internal.models.ModelA;
import org.openengsb.core.ekb.impl.internal.models.TestAnnotation;
import org.openengsb.core.ekb.impl.internal.models.TestModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Version;

public class ModelRegistryServiceTest {
    private ModelRegistryService registry;
    private ModelGraph graph;

    @Before
    public void init() {
        registry = ModelRegistryService.getInstance();
        graph = mock(ModelGraph.class);
        registry.setEkbClassLoader(new EKBTestClassLoader());
        registry.setGraphDb(graph);
    }

    private BundleEvent getMockedBundleEvent(boolean full) throws Exception {
        BundleEvent event = mock(BundleEvent.class);
        Bundle bundle = new MockedBundle(full);
        when(event.getBundle()).thenReturn(bundle);
        return event;
    }
    
    private ModelDescription getCorrectModel() {
        return new ModelDescription(TestModel.class, new Version(1, 0, 0));
    }
    
    private ModelDescription getIncorrectModel() {
        return new ModelDescription(ModelA.class, new Version(1, 0, 0));
    }

    @Test
    public void testIfModelRegistryOnlyAddsModels_shouldWork() throws Exception {
        BundleEvent event = getMockedBundleEvent(true);
        when(event.getType()).thenReturn(BundleEvent.STARTED);
        registry.bundleChanged(event);
        verify(graph).addModel(getCorrectModel());
        verify(graph, never()).addModel(getIncorrectModel());
    }
    
    @Test
    public void testIfModelRegistryRemovesModels_shouldWork() throws Exception {
        BundleEvent event = getMockedBundleEvent(true);
        when(event.getType()).thenReturn(BundleEvent.STOPPED);
        registry.bundleChanged(event);
        verify(graph).removeModel(getCorrectModel());
        verify(graph, never()).removeModel(getIncorrectModel());
    }
    
    @Test
    public void testIfModelRegistryIgnoresOtherEventTypes_shouldWork() throws Exception {
        BundleEvent event = getMockedBundleEvent(true);
        when(event.getType()).thenReturn(BundleEvent.INSTALLED);
        registry.bundleChanged(event);
        verify(graph, never()).removeModel(getCorrectModel());
        verify(graph, never()).removeModel(getIncorrectModel());
    }
    
    @Test
    public void testIfModelRegistryCanWorkWithEmptyBundles_shouldWork() throws Exception {
        BundleEvent event = getMockedBundleEvent(false);
        when(event.getType()).thenReturn(BundleEvent.STARTED);
        registry.bundleChanged(event);
    }
    
    @Test
    public void testIfModelRegistryRegistersAndUnregisteresModels_shouldWork() throws Exception {
        BundleEvent event = getMockedBundleEvent(true);
        when(event.getType()).thenReturn(BundleEvent.STARTED);
        registry.bundleChanged(event);
        when(event.getType()).thenReturn(BundleEvent.STOPPED);
        registry.bundleChanged(event);
        verify(graph).addModel(getCorrectModel());
        verify(graph).removeModel(getCorrectModel());
    }
    
    @Test
    public void testIfGetAnnotatedFieldsWork_shouldWork() throws Exception {
        List<String> fields = registry.getAnnotatedFields(getIncorrectModel(), TestAnnotation.class);
        assertThat(fields.size(), is(2));
        assertThat(fields.contains("idA"), is(true));
        assertThat(fields.contains("testA"), is(true));
    }
}
