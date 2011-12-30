package org.openengsb.infrastructure.ldap.internal;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.security.service.NoSuchCredentialsException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.infrastructure.ldap.EntityManagerLdap;

public class LdapUnitTest {

    //private static final Logger LOGGER = LoggerFactory.getLogger(LdapUnitTest.class);

    private static UserDataManager userManager;
    private static LdapConnection connection;
    private static String testUserName = "testUser";
    private static String testUserName2 = "testUser2";
    private static String testCredentials = "password";
    private static Dn testUserDn;
    private static Dn testCredentialDn;
    
    //can this be replaced by injection?
    private static UserDataManager setupUserManager(){
        UserDataManagerLdap userManager = new UserDataManagerLdap();
        userManager.setConnection(connection);
        userManager.setUserBaseDn(EntityManagerLdap.USERBASEDN);
        return userManager;
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        testUserDn = new Dn(String.format("uid=%s,%s", testUserName, EntityManagerLdap.USERBASEDN));
        testCredentialDn = new Dn(String.format("cn=%s,ou=credentials,uid=%s,%s", testCredentials, testUserName, EntityManagerLdap.USERBASEDN));
        connection = new LdapNetworkConnection("localhost", 10389);
        userManager = setupUserManager();
        
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(new Dn("uid=admin,ou=system"));
        bindRequest.setCredentials("secret");
        
        connection.setTimeOut(0);
        connection.connect();
        connection.bind(bindRequest);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        connection.unBind();
        connection.close();
    }

    @Before
    public void clear() throws Exception{
        userManager.deleteUser(testUserName);
        userManager.deleteUser(testUserName2);
    }

    @Test
    public void testCreateAndDeleteNewUser_shouldSucceed() throws Exception {
        assertFalse(connection.exists(testUserDn));
        userManager.createUser(testUserName);
        assertTrue(connection.exists(testUserDn));
        userManager.deleteUser(testUserName);
        assertFalse(connection.exists(testUserDn));
    }
    
    @Test
    public void testDeleteNonExistingUser_shouldDoNothing() throws Exception {
        assertFalse(connection.exists(testUserDn));
        userManager.deleteUser(testUserName);
    }

    @Test(expected = UserExistsException.class)
    public void testCreateExistingUser_expectedUserExistsException() throws Exception {
        userManager.createUser(testUserName);
        assertTrue(connection.exists(testUserDn));
        userManager.createUser(testUserName);
    }

    @Test
    public void testGetAllUsers() throws Exception {
        userManager.createUser(testUserName);
        userManager.createUser(testUserName2);
        Collection<String> userList = userManager.getUserList();
        assertThat(userList, not(nullValue()));
        assertThat(userList.size(), is(2));
        assertThat(userList, hasItems(testUserName, testUserName2));
    }
    
    @Test
    public void testDeleteAllUsers_shouldLeaveEmptyDirectory() throws Exception {
        Collection<String> userList = userManager.getUserList();
        assertThat(userList.size(), is(0));
        userManager.createUser(testUserName);
        userManager.createUser(testUserName2);
        userList = userManager.getUserList();
        assertThat(userList.size(), is(2));
        userManager.deleteUser(testUserName);
        userManager.deleteUser(testUserName2);
        userList = userManager.getUserList();
        assertThat(userList.size(), is(0));
    }
    
    @Test
    public void testSetCredentials_shouldPersistCredentials() throws Exception{
        userManager.createUser(testUserName);
        userManager.setUserCredentials(testUserName, testCredentials, "testpw");
        assertTrue(connection.exists(testCredentialDn));
    }
    
    @Test(expected = UserNotFoundException.class)
    public void testSetCredentials_expectedUserNotFoundException() throws Exception{
        assertFalse(connection.exists(testUserDn));
        userManager.setUserCredentials(testUserName, testCredentials, "testpw");
    }
    
    @Test
    public void testGetCredentials_shouldReturnExpectedValue() throws Exception{
        String pwvalue = "testpw";
        userManager.createUser(testUserName);
        userManager.setUserCredentials(testUserName, testCredentials, pwvalue);
        String pw = userManager.getUserCredentials(testUserName, testCredentials);
        assertThat(pw, is(pwvalue));
    }
    
    @Test(expected = UserNotFoundException.class)
    public void testGetCredentials_expectedUserNotFoundException() throws Exception{
        assertFalse(connection.exists(testUserDn));
        userManager.getUserCredentials(testUserName, testCredentials);
    }
    
    @Test(expected = NoSuchCredentialsException.class)
    public void testGetCredentials_expectedNoSuchCredentialsException() throws Exception{
        userManager.createUser(testUserName);
        assertFalse(connection.exists(testCredentialDn));
        userManager.getUserCredentials(testUserName, testCredentials);
    }
    
    @Test
    public void testSetCredentials_shouldOverwritePreviousValue() throws Exception{
        String pwvalue = "testpw";
        userManager.createUser(testUserName);
        userManager.setUserCredentials(testUserName, testCredentials, pwvalue);
        String pw = userManager.getUserCredentials(testUserName, testCredentials);
        assertThat(pw, is(pwvalue));
        String newPwValue = "newTestpw";
        userManager.setUserCredentials(testUserName, testCredentials, newPwValue);
        pw = userManager.getUserCredentials(testUserName, testCredentials);
        assertThat(pw, is(newPwValue));
    }
    
    @Test
    public void testRemoveCredentials_shouldRemoveCredentials() throws Exception{
        String pwvalue = "testpw";
        userManager.createUser(testUserName);
        userManager.setUserCredentials(testUserName, testCredentials, pwvalue);
        assertTrue(connection.exists(testCredentialDn));
        userManager.removeUserCredentials(testUserName, testCredentials);
        assertFalse(connection.exists(testCredentialDn));
    }
    
    @Test(expected = UserNotFoundException.class)
    public void testRemoveCredentials_expectedUserNotFoundException() throws Exception{
        assertFalse(connection.exists(testUserDn));
        userManager.getUserCredentials(testUserName, testCredentials);
    }
    
    @Test
    public void testRemoveNonExistingCredentials_shouldDoNothing() throws Exception{
        userManager.createUser(testUserName);
        assertFalse(connection.exists(testCredentialDn));
        userManager.removeUserCredentials(testUserName, testCredentials);
    }
    
}
