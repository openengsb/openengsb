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

package org.openengsb.core.services.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;

public class ConfigPersistenceServiceFactoryTest {

    private Dictionary<String, String> properties;
    private ConfigPersistenceServiceFactory serviceFactory;
    private OsgiUtilsService serviceUtils;
    private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;

    @SuppressWarnings("rawtypes")
    @Before
    public void setUp() throws Exception {
        properties = new Hashtable<String, String>();
        properties.put(Constants.BACKEND_ID, "backendService");
        properties.put(Constants.CONFIGURATION_ID, "configurationId");

        Filter filterMock = mock(Filter.class);

        ConfigPersistenceBackendService configPersistenceBackendServiceMock =
            mock(ConfigPersistenceBackendService.class);

        serviceUtils = mock(OsgiUtilsService.class);
        when(
            serviceUtils.makeFilter(ConfigPersistenceBackendService.class,
                String.format("(%s=%s)", Constants.BACKEND_ID, "backendService"))).thenReturn(filterMock);
        when(serviceUtils.getOsgiServiceProxy(filterMock, ConfigPersistenceBackendService.class)).thenReturn(
            configPersistenceBackendServiceMock);

        bundleContext = mock(BundleContext.class);

        serviceRegistration = mock(ServiceRegistration.class);
        when(bundleContext.registerService(eq(ConfigPersistenceService.class.getName()),
            any(DefaultConfigPersistenceService.class), argThat(new BaseMatcher<Dictionary>() {
                @Override
                public boolean matches(Object arg0) {
                    return ((Dictionary) arg0).get(Constants.CONFIGURATION_ID).equals(
                        properties.get(Constants.CONFIGURATION_ID));
                }

                @Override
                public void describeTo(Description arg0) {
                }
            }))).thenReturn(serviceRegistration);

        serviceFactory = new ConfigPersistenceServiceFactory();
        serviceFactory.setServiceUtils(serviceUtils);
        serviceFactory.setBundleContext(bundleContext);
    }

    @Test(expected = ConfigurationException.class)
    public void testNonExistingBackendId_shouldThrowConfigurationException() throws Exception {
        properties.remove(Constants.BACKEND_ID);
        serviceFactory.updated("anyPid", properties);
    }

    @Test(expected = ConfigurationException.class)
    public void testNonExisting_shouldThrowConfigurationException() throws Exception {
        properties.remove(Constants.CONFIGURATION_ID);
        serviceFactory.updated("anyPid", properties);
    }

    @Test(expected = ConfigurationException.class)
    public void testBackendServiceNotExisting_shouldThrowConfigurationException() throws Exception {
        properties.put(Constants.BACKEND_ID, "otherBackend");
        when(serviceUtils.getOsgiServiceProxy((Filter) null, ConfigPersistenceBackendService.class)).thenThrow(
            new OsgiServiceNotAvailableException());
        serviceFactory.updated("anyPid", properties);
    }

    @Test
    public void callingUpdate_shouldRegisterService() throws Exception {
        serviceFactory.updated("anyPid", properties);
        serviceFactory.deleted("anyPid");
        verify(serviceRegistration).unregister();
    }

    @Test
    public void callingDeleteWithDifferentPid_shouldSimplyReturn() throws Exception {
        serviceFactory.updated("anyPid", properties);
        serviceFactory.deleted("otherPid");
        verify(serviceRegistration, times(0)).unregister();
    }

}
