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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.LocalizableString;
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

    private Log log = LogFactory.getLog(AbstractOsgiMockServiceTest.class);

    protected BundleContext bundleContext;
    protected Bundle bundle;

    private Map<ServiceReference, Dictionary<String, Object>> serviceReferences =
        new HashMap<ServiceReference, Dictionary<String, Object>>();
    private Map<ServiceReference, Object> services = new HashMap<ServiceReference, Object>();
    private Long serviceId = Long.MAX_VALUE;

    @Before
    public void prepareServiceRegistry() throws Exception {
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
                        filterString = String.format("(%s=%s)", Constants.OBJECTCLASS, clazz);
                    } else {
                        filterString = String.format("(&(%s=%s)%s)", Constants.OBJECTCLASS, clazz, filterString);
                    }
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
                    @SuppressWarnings("unchecked")
                    Dictionary<String, Object> dict = (Dictionary<String, Object>) invocation.getArguments()[2];
                    final ServiceReference serviceReference = registerService(service, dict, clazzes);
                    ServiceRegistration result = mock(ServiceRegistration.class);
                    doAnswer(new Answer<Void>() {
                        @Override
                        public Void answer(InvocationOnMock invocation) throws Throwable {
                            services.remove(serviceReference);
                            serviceReferences.remove(serviceReference);
                            return null;
                        }
                    }).when(result).unregister();
                    doAnswer(new Answer<Void>() {
                        @Override
                        public Void answer(InvocationOnMock invocation) throws Throwable {
                            Dictionary<String, Object> arg = (Dictionary<String, Object>) invocation.getArguments()[0];
                            Dictionary<String, Object> newDict = new Hashtable<String, Object>();
                            Enumeration<String> keys = arg.keys();
                            while (keys.hasMoreElements()) {
                                String next = keys.nextElement();
                                newDict.put(next, arg.get(next));
                            }
                            serviceReferences.put(serviceReference, newDict);
                            return null;
                        }
                    }).when(result).setProperties(any(Dictionary.class));
                    when(result.getReference()).thenReturn(serviceReference);
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

    @After
    public void clearRegistry() throws Exception {
        bundleContext = null;
        serviceReferences = null;
        services = null;
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
    protected ServiceReference registerService(Object service, Dictionary<String, Object> props, String... interfazes) {
        props.put(Constants.OBJECTCLASS, interfazes);
        return putService(service, props);
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

    private ServiceReference putService(Object service, Dictionary<String, Object> props) {
        ServiceReference serviceReference = mock(ServiceReference.class);
        long serviceId = --this.serviceId;
        log.info("registering service with ID: " + serviceId);
        props.put(Constants.SERVICE_ID, serviceId);
        services.put(serviceReference, service);
        serviceReferences.put(serviceReference, props);
        when(serviceReference.getProperty(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return serviceReferences.get(invocation.getMock()).get(invocation.getArguments()[0]);
            }
        });
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(serviceReference.getPropertyKeys()).thenAnswer(new Answer<String[]>() {
            @Override
            public String[] answer(InvocationOnMock invocation) throws Throwable {
                Dictionary<String, Object> dictionary = serviceReferences.get(invocation.getMock());
                List<?> list = EnumerationUtils.toList(dictionary.keys());
                Collection<String> typedCollection = CollectionUtils.typedCollection(list, String.class);
                return typedCollection.toArray(new String[0]);
            }
        });
        return serviceReference;
    }

    protected abstract void setBundleContext(BundleContext bundleContext);

    protected ServiceInstanceFactory createFactoryMock(String connector, String... domains) throws Exception {
        ServiceInstanceFactory factory = mock(ServiceInstanceFactory.class);
        when(factory.createNewInstance(anyString())).thenAnswer(new Answer<Domain>() {
            @Override
            public Domain answer(InvocationOnMock invocation) throws Throwable {
                Domain result = mock(Domain.class);
                String id = (String) invocation.getArguments()[0];
                when(result.getInstanceId()).thenReturn(id);
                return result;
            }
        });
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("connector", connector);
        props.put("domain", domains);
        registerService(factory, props, ServiceInstanceFactory.class);
        return factory;
    }

    protected DomainProvider createDomainProviderMock(final Class<? extends Domain> interfaze, String name) {
        DomainProvider domainProviderMock = mock(DomainProvider.class);
        LocalizableString testDomainLocalizedStringMock = mock(LocalizableString.class);
        when(testDomainLocalizedStringMock.getString(Mockito.<Locale> any())).thenReturn(name);
        when(domainProviderMock.getId()).thenReturn(name);
        when(domainProviderMock.getName()).thenReturn(testDomainLocalizedStringMock);
        when(domainProviderMock.getDescription()).thenReturn(testDomainLocalizedStringMock);
        when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return interfaze;
            }
        });
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("domain", name);
        registerService(domainProviderMock, props, DomainProvider.class);
        return domainProviderMock;
    }

    public LocalizableString mockLocalizeableString(String value) {
        LocalizableString mock2 = mock(LocalizableString.class);
        when(mock2.getString(any(Locale.class))).thenReturn(value);
        return mock2;
    }

    protected ConnectorProvider createConnectorProviderMock(String connectorType, String... domains) {
        ConnectorProvider connectorProvider = mock(ConnectorProvider.class);
        when(connectorProvider.getId()).thenReturn(connectorType);
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("connector", connectorType);
        props.put("domain", domains);
        registerService(connectorProvider, props, ConnectorProvider.class);
        ServiceDescriptor descriptor = mock(ServiceDescriptor.class);
        when(descriptor.getId()).thenReturn(connectorType);
        LocalizableString name = mockLocalizeableString("service.name");
        when(descriptor.getName()).thenReturn(name);
        LocalizableString desc = mockLocalizeableString("service.description");
        when(descriptor.getDescription()).thenReturn(desc);
        when(connectorProvider.getDescriptor()).thenReturn(descriptor);
        return connectorProvider;
    }

}
