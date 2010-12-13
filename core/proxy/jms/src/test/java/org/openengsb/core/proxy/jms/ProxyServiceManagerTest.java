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

package org.openengsb.core.proxy.jms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.communication.CallRouter;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.proxy.ProxyServiceManager;
import org.openengsb.core.test.NullDomain;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Tests are only very few, as the ProxyServiceManager is mostly copied from AbstractServiceManager and has to be merged
 * together with it soon.
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
                return NullDomain.class;
            }
        });
        when(provider.getName()).thenReturn(new NullString());
        BundleContext mockContext = mock(BundleContext.class);
        Bundle mockBundle = mock(Bundle.class);
        Dictionary<?, ?> headers = mock(Dictionary.class);
        when(mockBundle.getHeaders()).thenReturn(headers);
        when(mockContext.getBundle()).thenReturn(mockBundle);

        ProxyServiceManager manager = new ProxyServiceManager(provider, mock(CallRouter.class));
        manager.setBundleContext(mockContext);
        ServiceDescriptor descriptor = manager.getDescriptor();
        assertThat(descriptor.getId(), equalTo(string));
        List<AttributeDefinition> attributes = descriptor.getAttributes();
        assertThat(attributes.size(), equalTo(2));
        assertThat(attributes.get(0).getId(), equalTo("portId"));
        assertThat(attributes.get(1).getId(), equalTo("destination"));
        assertThat(descriptor.getImplementationType().getName(), equalTo(NullDomain.class.getName()));
        assertThat(descriptor.getServiceType().getName(), equalTo(NullDomain.class.getName()));
    }

    @SuppressWarnings("serial")
    private static class NullString implements LocalizableString {
        @Override
        public String getString(Locale locale) {
            return "";
        }

        @Override
        public String getKey() {
            return "";
        }
    }
}
