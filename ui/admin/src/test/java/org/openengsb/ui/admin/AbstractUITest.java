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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorRegistrationManager;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.core.services.internal.ConnectorRegistrationManagerImpl;
import org.openengsb.core.services.internal.DefaultConfigPersistenceService;
import org.openengsb.core.services.internal.DefaultWiringService;
import org.openengsb.core.services.internal.persistence.connector.ConnectorJPAPersistenceBackendService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.ui.admin.model.OpenEngSBFallbackVersion;
import org.openengsb.ui.api.OpenEngSBVersionService;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;
import org.osgi.framework.BundleContext;

/**
 * abstract baseclass for OpenEngSB-UI-page-tests it creates a wicket-tester that handles the Dependency-injection via a
 * mocked ApplicationContext. Many required services are already mocked in placed in the ApplicationContext.
 * 
 * new beans can always be introduced by inserting them into the ApplicationContext represendted by the
 * "context"-variable
 */
public class AbstractUITest extends AbstractOsgiMockServiceTest {

    protected OsgiUtilsService serviceUtils;
    protected WicketTester tester;
    protected ApplicationContextMock context;
    protected ConnectorManager serviceManager;
    protected ConnectorRegistrationManager registrationManager;
    protected WiringService wiringService;
    private EntityManager em;
    private EntityTransaction tx;
    private final static File dbFile = new File("TEST.h2.db");

    @Before
    public void makeContextMock() throws Exception {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
        context.putBean(Mockito.mock(ContextCurrentService.class));
        context.putBean("openengsbVersion", new OpenEngSBFallbackVersion());
        List<OpenEngSBVersionService> versionService = new ArrayList<OpenEngSBVersionService>();
        context.putBean("openengsbVersionService", versionService);
        context.putBean(OpenEngSBCoreServices.getWiringService());
        OsgiUtilsService serviceUtilsService =
            OpenEngSBCoreServices.getServiceUtilsService().getOsgiServiceProxy(OsgiUtilsService.class);
        context.putBean("serviceUtils", serviceUtilsService);
        ConnectorRegistrationManagerImpl registrationManager = new ConnectorRegistrationManagerImpl();
        registrationManager.setBundleContext(bundleContext);
        registrationManager.setServiceUtils(serviceUtils);
        ConnectorManagerImpl serviceManager = new ConnectorManagerImpl();
        serviceManager.setRegistrationManager(registrationManager);

        registerConfigPersistence();
        tx = em.getTransaction();
        tx.begin();

        this.registrationManager = registrationManager;
        this.serviceManager = serviceManager;
        context.putBean(serviceManager);
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        this.serviceUtils = serviceUtils;
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        DefaultWiringService wiringService = new DefaultWiringService();
        wiringService.setBundleContext(bundleContext);
        registerService(wiringService, "wiringService", WiringService.class);
        this.wiringService = wiringService;
    }

    private void registerConfigPersistence() {
        final ConnectorJPAPersistenceBackendService persistenceBackend = new ConnectorJPAPersistenceBackendService();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("connector-test");
        em = emf.createEntityManager();
        persistenceBackend.setEntityManager(em);
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.CONFIGURATION_ID, Constants.CONFIG_CONNECTOR);
        props.put(Constants.BACKEND_ID, "dummy");
        registerService(new DefaultConfigPersistenceService(persistenceBackend), props, ConfigPersistenceService.class);
    }

    @BeforeClass
    @AfterClass
    public static void deleteDB() throws IOException {
        if (dbFile.exists()) {
            FileUtils.forceDelete(dbFile);
        }
    }

    @After
    public void tearDown() throws IOException {
        try {
            tx.commit();
        } catch (Exception ex) {
            // Do nothing the db will get destroyed either way.
        }

        em.close();
        deleteDB();
    }
}
