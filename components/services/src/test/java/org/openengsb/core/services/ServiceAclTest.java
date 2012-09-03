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

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;

import org.aopalliance.intercept.MethodInvocation;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openengsb.connector.serviceacl.ServicePermission;
import org.openengsb.connector.serviceacl.internal.ServiceAclServiceImpl;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.core.services.internal.security.AdminAccessConnector;
import org.openengsb.core.services.internal.security.AffirmativeBasedAuthorizationStrategy;
import org.openengsb.core.services.internal.security.SecurityInterceptor;
import org.openengsb.core.services.internal.security.model.RootPermission;
import org.openengsb.core.services.internal.virtual.CompositeConnectorProvider;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.core.test.rules.DedicatedThread;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;

public class ServiceAclTest extends AbstractOsgiMockServiceTest {

    private AuthorizationDomain accessControl;
    private AuthorizationDomain servicePermissionAccessConnector;
    private UserDataManager userManager;
    private SecurityAttributeProviderImpl attributeStore;

    @Rule
    public DedicatedThread dedicatedThread = new DedicatedThread();

    @Before
    public void setUp() throws Exception {
        ContextHolder.get().setCurrentContextId("foo");

        userManager = new UserManagerStub();
        userManager.createUser("admin");
        userManager.setUserCredentials("admin", "password", "password");
        userManager.addPermissionToUser("admin", new RootPermission());

        userManager.createUser("testuser");

        AdminAccessConnector adminAccessConnector = new AdminAccessConnector();
        adminAccessConnector.setUserManager(userManager);

        registerServiceAtLocation(adminAccessConnector, "authorization/admin", AuthorizationDomain.class);

        attributeStore = new SecurityAttributeProviderImpl();

        ServiceAclServiceImpl serviceAclServiceImpl = new ServiceAclServiceImpl(userManager, Collections
            .singletonList((SecurityAttributeProvider) attributeStore));

        servicePermissionAccessConnector = serviceAclServiceImpl;

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

        SecurityInterceptor interceptor = new SecurityInterceptor();
        interceptor.setAuthorizer(accessControl);
    }

    @Test
    public void testCheckAdminAccess_shouldGrant() throws Exception {
        MethodInvocation invocation = MethodInvocationUtils.create(new Object(), "toString");
        assertThat(accessControl.checkAccess("admin", invocation), Matchers.is(Access.GRANTED));
    }

    @Test
    public void testCheckServiceAccess_shouldGrant() throws Exception {
        NullDomainImpl nullDomainImpl = new NullDomainImpl("foo");
        MethodInvocation invocation = MethodInvocationUtils.create(nullDomainImpl, "nullMethod");

        userManager.addPermissionToUser("testuser", new ServicePermission(NullDomain.class.getName()));

        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.GRANTED));
    }

    @Test
    public void testCheckServiceAccessByName_shouldGrant() throws Exception {
        NullDomainImpl nullDomainImpl = new NullDomainImpl("foo");
        MethodInvocation invocation = MethodInvocationUtils.create(nullDomainImpl, "nullMethod");

        userManager.addPermissionToUser("testuser", new ServicePermission("NULL"));

        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.GRANTED));
    }

    @Test
    public void testCheckMethodAccessByName_shouldGrant() throws Exception {
        NullDomainImpl nullDomainImpl = new NullDomainImpl("foo");
        MethodInvocation invocation = MethodInvocationUtils.create(nullDomainImpl, "nullMethod", new Object());

        userManager.addPermissionToUser("testuser", new ServicePermission("NULL", "READ_NULL"));

        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.GRANTED));
    }

    @Test
    public void testCheckMethodAccessByWrongName_shouldDeny() throws Exception {
        NullDomainImpl nullDomainImpl = new NullDomainImpl("foo");
        MethodInvocation invocation = MethodInvocationUtils.create(nullDomainImpl, "nullMethod", new Object());

        userManager.addPermissionToUser("testuser", new ServicePermission("NULL", "GET_NULL"));

        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.ABSTAINED));
    }

    @Test
    public void testCheckMethodAccessByMethodName_shouldGrant() throws Exception {
        NullDomainImpl nullDomainImpl = new NullDomainImpl("foo");
        MethodInvocation invocation = MethodInvocationUtils.create(nullDomainImpl, "nullMethod", new Object());

        userManager.addPermissionToUser("testuser", new ServicePermission("NULL", "nullMethod"));

        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.GRANTED));
    }

    @Test
    public void testCheckServiceInstanceAccessById_shouldGrant() throws Exception {
        NullDomainImpl nullDomainImpl = new NullDomainImpl("foo");
        MethodInvocation invocation = MethodInvocationUtils.create(nullDomainImpl, "nullMethod", new Object());

        attributeStore.putAttribute(nullDomainImpl, new SecurityAttributeEntry("name", "service.foo"));

        ServicePermission permission = new ServicePermission();
        permission.setInstance("service.foo");
        userManager.addPermissionToUser("testuser", permission);

        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.GRANTED));
    }

    @Test
    public void testCheckMethodPublicAccess_shouldGrant() throws Exception {
        NullDomainImpl nullDomainImpl = new NullDomainImpl("foo");
        MethodInvocation invocation = MethodInvocationUtils.create(nullDomainImpl, "getInstanceId");

        assertThat(accessControl.checkAccess("testuser", invocation), Matchers.is(Access.GRANTED));
    }
}
