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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WireingService;
import org.openengsb.core.api.context.ContextHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Helper methods to mock the core {@link OsgiUtilsService} service responsible for working with the OpenEngSB osgi
 * registry.
 */
public abstract class AbstractOsgiMockServiceTest {

    protected OsgiUtilsService serviceUtils;
    protected WireingService wiringService;

    /**
     * Set up the default OSGi Services such as {@link WireingService} and {@link OsgiUtilsService}
     */
    @Before
    public void setUp() throws Exception {
        serviceUtils = mock(OsgiUtilsService.class);
        wiringService = mock(WireingService.class);
        when(serviceUtils.getOsgiServiceProxy(OsgiUtilsService.class)).thenReturn(serviceUtils);
        when(serviceUtils.getOsgiServiceProxy(WireingService.class)).thenReturn(wiringService);
        initializeOpenEngSBCoreServicesObject(serviceUtils);
    }

    /**
     * Since the testbundle does not have access to the OpenEngSBCoreServices bundle the serviceUtils have to be set by
     * hand
     */
    protected abstract void initializeOpenEngSBCoreServicesObject(OsgiUtilsService serviceUtils);

    /**
     * Simple method creating a mock, returning it and also registere it via its id in the
     * {@link #registerServiceViaId(Object, String, Class)} method
     */
    protected <T> T mockService(Class<T> serviceClass, String id) throws Exception {
        T serviceMock = mock(serviceClass);
        registerServiceViaId(serviceMock, id, serviceClass);
        return serviceMock;
    }

    /**
     * Helper method providing a very primitiv {@link BundleContext} mock if tests require it.
     */
    protected BundleContext createBundleContextMock() throws Exception {
        BundleContext bundleContext = mock(BundleContext.class);
        when(bundleContext.getAllServiceReferences(Mockito.anyString(), Mockito.anyString())).thenAnswer(
            new Answer<ServiceReference[]>() {
                @Override
                public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
                    Method method =
                        invocation.getMock().getClass().getMethod("getServiceReferences", String.class, String.class);
                    return (ServiceReference[]) method.invoke(invocation.getMock(), invocation.getArguments());
                }
            });
        when(bundleContext.getBundle()).thenReturn(new DummyBundle());
        return bundleContext;
    }

    /**
     * Registeres services using the location services of the OpenEngSB.All sub-interfaces are also automatically
     * registered.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void registerLocationService(Object service, Class interfaze, String location) throws Exception {
        ArrayList<Class> all = new ArrayList<Class>();
        findInterfaces(interfaze, all);
        for (Class class1 : all) {
            Filter filter =
                getFilterForLocation(class1, location, ContextHolder.get().getCurrentContextId().toString());
            when(serviceUtils.getFilterForLocation(class1, location)).thenReturn(filter);
            when(serviceUtils.getFilterForLocation(class1, location, ContextHolder.get().getCurrentContextId()))
                .thenReturn(filter);
            when(serviceUtils.getOsgiServiceProxy(filter, class1)).thenReturn(service);
        }
    }

    /**
     * Simple registers a service with a specific interface and filter. All sub-interfaces are also automatically
     * registered.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void registerService(Object service, Class interfaze, String filter)
        throws Exception {
        ArrayList<Class> all = new ArrayList<Class>();
        findInterfaces(interfaze, all);
        for (Class class1 : all) {
            Filter realFilter = makeFilter(class1, filter);
            when(serviceUtils.makeFilter(class1, filter)).thenReturn(realFilter);
            when(serviceUtils.getOsgiServiceProxy(filter, class1)).thenReturn(service);
            when(serviceUtils.getOsgiServiceProxy(class1)).thenReturn(service);
            when(serviceUtils.getServiceWithId(class1, filter)).thenReturn(service);
            when(serviceUtils.getService(realFilter.toString())).thenReturn(service);
            when(serviceUtils.getService(Mockito.eq(realFilter), Mockito.anyLong())).thenReturn(service);
            when(serviceUtils.getOsgiServiceProxy(realFilter.toString(), interfaze)).thenReturn(service);
        }
    }

    /**
     * If the query methods via id=xxx should be used services should be regsisterd using this method. All
     * sub-interfaces are also automatically registered.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void registerServiceViaId(final Object service, String id, final Class interfaze) throws Exception {
        List<Class> allInterfaces = new ArrayList<Class>();
        findInterfaces(interfaze, allInterfaces);
        for (Class local : allInterfaces) {
            String idQuery = String.format("(id=%s)", id);
            when(serviceUtils.getServiceWithId(local, id)).thenReturn(service);
            when(serviceUtils.getOsgiServiceProxy(FrameworkUtil.createFilter(idQuery), local)).thenReturn(service);
            when(serviceUtils.getOsgiServiceProxy(local)).thenReturn(service);
        }
    }

    @SuppressWarnings("rawtypes")
    private void findInterfaces(Class interfaze, List<Class> allInterfaces) {
        allInterfaces.add(interfaze);
        Class[] interfaces = interfaze.getInterfaces();
        if (interfaces == null || interfaces.length == 0) {
            return;
        }
        for (Class local : interfaces) {
            findInterfaces(local, allInterfaces);
        }
    }

    private Filter getFilterForLocation(Class<?> clazz, String location, String context) {
        String filter = makeLocationFilterString(location, context);
        try {
            return makeFilter(clazz, filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("location is invalid", e);
        }
    }

    private String makeLocationFilterString(String location, String context) {
        return String.format("(|(location.%s=%s)(location.root=%s))", context, location, location);
    }

    private Filter makeFilter(Class<?> clazz, String otherFilter) throws InvalidSyntaxException {
        return makeFilter(clazz.getName(), otherFilter);
    }

    private Filter makeFilter(String className, String otherFilter) throws InvalidSyntaxException {
        return FrameworkUtil.createFilter("(&" + makeFilterForClass(className) + otherFilter + ")");
    }

    private Filter makeFilterForClass(String className) {
        try {
            return FrameworkUtil.createFilter(String.format("(%s=%s)", Constants.OBJECTCLASS, className));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
