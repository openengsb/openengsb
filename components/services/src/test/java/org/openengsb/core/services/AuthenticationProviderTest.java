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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.connector.usernamepassword.internal.UsernamePasswordServiceImpl;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.services.internal.security.DefaultAuthenticationProviderStrategy;
import org.openengsb.core.services.internal.virtual.CompositeConnectorProvider;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.core.test.rules.DedicatedThread;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;

public class AuthenticationProviderTest extends AbstractOsgiMockServiceTest {

    @Rule
    public DedicatedThread dedicatedThread = new DedicatedThread();

    private UsernamePasswordServiceImpl passwordAuthenticator;
    private OnetimePasswordAuthenticator onetimeAuthenticator;
    private AuthenticationDomain authManager;

    @Before
    public void setUp() throws Exception {
        ContextHolder.get().setCurrentContextId("foo");

        UserDataManager userManager = new UserManagerStub();
        userManager.createUser("testuser");
        userManager.setUserCredentials("testuser", "password", "password");

        userManager.setUserCredentials("testuser", "onetime-basecode", "90489");
        userManager.setUserCredentials("testuser", "onetime-counter", "2");

        UsernamePasswordServiceImpl authenticator1 = new UsernamePasswordServiceImpl();
        authenticator1.setUserManager(userManager);
        registerServiceAtLocation(authenticator1, "authenticator/password", AuthenticationDomain.class);
        passwordAuthenticator = authenticator1;

        OnetimePasswordAuthenticator authenticator2 = new OnetimePasswordAuthenticator();
        authenticator2.setUserManager(userManager);
        registerServiceAtLocation(authenticator2, "authenticator/onetime", AuthenticationDomain.class);
        onetimeAuthenticator = authenticator2;

        DomainProvider provider = createDomainProviderMock(AuthenticationDomain.class, "authentication");
        CompositeConnectorProvider compositeConnectorProvider = new CompositeConnectorProvider();
        compositeConnectorProvider.setBundleContext(bundleContext);
        ConnectorInstanceFactory factory = compositeConnectorProvider.createFactory(provider);
        authManager = (AuthenticationDomain) factory.createNewInstance("authProvider");

        DefaultAuthenticationProviderStrategy strategy = new DefaultAuthenticationProviderStrategy();
        strategy.setUtilsService(new DefaultOsgiUtilsService(bundleContext));

        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("composite.strategy.name", "authManagerStrategy");
        registerService(strategy, props, CompositeConnectorStrategy.class);

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("compositeStrategy", "authManagerStrategy");
        attributes.put("queryString", "(location.foo=authenticator/*)");

        factory.applyAttributes((Connector) authManager, attributes);
    }

    @Test
    public void authenticateUsernamePassword_shouldAuthenticateSuccessful() throws Exception {
        Authentication authenticate = passwordAuthenticator.authenticate("testuser", new Password("password"));
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateUsernamePassword_shouldFail() throws Exception {
        passwordAuthenticator.authenticate("testuser", new Password("password2"));
    }

    @Test
    public void authenticateOnetimePassword() throws Exception {
        Authentication authenticate = onetimeAuthenticator.authenticate("testuser", new OneTimeValue(90489 * 2));
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateOnetimePassword_shouldFail() throws Exception {
        onetimeAuthenticator.authenticate("testuser", new OneTimeValue(123));
    }

    @Test
    public void authenticateUsernamePasswordAtManager_shouldAuthenticateSuccessful() throws Exception {
        Authentication authenticate = authManager.authenticate("testuser", new Password("password"));
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateUsernamePasswordAtManager_shouldFail() throws Exception {
        authManager.authenticate("testuser", new Password("password2"));
    }

    @Test
    public void authenticateOnetimePasswordAtManager_shouldWork() throws Exception {
        Authentication authenticate = authManager.authenticate("testuser", new OneTimeValue(90489 * 2));
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateOnetimePasswordAtManager_shouldFail() throws Exception {
        authManager.authenticate("testuser", new OneTimeValue(123));
    }

}
