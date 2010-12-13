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

package org.openengsb.core.common.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.communication.CallRouter;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.support.NullDomain;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class ProxySetupTest {

    private static final String ID = "ID";
    private DomainService domainService;
    private DomainProvider provider;

    @Before
    public void setUp() {
        domainService = mock(DomainService.class);
        provider = Mockito.mock(DomainProvider.class);
        when(provider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return NullDomain.class;
            }
        });
        LocalizableString stringMock = mock(LocalizableString.class);
        when(provider.getName()).thenReturn(stringMock);
        when(provider.getId()).thenReturn(ID);
        when(domainService.domains()).thenReturn(Arrays.asList(new DomainProvider[]{provider}));
    }

    @Test
    public void returnMockDomainInterface_shouldAddProxyToBundleContext() throws URISyntaxException {
        BundleContext mockContext = mock(BundleContext.class);
        Bundle mockBundle = mock(Bundle.class);
        Dictionary<?, ?> headers = mock(Dictionary.class);
        when(mockBundle.getHeaders()).thenReturn(headers);
        when(mockContext.getBundle()).thenReturn(mockBundle);

        InvocationHandler invocationHandlerMock = mock(InvocationHandler.class);
        InvocationHandlerFactory mock = mock(InvocationHandlerFactory.class);
        when(mock.createInstance(Mockito.any(DomainProvider.class))).thenReturn(invocationHandlerMock);

        CallRouter callRouter = mock(CallRouter.class);
        ProxySetup jmsConnector = new ProxySetup(domainService);
        jmsConnector.setBundleContext(mockContext);
        jmsConnector.addProxiesToContext();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockContext)
            .registerService(eq(ServiceManager.class.getName()), captor.capture(), any(Dictionary.class));
        assertTrue(captor.getValue() instanceof ProxyServiceManager);
        ProxyServiceManager manager = (ProxyServiceManager) captor.getValue();

        // This line is required since spring cannot set it automatically
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("portId", "123");
        attributes.put("destination", "456");
        manager.update("12345", attributes);
        verify(mockContext).registerService(eq(new String[]{NullDomain.class.getName(), Domain.class.getName()}),
            captor.capture(), any(Dictionary.class));
        ProxyConnector invocationHandler = (ProxyConnector) Proxy.getInvocationHandler(captor.getValue());

        assertThat(invocationHandler.getPortId(), equalTo("123"));
        assertThat(invocationHandler.getDestination(), equalTo(new URI("456")));
        assertThat(invocationHandler.getCallRouter(), sameInstance(callRouter));
    }
}
