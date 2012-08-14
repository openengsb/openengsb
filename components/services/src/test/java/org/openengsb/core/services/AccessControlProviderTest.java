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
package org.openengsb.core.services;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Hashtable;

import org.aopalliance.intercept.MethodInvocation;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.virtual.CompositeConnectorProvider;
import org.openengsb.core.services.internal.security.AdminAccessConnector;
import org.openengsb.core.services.internal.security.AffirmativeBasedAuthorizationStrategy;
import org.openengsb.core.services.internal.security.model.RootPermission;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.core.test.rules.DedicatedThread;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;

public class AccessControlProviderTest extends AbstractOsgiMockServiceTest {

    @Rule
    public DedicatedThread dedicatedThread = new DedicatedThread();

    private AuthorizationDomain accessControl;
    private AuthorizationDomain servicePermissionAccessConnector;

    @Before
    public void setUp() throws Exception {
        ContextHolder.get().setCurrentContextId("foo");

        UserDataManager userManager = new UserManagerStub();
        userManager.createUser("admin");
        userManager.setUserCredentials("admin", "password", "password");
        userManager.addPermissionToUser("admin", new RootPermission());

        userManager.createUser("testuser");

        AdminAccessConnector adminAccessConnector = new AdminAccessConnector();
        adminAccessConnector.setUserManager(userManager);

        registerServiceAtLocation(adminAccessConnector, "authorization/admin", AuthorizationDomain.class);

        servicePermissionAccessConnector = mock(AuthorizationDomain.class);
        when(servicePermissionAccessConnector.checkAccess(anyString(), any(MethodInvocation.class))).thenReturn(
            Access.ABSTAINED);
        registerServiceAtLocation(servicePermissionAccessConnector, "authorization/service", AuthorizationDomain.class);
        AffirmativeBasedAuthorizationStrategy strategy = new AffirmativeBasedAuthorizationStrategy();
        strategy.setUtilsService(new DefaultOsgiUtilsService(bundleContext));

        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("composite.strategy.name", "accessControlStrategy");
        registerService(strategy, props, CompositeConnectorStrategy.class);

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("compositeStrategy", "accessControlStrategy");
        attributes.put("queryString", "(location.foo=authorization/*)");

        DomainProvider provider = createDomainProviderMock(AuthorizationDomain.class, "accessControl");
        CompositeConnectorProvider compositeConnectorProvider = new CompositeConnectorProvider();
        compositeConnectorProvider.setBundleContext(bundleContext);
        ConnectorInstanceFactory factory = compositeConnectorProvider.createFactory(provider);
        accessControl = (AuthorizationDomain) factory.createNewInstance("authProvider");

        factory.applyAttributes((Connector) accessControl, attributes);
    }

    @Test
    public void checkAdminAccess_shouldGrant() throws Exception {
        MethodInvocation invocation = mock(MethodInvocation.class);
        assertThat(accessControl.checkAccess("admin", invocation), Matchers.is(Access.GRANTED));
    }

    @Test
    public void checkServiceAccess_shouldGrant() throws Exception {
        OpenEngSBService service = mock(OpenEngSBService.class);
        when(service.getInstanceId()).thenReturn("fooService");
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getThis()).thenReturn(service);
        when(servicePermissionAccessConnector.checkAccess("testuser", invocation)).thenReturn(Access.GRANTED);
        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.GRANTED));
    }
}
