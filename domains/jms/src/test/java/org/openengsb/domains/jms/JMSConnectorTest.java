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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class JMSConnectorTest {

    @Test
    public void returnMockDomainInterface_shouldAddProxyToBundleContext() {
        BundleContext mockContext = mock(BundleContext.class);
        Bundle mockBundle = mock(Bundle.class);
        Dictionary headers = mock(Dictionary.class);
        when(mockBundle.getHeaders()).thenReturn(headers);
        when(mockContext.getBundle()).thenReturn(mockBundle);
        DomainService domainService = mock(DomainService.class);
        DomainProvider provider = Mockito.mock(DomainProvider.class);
        when(provider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return TestInterface.class;
            }
        });
        when(domainService.domains()).thenReturn(Arrays.asList(new DomainProvider[]{provider}));
        InvocationHandler invocationHandlerMock = mock(InvocationHandler.class);
        InvocationHandlerFactory mock = mock(InvocationHandlerFactory.class);
        when(mock.createInstance(Mockito.any(DomainProvider.class))).thenReturn(invocationHandlerMock);

        JMSConnector jmsConnector = new JMSConnector(domainService, mock);
        jmsConnector.setBundleContext(mockContext);
        jmsConnector.addProxiesToContext();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockContext)
            .registerService(eq(ServiceManager.class.getName()), captor.capture(), any(Dictionary.class));
        assertTrue(captor.getValue() instanceof ProxyServiceManager);
        ProxyServiceManager manager = (ProxyServiceManager) captor.getValue();
        
        manager.update("12345", new HashMap<String, String>());
        verify(mockContext).registerService(eq(new String[]{TestInterface.class.getName(), Domain.class.getName()}),
            captor.capture(), any(Dictionary.class));

        ((TestInterface) captor.getValue()).log(5);
        ArgumentCaptor<Method> methodCaptor = ArgumentCaptor.forClass(Method.class);
        try {
            verify(invocationHandlerMock).invoke(same(captor.getValue()), methodCaptor.capture(),
                eq(new Object[]{new Integer(5)}));
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Method value = methodCaptor.getValue();
        assertEquals("log", value.getName());
        assertEquals(TestInterface.class, value.getDeclaringClass());
    }

    private static interface TestInterface extends Domain {
        void log(int i);
    }
}
