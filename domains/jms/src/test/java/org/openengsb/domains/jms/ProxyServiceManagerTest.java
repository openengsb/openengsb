/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.jms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Tests are only very few, as the ProxyServiceManager is mostly copied from AbstractServiceManager and has to be merged
 * together with it soon.
 *
 * @author Florian Motlik
 *
 */
public class ProxyServiceManagerTest {

    @Test
    public void setupProxyServiceManager_shouldReturnCorrectServiceDescriptor() {
        DomainProvider provider = mock(DomainProvider.class);
        String string = "1234";
        when(provider.getId()).thenReturn(string);
        when(provider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return TestInterface.class;
            }
        });
        BundleContext mockContext = mock(BundleContext.class);
        Bundle mockBundle = mock(Bundle.class);
        Dictionary<?, ?> headers = mock(Dictionary.class);
        when(mockBundle.getHeaders()).thenReturn(headers);
        when(mockContext.getBundle()).thenReturn(mockBundle);

        ProxyServiceManager manager = new ProxyServiceManager(provider, null, mockContext);
        ServiceDescriptor descriptor = manager.getDescriptor();
        assertThat(descriptor.getId(), equalTo(string));
        assertThat(descriptor.getAttributes().size(), equalTo(0));
        assertThat(descriptor.getImplementationType().getName(), equalTo(TestInterface.class.getName()));
        assertThat(descriptor.getServiceType().getName(), equalTo(TestInterface.class.getName()));
    }

    private static interface TestInterface extends Domain {
    }
}
