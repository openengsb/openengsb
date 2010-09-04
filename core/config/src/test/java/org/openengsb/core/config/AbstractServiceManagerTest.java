/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.core.config;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.core.config.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.config.util.BundleStrings;
import org.openengsb.core.config.util.BundleStringsTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class AbstractServiceManagerTest {

    private static interface DummyDomain extends Domain {
    }

    private static class DummyInstance implements DummyDomain {
    }

    private static class DummyServiceManager extends AbstractServiceManager<DummyDomain, DummyInstance> {

        public DummyServiceManager(final DummyInstance instance) {
            super(new ServiceInstanceFactory<AbstractServiceManagerTest.DummyDomain, AbstractServiceManagerTest.DummyInstance>() {

                @Override
                public void updateServiceInstance(DummyInstance instance, Map<String, String> attributes) {
                }

                @Override
                public ServiceDescriptor getDescriptor(Builder builder, Locale locale, BundleStrings strings) {
                    builder.implementationType(DummyInstance.class);
                    builder.serviceType(DummyDomain.class);
                    builder.name("abstract.name");
                    builder.description("abstract.description");
                    return builder.build();
                }

                @Override
                public DummyInstance createServiceInstance(String id, Map<String, String> attributes) {
                    return instance;
                }
            });
        }
    }

    @Test
    public void testInterfaceGetters() {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        DummyServiceManager manager = createDummyManager(bundleContextMock, new DummyInstance());
        Assert.assertEquals(DummyDomain.class, manager.getDomainInterface());
        Assert.assertEquals(DummyInstance.class, manager.getImplementationClass());
    }

    @Test
    public void testGetDescriptor() {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        DummyServiceManager manager = createDummyManager(bundleContextMock, new DummyInstance());

        ServiceDescriptor descriptor = manager.getDescriptor(Locale.ENGLISH);

        Assert.assertEquals(DummyInstance.class.getName(), descriptor.getId());
        Assert.assertEquals(DummyDomain.class, descriptor.getServiceType());
        Assert.assertEquals(DummyInstance.class, descriptor.getImplementationType());
    }

    @Test
    public void testAddNewOne() {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        HashMap<String, String> attributes = new HashMap<String, String>();
        DummyInstance instance = new DummyInstance();

        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);

        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.verify(bundleContextMock).registerService(
                new String[] { DummyInstance.class.getName(), DummyDomain.class.getName(), Domain.class.getName() },
                instance, props);
    }

    @Test
    public void testUpdateExistingOne() {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        HashMap<String, String> attributes = new HashMap<String, String>();

        DummyInstance instance = new DummyInstance();
        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);
        HashMap<String, String> verificationAttributes = new HashMap<String, String>();
        manager.update("test", verificationAttributes);

        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.verify(bundleContextMock, Mockito.times(1)).registerService(
                new String[] { DummyInstance.class.getName(), DummyDomain.class.getName(), Domain.class.getName() },
                instance, props);
    }

    @Test
    public void testDeleteService() {
        BundleContext bundleContextMock = mockBundleContextForServiceManager();
        HashMap<String, String> attributes = new HashMap<String, String>();
        DummyInstance instance = new DummyInstance();
        ServiceRegistration serviceRegistrationMock = appendServiceRegistrationMockToBundleContextMock(
                bundleContextMock, instance);

        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);
        manager.delete("test");

        Mockito.verify(serviceRegistrationMock).unregister();
    }

    private ServiceRegistration appendServiceRegistrationMockToBundleContextMock(BundleContext bundleContextMock,
            DummyInstance mock) {
        ServiceRegistration serviceRegistrationMock = Mockito.mock(ServiceRegistration.class);
        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.when(
                bundleContextMock.registerService(
                        new String[] { DummyInstance.class.getName(), DummyDomain.class.getName(),
                                Domain.class.getName() }, mock, props)).thenReturn(serviceRegistrationMock);
        return serviceRegistrationMock;
    }

    private Hashtable<String, String> createVerificationHashmap() {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("id", "test");
        props.put("domain", DummyDomain.class.getName());
        props.put("class", DummyInstance.class.getName());
        return props;
    }

    private DummyServiceManager createDummyManager(BundleContext bundleContextMock, DummyInstance instance) {
        DummyServiceManager manager = new DummyServiceManager(instance);
        manager.setBundleContext(bundleContextMock);
        return manager;
    }

    private BundleContext mockBundleContextForServiceManager() {

        Bundle bundleMock = Mockito.mock(Bundle.class);
        BundleStringsTest.mockHeaders(bundleMock);
        BundleStringsTest.mockFindEntries(bundleMock);

        BundleContext bundleContextMock = Mockito.mock(BundleContext.class);
        Mockito.when(bundleContextMock.getBundle()).thenReturn(bundleMock);
        return bundleContextMock;
    }
}
