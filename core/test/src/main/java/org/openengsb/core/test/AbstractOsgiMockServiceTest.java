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

package org.openengsb.core.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Activator;
import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public abstract class AbstractOsgiMockServiceTest {

    protected BundleContext bundleContext;

    @Before
    public void setUp() throws Exception {
        bundleContext = mock(BundleContext.class);
        new Activator().start(bundleContext);
        Bundle bundleMock = mock(Bundle.class);
        when(bundleContext.getBundle()).thenReturn(bundleMock);
        when(bundleMock.loadClass(anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return this.getClass().getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        });
        when(bundleContext.getAllServiceReferences(anyString(), anyString())).thenAnswer(
            new Answer<ServiceReference[]>() {
                @Override
                public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
                    Method method =
                        invocation.getMock().getClass().getMethod("getServiceReferences", String.class, String.class);
                    return (ServiceReference[]) method.invoke(invocation.getMock(), invocation.getArguments());
                }
            });
    }

    protected <T> T mockService(Class<T> serviceClass, String id)
        throws InvalidSyntaxException {
        T serviceMock = mock(serviceClass);
        registerService(serviceMock, id, serviceClass);
        return serviceMock;
    }

    protected void registerSerivce(Object service, Class<?>[] interfaces, String... validQueries)
        throws InvalidSyntaxException {
        if (service == null) {
            throw new IllegalArgumentException("service must not be null");
        }
        final ServiceReference serviceRefMock = mock(ServiceReference.class);
        ServiceReference[] mockAsArray = new ServiceReference[]{ serviceRefMock, };
        for (Class<?> serviceClass : interfaces) {
            when(bundleContext.getServiceReferences(serviceClass.getName(), null)).thenReturn(mockAsArray);
            for (String query : validQueries) {
                mockQueriesForString(mockAsArray, serviceClass, query);
            }
        }
        when(bundleContext.getService(serviceRefMock)).thenReturn(service);
    }

    protected void registerService(Object service, Class<?> interfaze, String... validQueries)
        throws InvalidSyntaxException {
        registerSerivce(service, new Class<?>[]{ interfaze }, validQueries);
    }

    protected void registerService(Object service, String id, Class<?>... interfaces) throws InvalidSyntaxException {
        final ServiceReference serviceRefMock = mock(ServiceReference.class);
        ServiceReference[] mockAsArray = new ServiceReference[]{ serviceRefMock, };
        for (Class<?> serviceClass : interfaces) {
            when(bundleContext.getServiceReferences(serviceClass.getName(), null)).thenReturn(mockAsArray);
            String idQuery = String.format("(id=%s)", id);
            mockQueriesForString(mockAsArray, serviceClass, idQuery);
            when(bundleContext.getService(serviceRefMock)).thenReturn(service);
        }
    }

    private void mockQueriesForString(ServiceReference[] mockAsArray, Class<?> serviceClass, String query)
        throws InvalidSyntaxException {
        when(bundleContext.getServiceReferences(eq(serviceClass.getName()), eq(query)))
            .thenReturn(mockAsArray);
        when(bundleContext.getServiceReferences(null,
            OsgiServiceUtils.makeFilter(serviceClass.getName(), query).toString()))
            .thenReturn(mockAsArray);
        when(bundleContext.getServiceReferences(null,
            OsgiServiceUtils.makeFilter(OpenEngSBService.class.getName(), query).toString()))
            .thenReturn(mockAsArray);
        when(
            bundleContext.getServiceReferences(eq(OpenEngSBService.class.getName()),
                eq(query)))
            .thenReturn(mockAsArray);
    }
}
