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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.context.ContextHolder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Helper methods to mock the core {@link org.openengsb.core.api.OsgiUtilsService} service responsible for working with
 * the OpenEngSB osgi registry.
 */
public abstract class AbstractOsgiMockServiceTest {

    protected BundleContext bundleContext;
    protected Bundle bundle;

    private Map<ServiceReference, Dictionary<String, Object>> serviceReferences =
        new HashMap<ServiceReference, Dictionary<String, Object>>();
    private Map<ServiceReference, Object> services = new HashMap<ServiceReference, Object>();
    private Long serviceId = Long.MAX_VALUE;

    @Before
    public void setUp() throws Exception {
        bundleContext = mock(BundleContext.class);
        setBundleContext(bundleContext);
        when(bundleContext.getAllServiceReferences(anyString(), anyString())).thenAnswer(
            new Answer<ServiceReference[]>() {
                @Override
                public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
                    Method method =
                        invocation.getMock().getClass().getMethod("getServiceReferences", String.class, String.class);
                    return (ServiceReference[]) method.invoke(invocation.getMock(), invocation.getArguments());
                }
            });
        when(bundleContext.getServiceReferences(anyString(), anyString())).thenAnswer(new Answer<ServiceReference[]>() {
            @Override
            public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
                String clazz = (String) invocation.getArguments()[0];
                String filterString = (String) invocation.getArguments()[1];
                if (clazz != null) {
                    if (filterString == null) {
                        filterString = "";
                    }
                    filterString = String.format("(&(%s=%s)%s)", Constants.OBJECTCLASS, clazz, filterString);
                }
                Filter filter = FrameworkUtil.createFilter(filterString);
                Collection<ServiceReference> result = new ArrayList<ServiceReference>();
                for (Map.Entry<ServiceReference, Dictionary<String, Object>> entry : serviceReferences.entrySet()) {
                    if (filter.match(entry.getValue())) {
                        result.add(entry.getKey());
                    }
                }
                return result.toArray(new ServiceReference[result.size()]);
            }
        });
        when(bundleContext.getService(any(ServiceReference.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ServiceReference ref = (ServiceReference) invocation.getArguments()[0];
                return services.get(ref);
            }
        });
        when(bundleContext.registerService(any(String[].class), any(), any(Dictionary.class))).thenAnswer(
            new Answer<ServiceRegistration>() {
                @Override
                public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {
                    String[] clazzes = (String[]) invocation.getArguments()[0];
                    final Object service = invocation.getArguments()[1];
                    Dictionary<String, Object> dict = (Dictionary<String, Object>) invocation.getArguments()[2];
                    registerService(service, dict, clazzes);
                    ServiceRegistration result = mock(ServiceRegistration.class);
                    doAnswer(new Answer<Void>() {
                        @Override
                        public Void answer(InvocationOnMock invocation) throws Throwable {
                            services.remove(service);
                            return null;
                        }
                    }).when(result).unregister();
                    return result;
                }
            });

        bundle = mock(Bundle.class);
        when(bundle.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getBundle()).thenReturn(bundle);
        when(bundle.loadClass(anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return this.getClass().getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        });
    }

    /**
     * create a mock of the specified class and register the service under that interface. It also adds the given id as
     * property to the service.
     */
    protected <T> T mockService(Class<T> serviceClass, String id) {
        T serviceMock = mock(serviceClass);
        registerService(serviceMock, id, serviceClass);
        return serviceMock;
    }

    /**
     * registers the service with the given properties under the given interfaces
     */
    protected void registerService(Object service, Dictionary<String, Object> props, Class<?>... interfazes) {
        Set<String> objectClasses = new HashSet<String>();
        for (Class<?> interfaze : interfazes) {
            objectClasses.add(interfaze.getCanonicalName());
        }
        objectClasses.add(OpenEngSBService.class.getCanonicalName());
        props.put(Constants.OBJECTCLASS, objectClasses.toArray(new String[objectClasses.size()]));
        putService(service, props);
    }

    /**
     * registers the service with the given properties under the given interfaces
     */
    protected void registerService(Object service, Dictionary<String, Object> props, String... interfazes) {
        props.put(Constants.OBJECTCLASS, interfazes);
        putService(service, props);
    }

    /**
     * registers the service under the given interfaces and sets its location-property accordingly:
     * location.context=location
     */
    protected void registerServiceAtLocation(Object service, String location, String context, Class<?>... interfazes) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("location." + context, new String[]{ location });
        registerService(service, props, interfazes);
    }

    /**
     * registers the service under the given interfaces and sets its location-property for the current context
     * accordingly: location.context=location
     */
    protected void registerServiceAtLocation(Object service, String location, Class<?>... interfazes) {
        registerServiceAtLocation(service, location, ContextHolder.get().getCurrentContextId(), interfazes);
    }

    /**
     * registers the service under the given interfaces and sets it's location in the root-context
     */
    protected void registerServiceAtRootLocation(Object service, String location, Class<?>... interfazes) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("location.root", new String[]{ location });
        registerService(service, props, interfazes);
    }

    /**
     * registers the service under the given interfaces, settint its "id"-property to the given id
     */
    protected void registerService(Object service, String id, Class<?>... interfaces) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("id", id);
        registerService(service, props, interfaces);
    }

    protected void registerServiceViaId(Object service, String id, Class<?>... interfaces) {
        registerService(service, id, interfaces);
    }

    private void putService(Object service, Dictionary<String, Object> props) {
        ServiceReference serviceReference = mock(ServiceReference.class);
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(--serviceId);
        services.put(serviceReference, service);
        serviceReferences.put(serviceReference, props);
    }

    protected abstract void setBundleContext(BundleContext bundleContext);

}
