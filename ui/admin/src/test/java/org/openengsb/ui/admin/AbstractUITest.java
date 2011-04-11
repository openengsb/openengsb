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

package org.openengsb.ui.admin;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.mockito.Mockito;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ServiceManager;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.common.CorePersistenceServiceBackend;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.services.internal.DefaultConfigPersistenceService;
import org.openengsb.core.services.internal.DefaultWiringService;
import org.openengsb.core.services.internal.ServiceManagerImpl;
import org.openengsb.core.services.internal.ServiceRegistrationManagerImpl;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistenceManager;
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.osgi.framework.BundleContext;

public class AbstractUITest extends AbstractOsgiMockServiceTest {

    protected OsgiUtilsService serviceUtils;
    protected WicketTester tester;
    protected ApplicationContextMock context;
    protected ServiceManager serviceManager;
    protected InternalServiceRegistrationManager registrationManager;
    protected WiringService wiringService;

    @Before
    public void makeContextMock() throws Exception {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(Mockito.mock(ContextCurrentService.class));
        context.putBean("openengsbVersion", new OpenEngSBVersion());
        context.putBean(OpenEngSBCoreServices.getWiringService());
        ServiceRegistrationManagerImpl registrationManager = new ServiceRegistrationManagerImpl();
        registrationManager.setBundleContext(bundleContext);
        ServiceManagerImpl serviceManager = new ServiceManagerImpl();
        serviceManager.setRegistrationManager(registrationManager);

        CorePersistenceServiceBackend backend = new CorePersistenceServiceBackend();
        backend.setPersistenceManager(new DummyPersistenceManager());
        backend.setBundleContext(bundleContext);
        backend.init();
        DefaultConfigPersistenceService persistenceService = new DefaultConfigPersistenceService(backend);
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("configuration.id", Constants.CONNECTOR);
        registerService(persistenceService, props, ConfigPersistenceService.class);

        // (&(objectClass=org.openengsb.core.api.persistence.ConfigPersistenceService)(configuration.id=connector))

        this.registrationManager = registrationManager;
        this.serviceManager = serviceManager;
        context.putBean(serviceManager);
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        this.serviceUtils = serviceUtils;
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        DefaultWiringService wiringService = new DefaultWiringService();
        wiringService.setBundleContext(bundleContext);
        registerService(wiringService, "wiringService", WiringService.class);
        this.wiringService = wiringService;
    }

}
