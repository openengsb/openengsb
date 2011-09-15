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

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.openengsb.connector.usernamepassword.internal.UsernamePasswordServiceImpl;
import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.connector.wicketacl.internal.WicketAclServiceImpl;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.common.virtual.CompositeConnectorProvider;
import org.openengsb.core.security.AdminAccessConnector;
import org.openengsb.core.security.AffirmativeBasedAuthorizationStrategy;
import org.openengsb.core.security.model.RootPermission;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.core.test.rules.DedicatedThread;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.ops4j.pax.wicket.api.ApplicationLifecycleListener;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

import com.google.common.collect.ImmutableMap;

public abstract class AbstractLoginTest extends AbstractUITest {

    protected UserDataManager userManager;

    @Rule
    public MethodRule dedicatedThread = new DedicatedThread();

    @Before
    public void setupLogin() throws Exception {
        mockAuthentication();
        ApplicationLifecycleListener listener = mock(ApplicationLifecycleListener.class);
        tester = new WicketTester(new WicketApplication(listener) {
            @Override
            protected void addInjector() {
                addComponentInstantiationListener(new PaxWicketSpringBeanComponentInjector(this, context));
            }
        });
    }

    private void mockAuthentication() throws UserNotFoundException, UserExistsException {
        userManager = new UserManagerStub();
        userManager.createUser("test");
        userManager.setUserCredentials("test", "password", "password");
        userManager.storeUserPermission("test", new WicketPermission("USER"));

        userManager.createUser("user");
        userManager.setUserCredentials("user", "password", "password");

        userManager.createUser("admin");
        userManager.setUserCredentials("admin", "password", "password");
        userManager.storeUserPermission("admin", new RootPermission());

        UsernamePasswordServiceImpl authConnector = new UsernamePasswordServiceImpl();
        authConnector.setUserManager(userManager);
        context.putBean("authenticationManager", authConnector);

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
        registerServiceAtLocation(instance, "authorization", "root", AuthorizationDomain.class, Domain.class);
    }
}
