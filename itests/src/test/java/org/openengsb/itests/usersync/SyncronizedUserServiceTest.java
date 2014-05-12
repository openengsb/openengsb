package org.openengsb.itests.usersync;

import static org.junit.Assert.assertTrue;
import static org.openengsb.itests.usersync.ModelAsserts.assertEqualUser;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.services.SecurityContext;
import org.openengsb.core.usersync.SyncronizedUserService;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Credential;
import org.openengsb.domain.userprojects.model.User;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class SyncronizedUserServiceTest extends AbstractPreConfiguredExamTestHelper {

    private SyncronizedUserService impl;
    private UserDataManager userManager;
    private QueryInterface queryService;
    private AuthenticationContext authenticationContext;

    private final String userName = "testUser";
    private final String credType = "password";
    private final String credValue = "password123";
    private final String attrName = "testAttr";
    private final String attrValue = "attrValue1";
    private final String cred2Type = "retinascan";
    private final String cred2Value = "blue";


    @Before
    public void setUp() throws Exception {
        ContextHolder.get().setCurrentContextId("test");

        userManager = getOsgiService(UserDataManager.class);
        authenticationContext = getOsgiService(AuthenticationContext.class);
        queryService = getOsgiService(QueryInterface.class);
        
        impl = getOsgiService(SyncronizedUserService.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCheckinUser_shouldCreateUserInDbAndDataManager() throws ExecutionException {

        SecurityContext.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                authenticationContext.login("admin", new Password("password"));

                User user = new User(userName);

                Credential cred = new Credential();
                cred.setType(credType);
                cred.setValue(credValue);
                cred.generateUuid(user.getUsername());
                user.getCredentials().add(cred);

                Attribute attr = new Attribute();
                attr.setAttributeName(attrName);
                attr.getValues().add(attrValue);
                attr.generateUuid(user.getUsername());
                user.getAttributes().add(attr);

                impl.checkinUser(user);

                // Assert userManager-Content
                assertTrue(userManager.getUserList().contains(userName));
                assertTrue(userManager.getUserAttribute(userName, attrName).get(0).equals(attrValue));
                assertTrue(userManager.getUserCredentials(userName, credType).equals(credValue));
                List<User> result = queryService.queryByString(User.class, "username:\"" + user.getUsername() + "\"");

                // Assert DB-Result
                assertEqualUser(user, result.get(0));

                return null;
            }
        });
    }

    @Test
    public void testCheckinUser_shouldUpdateUserInDbAndDataManager() throws ExecutionException {

        SecurityContext.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                authenticationContext.login("admin", new Password("password"));

                User user = new User(userName);
                Credential cred = new Credential();
                cred.setType(credType);
                cred.setValue(credValue);
                cred.generateUuid(user.getUsername());
                user.getCredentials().add(cred);

                Attribute attr = new Attribute();
                attr.setAttributeName(attrName);
                attr.getValues().add(attrValue);
                attr.generateUuid(user.getUsername());
                user.getAttributes().add(attr);

                impl.checkinUser(user);

                Credential cred2 = new Credential();
                cred2.setType(cred2Type);
                cred2.setValue(cred2Value);
                cred2.generateUuid(user.getUsername());
                user.getCredentials().add(cred2);

                impl.checkinUser(user);

                assertTrue(userManager.getUserList().contains(userName));
                assertTrue(userManager.getUserAttribute(userName, attrName).get(0).equals(attrValue));
                assertTrue(userManager.getUserCredentials(userName, credType).equals(credValue));
                assertTrue(userManager.getUserCredentials(userName, cred2Type).equals(cred2Value));
                List<User> result = queryService.queryByString(User.class, "username:\"" + user.getUsername() + "\"");

                assertEqualUser(user, result.get(0));

                return null;
            }
        });
    }
}
