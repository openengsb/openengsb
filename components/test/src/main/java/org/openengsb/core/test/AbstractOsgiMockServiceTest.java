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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.LocalizableString;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods to mock the core {@link org.openengsb.core.api.OsgiUtilsService} service responsible for working with
 * the OpenEngSB osgi registry.
 *
 * ServiceManagement-operations are performed via the {@link BundleContext}. All these calls are handled using two maps
 * to mock a service-registry (serviceReferences, services)
 */
public abstract class AbstractOsgiMockServiceTest extends AbstractOpenEngSBTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOsgiMockServiceTest.class);

    protected BundleContext bundleContext;
    protected Bundle bundle;

    /**
     * This map keeps track of service-references and their properties
     */
    private Map<ServiceReference<?>, Dictionary<String, Object>> serviceReferences =
        new HashMap<ServiceReference<?>, Dictionary<String, Object>>();

    private Map<ServiceListener, Filter> listeners = new HashMap<ServiceListener, Filter>();

    /**
     * This map keeps track of service-references and their corresponding service-object
     */
    private Map<ServiceReference<?>, Object> services = new HashMap<ServiceReference<?>, Object>();
    private Long serviceId = Long.MAX_VALUE;

    @SuppressWarnings("unchecked")
    @Before
    public void prepareServiceRegistry() throws Exception {
        bundleContext = mock(BundleContext.class);
        /*
         * redirect calls to getAllServiceReferences to getServiceReferences, since we do not care for
         * Classloader-restrictions in unit-tests
         */
        when(bundleContext.getAllServiceReferences(anyString(), anyString())).thenAnswer(
            new Answer<ServiceReference<?>[]>() {
                @Override
                public ServiceReference<?>[] answer(InvocationOnMock invocation) throws Throwable {
                    String clazz = (String) invocation.getArguments()[0];
                    String filter = (String) invocation.getArguments()[1];
                    return bundleContext.getServiceReferences(clazz, filter);
                }
            });

        when(bundleContext.getServiceReference(any(Class.class))).thenAnswer(new Answer<ServiceReference<?>>() {
            @Override
            public ServiceReference<?> answer(InvocationOnMock invocation) throws Throwable {
                Class<?> clazz = (Class<?>) invocation.getArguments()[0];
                return bundleContext.getServiceReference(clazz.getName());
            }
        });

        when(bundleContext.getServiceReference(anyString())).thenAnswer(new Answer<ServiceReference<?>>() {
            @Override
            public ServiceReference<?> answer(InvocationOnMock invocation) throws Throwable {
                String clazz = (String) invocation.getArguments()[0];
                ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences(clazz, null);
                if (serviceReferences == null) {
                    return null;
                }
                return serviceReferences[0];
            }
        });

        when(bundleContext.getServiceReferences(any(Class.class), anyString())).thenAnswer(new Answer<Collection<?>>() {
            @Override
            public Collection<?> answer(InvocationOnMock invocation) throws Throwable {
                Class<?> clazz = (Class<?>) invocation.getArguments()[0];
                String filter = (String) invocation.getArguments()[1];
                ServiceReference<?>[] references = bundleContext.getAllServiceReferences(clazz.getName(), filter);
                return Arrays.asList(references);
            }
        });

        /*
         * retrieve a service-instance from the serviceReferencesMap
         */
        when(bundleContext.getServiceReferences(anyString(), anyString())).thenAnswer(
            new Answer<ServiceReference<?>[]>() {
                @Override
                public ServiceReference<?>[] answer(InvocationOnMock invocation) throws Throwable {
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
                    Collection<ServiceReference<?>> result = new ArrayList<ServiceReference<?>>();
                    synchronized (serviceReferences) {
                        for (Map.Entry<ServiceReference<?>, Dictionary<String, Object>> entry : serviceReferences
                            .entrySet()) {
                            if (filter.match(entry.getValue())) {
                                result.add(entry.getKey());
                            }
                        }
                        if (result.isEmpty()) {
                            return null;
                        }
                        return result.toArray(new ServiceReference<?>[result.size()]);
                    }
                }
            });
        /*
         * retrieves a service-object from the services-map
         */
        when(bundleContext.getService(any(ServiceReference.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ServiceReference<?> ref = (ServiceReference<?>) invocation.getArguments()[0];
                return services.get(ref);
            }
        });
        /*
         * register a new service. This step involves creating mock-objects for ServiceRegistration and
         * ServiceReference.
         */
        when(bundleContext.registerService(any(String[].class), any(), any(Dictionary.class))).thenAnswer(
            new Answer<ServiceRegistration<?>>() {
                @Override
                public ServiceRegistration<?> answer(InvocationOnMock invocation) throws Throwable {
                    String[] clazzes = (String[]) invocation.getArguments()[0];
                    final Object service = invocation.getArguments()[1];
                    Dictionary<String, Object> dict = (Dictionary<String, Object>) invocation.getArguments()[2];
                    return registerServiceInBundlecontext(clazzes, service, dict);
                }
            });
        when(bundleContext.registerService(anyString(), any(), any(Dictionary.class))).thenAnswer(
            new Answer<ServiceRegistration<?>>() {
                @Override
                public ServiceRegistration<?> answer(InvocationOnMock invocation) throws Throwable {
                    String clazz = (String) invocation.getArguments()[0];
                    final Object service = invocation.getArguments()[1];
                    Dictionary<String, Object> dict = (Dictionary<String, Object>) invocation.getArguments()[2];
                    return registerServiceInBundlecontext(new String[]{ clazz }, service, dict);
                }
            });
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ServiceListener listener = (ServiceListener) invocation.getArguments()[0];
                String filter = (String) invocation.getArguments()[1];
                synchronized (listeners) {
                    if (filter == null) {
                        listeners.put(listener, null);
                    } else {
                        listeners.put(listener, FrameworkUtil.createFilter(filter));
                    }
                }
                return null;
            }
        }).when(bundleContext).addServiceListener(any(ServiceListener.class), anyString());

        bundle = mock(Bundle.class);
        when(bundle.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getBundle()).thenReturn(bundle);
        /*
         * since we ignore ClassLoader-visibility issues in unit-tests, just load the class
         */
        when(bundle.loadClass(anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return this.getClass().getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        });
        when(bundle.getHeaders()).thenReturn(new Hashtable<String, String>());
    }

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
        String[] interfaceNames = new String[interfazes.length];
        for (int i = 0; i < interfazes.length; i++) {
            interfaceNames[i] = interfazes[i].getCanonicalName();
        }
        registerService(service, props, interfaceNames);
    }

    /**
     * registers the service with the given properties under the given interfaces
     */
    protected ServiceReference<?> registerService(Object service, Dictionary<String, Object> props,
            String... interfazes) {
        return registerServiceInBundlecontext(interfazes, service, props).getReference();
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
        props.put(Constants.SERVICE_PID, id);
        registerService(service, props, interfaces);
    }

    protected void registerServiceViaId(Object service, String id, Class<?>... interfaces) {
        registerService(service, id, interfaces);
    }

    private <T> ServiceReference<T> putService(T service, Dictionary<String, Object> props) {
        @SuppressWarnings("unchecked")
        ServiceReference<T> serviceReference = mock(ServiceReference.class);
        long serviceId = --this.serviceId;
        LOGGER.info("registering service with ID: " + serviceId);
        props.put(Constants.SERVICE_ID, serviceId);
        services.put(serviceReference, service);
        synchronized (serviceReference) {
            serviceReferences.put(serviceReference, props);
        }
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
                @SuppressWarnings("unchecked")
                Collection<String> typedCollection = CollectionUtils.typedCollection(list, String.class);
                return typedCollection.toArray(new String[0]);
            }
        });
        return serviceReference;
    }

    /**
     * creates a mock of {@link ConnectorInstanceFactory} for the given connectorType and domains.
     *
     * Only {@link ConnectorInstanceFactory#createNewInstance(String)} is mocked to return a {@link Connector}-mock that
     * contains the given String as id.
     *
     * Also the factory is registered as a service with the required properties
     */
    protected ConnectorInstanceFactory createFactoryMock(String connector,
            final Class<? extends Connector> connectorClass,
            String... domains) throws Exception {
        ConnectorInstanceFactory factory = mock(ConnectorInstanceFactory.class);
        when(factory.createNewInstance(anyString())).thenAnswer(new Answer<Connector>() {
            @Override
            public Connector answer(InvocationOnMock invocation) throws Throwable {
                Connector result = mock(connectorClass);
                String id = (String) invocation.getArguments()[0];
                when(result.getInstanceId()).thenReturn(id);
                return result;
            }
        });
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(org.openengsb.core.api.Constants.CONNECTOR_KEY, connector);
        props.put(org.openengsb.core.api.Constants.DOMAIN_KEY, domains);
        registerService(factory, props, ConnectorInstanceFactory.class);
        return factory;
    }

    /**
     * creates a DomainProvider with for the given interface and name. This creates a mock of {@link DomainProvider}
     * where all String-methods return the name again.
     *
     * Also the service is registered with the mocked service-registry with the given name as domain-value
     */
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
        props.put(org.openengsb.core.api.Constants.DOMAIN_KEY, name);
        registerService(domainProviderMock, props, DomainProvider.class);
        return domainProviderMock;
    }

    /**
     * creates a {@link LocalizableString} that returns the given value for all {@link Locale}s
     */
    protected LocalizableString mockLocalizeableString(String value) {
        LocalizableString mock2 = mock(LocalizableString.class);
        when(mock2.getString(any(Locale.class))).thenReturn(value);
        return mock2;
    }

    /**
     * creates a ConnectorProvider with for the given connectorType and domains. This creates a mock of
     * {@link ConnectorProvider} that returns a descriptor-mock when calling {@link ConnectorProvider#getDescriptor()}
     * Also the service is registered with the mocked service-registry
     */
    protected ConnectorProvider createConnectorProviderMock(String connectorType, String... domains) {
        ConnectorProvider connectorProvider = mock(ConnectorProvider.class);
        when(connectorProvider.getId()).thenReturn(connectorType);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(org.openengsb.core.api.Constants.CONNECTOR_KEY, connectorType);
        props.put(org.openengsb.core.api.Constants.DOMAIN_KEY, domains);
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

    @SuppressWarnings("unchecked")
    private ServiceRegistration<?> registerServiceInBundlecontext(String[] clazzes, final Object service,
            Dictionary<String, Object> dict) {
        dict.put(Constants.OBJECTCLASS, clazzes);
        final ServiceReference<?> serviceReference = putService(service, dict);
        ServiceRegistration<?> result = mock(ServiceRegistration.class);

        // unregistering removes the service from both maps
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                synchronized (serviceReferences) {
                    services.remove(serviceReference);
                    Dictionary<String, Object> props = serviceReferences.remove(serviceReference);
                    updateServiceListeners(ServiceEvent.UNREGISTERING, serviceReference, props);
                    return null;
                }
            }
        }).when(result).unregister();

        // when properties are replaced, place a copy of the new properties-dictionary in the
        // serviceReferences-map (that overwrites the old dictionary)
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
                synchronized (serviceReferences) {
                    serviceReferences.put(serviceReference, newDict);
                }
                return null;
            }
        }).when(result).setProperties(any(Dictionary.class));

        when(result.getReference()).thenAnswer(new ValueAnswer<ServiceReference<?>>(serviceReference));
        updateServiceListeners(ServiceEvent.REGISTERED, serviceReference, dict);
        return result;
    }

    public void updateServiceListeners(int eventType, final ServiceReference<?> serviceReference,
            Dictionary<String, Object> dict) {
        synchronized (listeners) {
            for (Entry<ServiceListener, Filter> entry : listeners.entrySet()) {
                Filter filter = entry.getValue();
                if (filter == null || filter.match(dict)) {
                    entry.getKey().serviceChanged(new ServiceEvent(eventType, serviceReference));
                }
            }
        }
    }

    protected <T> ServiceList<T> makeServiceList(Class<T> serviceClass) {
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, serviceClass.getName(), null);
        ServiceList<T> serviceList = new ServiceList<T>(serviceTracker);
        return serviceList;
    }

    protected <T> ServiceReferenceList<T> makeServiceReferenceList(Class<T> serviceClass) {
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, serviceClass.getName(), null);
        return new ServiceReferenceList<T>(serviceTracker);
    }

}
