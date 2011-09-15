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
package org.openengsb.core.security;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.virtual.CompositeConnectorProvider;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;
import org.osgi.framework.BundleContext;

public class AuthenticationProviderTest extends AbstractOsgiMockServiceTest {

    private UsernamePasswordAuthenticator passwordAuthenticator;
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

        UsernamePasswordAuthenticator authenticator1 = new UsernamePasswordAuthenticator();
        authenticator1.setUserManager(userManager);
        registerServiceAtLocation(authenticator1, "authenticator/password", AuthenticationDomain.class);
        passwordAuthenticator = authenticator1;

        OnetimePasswordAuthenticator authenticator2 = new OnetimePasswordAuthenticator();
        authenticator2.setUserManager(userManager);
        registerServiceAtLocation(authenticator2, "authenticator/onetime", AuthenticationDomain.class);
        onetimeAuthenticator = authenticator2;

        DomainProvider provider = createDomainProviderMock(AuthenticationDomain.class, "authentication");
        ConnectorInstanceFactory factory = new CompositeConnectorProvider().createFactory(provider);
        authManager = (AuthenticationDomain) factory.createNewInstance("authProvider");

        CompositeConnectorStrategy strategy = new AuthenticationProviderStrategy();

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
        Authentication authenticate = passwordAuthenticator.authenticate("testuser", "password");
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateUsernamePassword_shouldFail() throws Exception {
        passwordAuthenticator.authenticate("testuser", "password2");
    }

    @Test
    public void authenticateOnetimePassword() throws Exception {
        Authentication authenticate = onetimeAuthenticator.authenticate("testuser", 90489 * 2);
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateOnetimePassword_shouldFail() throws Exception {
        onetimeAuthenticator.authenticate("testuser", 123);
    }

    @Test
    public void authenticateUsernamePasswordAtManager_shouldAuthenticateSuccessful() throws Exception {
        Authentication authenticate = authManager.authenticate("testuser", "password");
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateUsernamePasswordAtManager_shouldFail() throws Exception {
        authManager.authenticate("testuser", "password2");
    }

    @Test
    public void authenticateOnetimePasswordAtManager() throws Exception {
        Authentication authenticate = authManager.authenticate("testuser", 90489 * 2);
        assertThat(authenticate.getUsername(), is("testuser"));
    }

    @Test(expected = AuthenticationException.class)
    public void authenticateOnetimePasswordAtManager_shouldFail() throws Exception {
        authManager.authenticate("testuser", 123);
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService osgiServiceUtils = new DefaultOsgiUtilsService();
        osgiServiceUtils.setBundleContext(bundleContext);
        registerService(osgiServiceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(osgiServiceUtils);
    }
}
