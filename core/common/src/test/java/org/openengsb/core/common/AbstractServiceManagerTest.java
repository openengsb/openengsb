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

package org.openengsb.core.common;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.openengsb.core.common.connectorsetupstore.ConnectorSetupStore;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.l10n.BundleStringsTest;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class AbstractServiceManagerTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private static interface DummyDomain extends Domain {
    }

    private static class DummyInstance implements DummyDomain {
        private final AliveState aliveState = AliveState.OFFLINE;

        @Override
        public AliveState getAliveState() {
            return aliveState;
        }
    }

    private static class DummyServiceManager extends AbstractServiceManager<DummyDomain, DummyInstance> {

        public DummyServiceManager(final DummyInstance instance) {
            super(new ServiceInstanceFactory<DummyDomain, DummyInstance>() {

                @Override
                public void updateServiceInstance(DummyInstance instance, Map<String, String> attributes) {
                }

                @Override
                public ServiceDescriptor getDescriptor(Builder builder) {
                    builder.implementationType(DummyInstance.class);
                    builder.serviceType(DummyDomain.class);
                    builder.name("abstract.name");
                    builder.description("abstract.description");
                    builder.id("DummyServiceManager");
                    return builder.build();
                }

                @Override
                public DummyInstance createServiceInstance(String id, Map<String, String> attributes) {
                    return instance;
                }
            });
        }

        @Override
        public MultipleAttributeValidationResult updateWithValidation(String anyString, Map<String, String> anyMap) {
            this.update(anyString, anyMap);
            return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
        }
    }

    @Test
    public void testInterfaceGetters() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        DummyServiceManager manager = createDummyManager(bundleContextMock, new DummyInstance());
        assertThat(manager.getDomainInterface(), sameInstance(DummyDomain.class));
        assertThat(manager.getImplementationClass(), sameInstance(DummyInstance.class));
    }

    @Test
    public void testGetDescriptor() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        DummyServiceManager manager = createDummyManager(bundleContextMock, new DummyInstance());

        ServiceDescriptor descriptor = manager.getDescriptor();

        assertThat(descriptor.getId(), is(DummyServiceManager.class.getSimpleName()));
        assertEquals(descriptor.getServiceType(), DummyDomain.class);
        assertEquals(descriptor.getImplementationType(), DummyInstance.class);
    }

    @Test
    public void testAddNewOne() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        HashMap<String, String> attributes = new HashMap<String, String>();
        DummyInstance instance = new DummyInstance();

        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);

        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.verify(bundleContextMock).registerService(
            new String[]{DummyInstance.class.getName(), DummyDomain.class.getName(), Domain.class.getName()}, instance,
            props);
    }

    @Test
    public void testUpdateExistingOne() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        HashMap<String, String> attributes = new HashMap<String, String>();

        DummyInstance instance = new DummyInstance();
        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);
        HashMap<String, String> verificationAttributes = new HashMap<String, String>();
        manager.update("test", verificationAttributes);

        Hashtable<String, String> props = createVerificationHashmap();
        Mockito.verify(bundleContextMock, Mockito.times(1)).registerService(
            new String[]{DummyInstance.class.getName(), DummyDomain.class.getName(), Domain.class.getName()}, instance,
            props);
    }

    @Test
    public void testDeleteService() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        HashMap<String, String> attributes = new HashMap<String, String>();
        DummyInstance instance = new DummyInstance();
        ServiceRegistration serviceRegistrationMock =
            appendServiceRegistrationMockToBundleContextMock(bundleContextMock, instance);

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
            bundleContextMock.registerService(new String[]{DummyInstance.class.getName(), DummyDomain.class.getName(),
                Domain.class.getName()}, mock, props)).thenReturn(serviceRegistrationMock);
        return serviceRegistrationMock;
    }

    private Hashtable<String, String> createVerificationHashmap() {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("id", "test");
        props.put("domain", DummyDomain.class.getName());
        props.put("class", DummyInstance.class.getName());
        props.put("managerId", DummyServiceManager.class.getSimpleName());
        return props;
    }

    private DummyServiceManager createDummyManager(BundleContext bundleContextMock, DummyInstance instance) {
        DummyServiceManager manager = new DummyServiceManager(instance);
        manager.setBundleContext(bundleContextMock);
        manager.setConnectorSetupStore(Mockito.mock(ConnectorSetupStore.class));
        return manager;
    }

    @Test
    public void testGetAttributeValues() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", "test");
        attributes.put("attribute2", "atr2");

        DummyInstance instance = new DummyInstance();

        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);
        Map<String, String> attributeValues = manager.getAttributeValues("test");
        assertThat(attributes.size(), is(attributeValues.size()));
        assertThat(attributeValues.get("id"), is("test"));
        assertThat(attributeValues.get("attribute2"), is("atr2"));
    }

    @Test
    public void testGetAttributeValuesAfterUpdate() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", "test");
        attributes.put("attribute2", "atr2");

        DummyInstance instance = new DummyInstance();

        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);

        Map<String, String> attributesNew = new HashMap<String, String>();
        attributesNew.put("id", "test");
        attributesNew.put("attribute2", "newAtr2");
        manager.update("test", attributesNew);

        Map<String, String> attributeValues = manager.getAttributeValues("test");
        assertThat(attributes.size(), is(attributeValues.size()));
        assertThat(attributeValues.get("id"), is("test"));
        assertThat(attributeValues.get("attribute2"), is("newAtr2"));
    }

    @Test
    public void testCheckIfDeletedServiceDoesNotHaveAttributeValues() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("does not exist");

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", "test");
        attributes.put("attribute2", "atr2");

        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        DummyInstance instance = new DummyInstance();
        ServiceRegistration serviceRegistrationMock =
            appendServiceRegistrationMockToBundleContextMock(bundleContextMock, instance);

        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);
        manager.delete("test");

        Mockito.verify(serviceRegistrationMock).unregister();

        Map<String, String> attributeValues = manager.getAttributeValues("test");
        assertThat(attributeValues.size(), is(0));
    }

    @Test
    public void testIfUpdateOfSingleAttributesIsPossible() {
        BundleContext bundleContextMock = BundleStringsTest.createBundleContextMockWithBundleStrings();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", "test");
        attributes.put("attribute2", "atr2");

        DummyInstance instance = new DummyInstance();

        DummyServiceManager manager = createDummyManager(bundleContextMock, instance);
        manager.update("test", attributes);

        Map<String, String> attributesNew = new HashMap<String, String>();
        attributesNew.put("attribute2", "newAtr2");
        manager.update("test", attributesNew);

        Map<String, String> attributeValues = manager.getAttributeValues("test");
        assertThat(attributeValues.size(), is(attributes.size()));
        assertThat(attributeValues.get("id"), is("test"));
        assertThat(attributeValues.get("attribute2"), is("newAtr2"));
    }

}
