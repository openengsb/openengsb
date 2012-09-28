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
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.openengsb.connector.usernamepassword.internal.UsernamePasswordServiceImpl;
import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.connector.wicketacl.internal.WicketAclServiceImpl;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.core.persistence.internal.DefaultConfigPersistenceService;
import org.openengsb.core.services.OpenEngSBShiroAuthenticator;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.core.services.internal.ConnectorRegistrationManager;
import org.openengsb.core.services.internal.DefaultWiringService;
import org.openengsb.core.services.internal.security.AdminAccessConnector;
import org.openengsb.core.services.internal.security.AffirmativeBasedAuthorizationStrategy;
import org.openengsb.core.services.internal.security.model.RootPermission;
import org.openengsb.core.services.internal.security.model.ShiroContext;
import org.openengsb.core.services.internal.virtual.CompositeConnectorProvider;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyConfigPersistenceService;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;
import org.openengsb.ui.admin.model.OpenEngSBFallbackVersion;
import org.openengsb.ui.api.OpenEngSBVersionService;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

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
    protected PaxWicketSpringBeanComponentInjector defaultPaxWicketInjector;
    protected AuditingDomain auditingDomain;
    protected RuleManager ruleManager;

    @Before
    public void makeContextMock() throws Exception {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        defaultPaxWicketInjector = new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context);
        tester.getApplication().getComponentInstantiationListeners().add(defaultPaxWicketInjector);

        context.putBean("blueprintBundleContext", bundleContext);
        context.putBean("blueprintBundle", bundle);
        contextCurrentService = mock(ContextCurrentService.class);
        context.putBean("contextCurrentService", contextCurrentService);
        context.putBean("openengsbVersion", new OpenEngSBFallbackVersion());
        List<OpenEngSBVersionService> versionService = new ArrayList<OpenEngSBVersionService>();
        context.putBean("openengsbVersionService", versionService);
        DefaultWiringService defaultWiringService = new DefaultWiringService();
        defaultWiringService.setBundleContext(bundleContext);
        context.putBean("wiringService", defaultWiringService);
        serviceUtils = new DefaultOsgiUtilsService(bundleContext);
        context.putBean("osgiUtilsService", serviceUtils);
        ConnectorRegistrationManager registrationManager = new ConnectorRegistrationManager();
        registrationManager.setBundleContext(bundleContext);
        ConnectorManagerImpl serviceManager = new ConnectorManagerImpl();
        serviceManager.setRegistrationManager(registrationManager);

        DummyConfigPersistenceService<String> backend = new DummyConfigPersistenceService<String>();
        DefaultConfigPersistenceService persistenceService = new DefaultConfigPersistenceService(backend);

        serviceManager.setConfigPersistence(persistenceService);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("configuration.id", Constants.CONFIG_CONNECTOR);
        registerService(persistenceService, props, ConfigPersistenceService.class);

        this.registrationManager = registrationManager;
        this.serviceManager = serviceManager;
        context.putBean("serviceManager", serviceManager);

        userManager = new UserManagerStub();
        userManager.createUser("test");
        userManager.setUserCredentials("test", "password", "password");
        userManager.addPermissionToUser("test", new WicketPermission("USER"));
        userManager.addPermissionToUser("test", new WicketPermission("INDEX"));

        userManager.createUser("user");
        userManager.setUserCredentials("user", "password", "password");
        userManager.addPermissionToUser("user", new WicketPermission("INDEX"));

        userManager.createUser("admin");
        userManager.setUserCredentials("admin", "password", "password");
        userManager.addPermissionToUser("admin", new RootPermission());
        context.putBean("userManager", userManager);

        Dictionary<String, Object> wicketProviderProps = new Hashtable<String, Object>();
        wicketProviderProps.put(org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_KEY,
            WicketPermission.class.getName());
        wicketProviderProps.put(org.openengsb.labs.delegation.service.Constants.DELEGATION_CONTEXT_KEY,
            Constants.DELEGATION_CONTEXT_PERMISSIONS);
        ClassProvider wicketPermissionProvider =
            new ClassProviderImpl(bundle, Sets.newHashSet(WicketPermission.class.getName()));
        registerService(wicketPermissionProvider, wicketProviderProps, ClassProvider.class);

        SecurityAttributeProvider attributeStore = new SecurityAttributeProviderImpl();
        context.putBean("attributeStore", attributeStore);
        context.putBean("attributeProviders", Collections.singletonList(attributeStore));

        context.putBean("domainProviders", makeServiceList(DomainProvider.class));
        context.putBean("permissionProviders", makeServiceList(ClassProvider.class));

        context.putBean("connectorList", makeServiceReferenceList(Domain.class));
        auditingDomain = mock(AuditingDomain.class);
        context.putBean("auditing", auditingDomain);
        ruleManager = mock(RuleManager.class);
        context.putBean("ruleManager", ruleManager);
    }

    protected void mockAuthentication() throws UserNotFoundException, UserExistsException {
        context.putBean("authenticationContext", new ShiroContext());
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
        CompositeConnectorProvider compositeConnectorProvider = new CompositeConnectorProvider();
        compositeConnectorProvider.setBundleContext(bundleContext);
        ConnectorInstanceFactory cFactory = compositeConnectorProvider.createFactory(authDomainProvider);
        Connector instance = cFactory.createNewInstance("auth-admin");
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("composite.strategy.name", "authorization");
        AffirmativeBasedAuthorizationStrategy service = new AffirmativeBasedAuthorizationStrategy();
        service.setUtilsService(serviceUtils);
        registerService(service, props, CompositeConnectorStrategy.class);

        cFactory.applyAttributes(instance,
            ImmutableMap.of("compositeStrategy", "authorization", "queryString", "(location.root=authorization/*)"));
        registerServiceAtLocation(instance, "authorization-root", "root", AuthorizationDomain.class, Domain.class);
        context.putBean("authorizer", instance);

        OpenEngSBShiroAuthenticator openEngSBShiroAuthenticator = new OpenEngSBShiroAuthenticator();
        openEngSBShiroAuthenticator.setAuthenticator(authConnector);
        DefaultWebSecurityManager webSecurityManager = new DefaultWebSecurityManager();
        webSecurityManager.setAuthenticator(openEngSBShiroAuthenticator);
        context.putBean("webSecurityManager", webSecurityManager);
    }

    protected int getIndexForValue(DropDownChoice<?> component, String valueString) {
        List<?> choices = component.getChoices();
        Iterator<?> iterator = choices.iterator();
        for (int i = 0; i < choices.size(); i++) {
            Object next = iterator.next();
            if (next.toString().contains(valueString)) {
                return i;
            }
        }
        throw new IllegalArgumentException("provided Dropdown did not contain String " + valueString);
    }
}
