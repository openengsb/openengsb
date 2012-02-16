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

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.openengsb.connector.usernamepassword.internal.UsernamePasswordServiceImpl;
import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.connector.wicketacl.internal.WicketAclServiceImpl;
import org.openengsb.connector.wicketacl.internal.WicketPermissionProvider;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorRegistrationManager;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.security.PermissionProvider;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.virtual.CompositeConnectorProvider;
import org.openengsb.core.persistence.internal.CorePersistenceServiceBackend;
import org.openengsb.core.persistence.internal.DefaultConfigPersistenceService;
import org.openengsb.core.security.internal.AdminAccessConnector;
import org.openengsb.core.security.internal.AffirmativeBasedAuthorizationStrategy;
import org.openengsb.core.security.internal.model.RootPermission;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.core.services.internal.ConnectorRegistrationManagerImpl;
import org.openengsb.core.services.internal.DefaultWiringService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistenceManager;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.ui.admin.model.OpenEngSBFallbackVersion;
import org.openengsb.ui.api.OpenEngSBVersionService;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;
import org.osgi.framework.BundleContext;

import com.google.common.collect.ImmutableMap;

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
    protected ContextCurrentService contextCurrentService;
    protected UserDataManager userManager;
    protected UsernamePasswordServiceImpl authConnector;

    @Before
    public void makeContextMock() throws Exception {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
        contextCurrentService = mock(ContextCurrentService.class);
        context.putBean("contextCurrentService", contextCurrentService);
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

        CorePersistenceServiceBackend<String> backend = new CorePersistenceServiceBackend<String>();
        backend.setPersistenceManager(new DummyPersistenceManager());
        backend.setBundleContext(bundleContext);
        backend.init();
        DefaultConfigPersistenceService persistenceService = new DefaultConfigPersistenceService(backend);

        serviceManager.setConfigPersistence(persistenceService);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("configuration.id", Constants.CONFIG_CONNECTOR);
        registerService(persistenceService, props, ConfigPersistenceService.class);

        this.registrationManager = registrationManager;
        this.serviceManager = serviceManager;
        context.putBean(serviceManager);

        userManager = new UserManagerStub();
        userManager.createUser("test");
        userManager.setUserCredentials("test", "password", "password");
        userManager.addPermissionToUser("test", new WicketPermission("USER"));

        userManager.createUser("user");
        userManager.setUserCredentials("user", "password", "password");

        userManager.createUser("admin");
        userManager.setUserCredentials("admin", "password", "password");
        userManager.addPermissionToUser("admin", new RootPermission());
        context.putBean("userManager", userManager);

        Dictionary<String, Object> wicketProviderProps = new Hashtable<String, Object>();
        wicketProviderProps.put("permissionClass", WicketPermission.class.getName());
        WicketPermissionProvider wicketPermissionProvider = new WicketPermissionProvider();
        registerService(wicketPermissionProvider, wicketProviderProps, PermissionProvider.class);

        context.putBean("permissionProviders", Arrays.asList(wicketPermissionProvider));

        SecurityAttributeProvider attributeStore = new SecurityAttributeProviderImpl();
        context.putBean("attributeStore", attributeStore);
        context.putBean("attributeProviders", Collections.singletonList(attributeStore));
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

    protected void mockAuthentication() throws UserNotFoundException, UserExistsException {

        authConnector = new UsernamePasswordServiceImpl();
        authConnector.setUserManager(userManager);
        context.putBean("authenticator", authConnector);

        WicketAclServiceImpl wicketAclServiceImpl = new WicketAclServiceImpl();
        wicketAclServiceImpl.setUserManager(userManager);
        registerServiceAtLocation(wicketAclServiceImpl, "authorization/wicket", "root", AuthorizationDomain.class,
            Domain.class);

        AdminAccessConnector adminAccessConnector = new AdminAccessConnector();
        adminAccessConnector.setUserManager(userManager);
        registerServiceAtLocation(adminAccessConnector, "authorization/admin", "root", AuthorizationDomain.class,
            Domain.class);

        DomainProvider authDomainProvider = createDomainProviderMock(AuthorizationDomain.class, "authorization");
        ConnectorInstanceFactory cFactory = new CompositeConnectorProvider().createFactory(authDomainProvider);
        Connector instance = cFactory.createNewInstance("auth-admin");
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("composite.strategy.name", "authorization");
        registerService(new AffirmativeBasedAuthorizationStrategy(), props, CompositeConnectorStrategy.class);

        cFactory.applyAttributes(instance,
            ImmutableMap.of("compositeStrategy", "authorization", "queryString", "(location.root=authorization/*)"));
        registerServiceAtLocation(instance, "authorization-root", "root", AuthorizationDomain.class, Domain.class);
        context.putBean("authorizer", instance);

        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        OpenEngSBShiroAuthenticator authenticator = new OpenEngSBShiroAuthenticator();
        authenticator.setAuthenticator(authConnector);
        defaultWebSecurityManager.setAuthenticator(authenticator);
        context.putBean("securityManager", defaultWebSecurityManager);
    }

}
