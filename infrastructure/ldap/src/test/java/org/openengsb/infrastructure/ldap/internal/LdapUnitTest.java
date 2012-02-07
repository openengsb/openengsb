package org.openengsb.infrastructure.ldap.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.NetworkSchemaLoader;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.apache.directory.shared.ldap.model.schema.registries.Schema;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.infrastructure.ldap.internal.dao.LdapDao;
import org.openengsb.infrastructure.ldap.internal.model.SchemaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LdapUnitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUnitTest.class);

    private static UserDataManager userManager;
    private static LdapConnection connection;
    private static String userName = "testUser";
    private static String userName2 = "testUser2";
    private static String credentialsName = "password";
    private static String credentialsName2 = "password";
    private static String attributeName = "testAttribute";
    private static String attributeName2 = "testAttribute2";
    private Object[] attributeValue = new Object[] {new Boolean(true), new String("abc")};
    private Object[] attributeValue2 = new Object[] {new String("xyz"), new Boolean(false)};
    private static Dn dnTestUser;
    private static Dn dnTestUser2;
    private static Dn dnTestCredentials;
    private static Dn dnTestCredentials2;
    private static Dn dnTestAttribute;
    private static Dn dnTestAttribute2;

    private static class PermissionImpl implements Permission{
        private String description;
        public PermissionImpl(String description){
            this.description = description;
        }
        @Override
        public String describe(){
            return description;
        }
    };

    private static UserDataManager setupUserManager(){
        UserDataManagerLdapNEW m = new UserDataManagerLdapNEW();        
        m.setLdapDao(new LdapDao(connection));
        return m;
    }

    private static LdapConnection setupConnection() throws Exception{
        LdapConnection c = new LdapNetworkConnection("localhost", 10389);

        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(new Dn("uid=admin,ou=system"));
        bindRequest.setCredentials("secret");

        c.setTimeOut(0);
        c.connect();
        c.bind(bindRequest);
        return c;
    }
    
    private static LdapNetworkConnection setupNetworkConnection() throws Exception{
        LdapNetworkConnection c = new LdapNetworkConnection("localhost", 10389);
        
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(new Dn("uid=admin,ou=system"));
        bindRequest.setCredentials("secret");
        
        c.setTimeOut(0);
        c.connect();
        c.bind(bindRequest);
        
        NetworkSchemaLoader nsl = new NetworkSchemaLoader(c);
        c.loadSchema(nsl);

        return c;
    }

    private static void setupTests() throws Exception{
        
        dnTestUser = new Dn(String.format("cn=%s,ou=users,ou=userdata,dc=openengsb,dc=org", userName));
        dnTestUser2 = new Dn(String.format("cn=%s,ou=users,ou=userdata,dc=openengsb,dc=org", userName2));
        Dn ou = new Dn(new Rdn("ou=attributes"),dnTestUser);
        dnTestAttribute = new Dn(new Rdn(String.format("cn=%s", attributeName)),ou);
        ou = new Dn(new Rdn("ou=attributes"),dnTestUser2);
        dnTestAttribute2 = new Dn(new Rdn(String.format("cn=%s", attributeName)),ou);
        ou = new Dn(new Rdn("ou=credentials"),dnTestUser);
        dnTestCredentials = new Dn(new Rdn(String.format("cn=%s", credentialsName)),ou);
        ou = new Dn(new Rdn("ou=credentials"),dnTestUser2);
        dnTestCredentials2 = new Dn(new Rdn(String.format("cn=%s", credentialsName)),ou);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        connection = setupNetworkConnection();
        userManager = setupUserManager();
        setupTests();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        connection.unBind();
        connection.close();
    }

    @Before //TODO make some resetServer() method or even better applyLdifs
    public void doBefore() throws Exception{
        userManager.deleteUser(userName);
        userManager.deleteUser(userName2);
        ((UserDataManagerLdapNEW)userManager).getDao().deleteSubtreeExcludingRoot(SchemaConstants.ouGlobalPermissionSets());
        userManager.createUser(userName);
    }
    

    
    /*--------------- credentials -----------------*/
    
    @Test
    public void testSetUserCredentialsNullValue_shouldPersistNullValue() throws Exception{
        assertThat(connection.exists(dnTestUser), is(true));
        assertThat(connection.exists(dnTestCredentials), is(false));
        userManager.setUserCredentials(userName, credentialsName, null);
        assertThat(connection.exists(dnTestCredentials), is(true));
        String returnedCredentials = userManager.getUserCredentials(userName, credentialsName);
        assertThat(returnedCredentials, nullValue());
    }
    
    @Test
    public void testSetUserCredentialsEmptyString_shouldPersistEmptyString() throws Exception{
        String expectedValue = "";
        assertThat(connection.exists(dnTestUser), is(true));
        assertThat(connection.exists(dnTestCredentials), is(false));
        userManager.setUserCredentials(userName, credentialsName, expectedValue);
        assertThat(connection.exists(dnTestCredentials), is(true));
        String returnedCredentials = userManager.getUserCredentials(userName, credentialsName);
        assertThat(returnedCredentials, is(expectedValue));
    }
    
    @Test
    public void testSetUserCredentials_shouldPersistCredentials() throws Exception{
        String expectedValue = "abc";
        assertThat(connection.exists(dnTestUser), is(true));
        assertThat(connection.exists(dnTestCredentials), is(false));
        userManager.setUserCredentials(userName, credentialsName, expectedValue);
        assertThat(connection.exists(dnTestCredentials), is(true));
        String returnedCredentials = userManager.getUserCredentials(userName, credentialsName);
        assertThat(returnedCredentials, is(expectedValue));
    }
    
    @Test
    public void testSetExistingUserCredentials_shouldOverwrite() throws Exception{
        String originalValue = "abc";
        String newValue = "xyz";
        userManager.setUserCredentials(userName, credentialsName, originalValue);
        String returnedCredentials = userManager.getUserCredentials(userName, credentialsName);
        assertThat(returnedCredentials, is(originalValue));
        userManager.setUserCredentials(userName, credentialsName, newValue); //overwrite original value
        returnedCredentials = userManager.getUserCredentials(userName, credentialsName);
        assertThat(returnedCredentials, is(newValue));
    }
    
    @Test(expected = UserNotFoundException.class)
    public void testSetCredentialsForNonexistingUser_shouldThrowUserNotFoundException() throws Exception{
        assertThat(connection.exists(dnTestUser2), is(false));
        userManager.setUserCredentials(userName2, "randomName", "randomValue");
    }
    
    /*--------------- attributes -----------------*/
    
    @Test
    public void testSetUserAttribute_shouldAlsoPersistOrder() throws Exception {
        Boolean value0 = true;
        String value1 = "";
        Integer value2 = 307708;
        
        userManager.setUserAttribute(userName, attributeName, value0, value1, value2);
        assertThat(connection.exists(dnTestAttribute), is(true));
        
        List<Object> result = userManager.getUserAttribute(userName, attributeName);
        assertThat((Boolean)result.get(0), is(value0));
        assertThat((String)result.get(1), is(value1));
        assertThat((Integer)result.get(2), is(value2));
    }
    
    /*--------------- permissions -----------------*/
    
    /*
     * What to test:
     * null description
     * insertion order
     * duplicates
     * nonexisting permission set
     * 
     * */
    
    
    @Test
    public void testCreatePermissionSet_shouldPersistSet() throws Exception{
        String description = "le description";
        String name = "SetA";
        Permission p = new PermissionImpl(description);
        
        Dn permissionSetDn = SchemaConstants.globalPermissionSet(name);
        
        assertThat(connection.exists(permissionSetDn), is(false));
        userManager.createPermissionSet(name, p);
        assertThat(connection.exists(permissionSetDn), is(true));
    }
    
    @Test
    public void testAddPermissionSetToPermissionSet_shouldPersistSet() throws Exception{
        String description = "le description";
        Permission p = new PermissionImpl(description);
        
        String name = "SetA";
        Dn permissionSetDn = SchemaConstants.globalPermissionSet(name);
        
        assertThat(connection.exists(permissionSetDn), is(false));
        userManager.createPermissionSet(name, p);
        assertThat(connection.exists(permissionSetDn), is(true));
        
        String name2 = "setB";
        Dn permissionSet2Dn = SchemaConstants.globalPermissionSet(name2);
        
        assertThat(connection.exists(permissionSet2Dn), is(false));
        userManager.createPermissionSet(name2, p);
        assertThat(connection.exists(permissionSet2Dn), is(true));
        
        userManager.addPermissionSetToPermissionSet(name, name2);
        
    }

//    @Test
//    public void testCreateAndDeleteNewUser_shouldSucceed() throws Exception {
//        assertThat(connection.exists(dnTestUser2), is(false));
//        userManager.createUser(userName2);
//        assertThat(connection.exists(dnTestUser2), is(true));
//        userManager.deleteUser(userName2);
//        assertThat(connection.exists(dnTestUser2), is(false));
//    }
//
//    @Test
//    public void testDeleteNonExistingUser_shouldDoNothing() throws Exception {
//        assertThat(connection.exists(dnTestUser2), is(false));
//        userManager.deleteUser(userName2);
//    }
//
//    @Test(expected = UserExistsException.class)
//    public void testCreateExistingUser_expectedUserExistsException() throws Exception {
//        assertThat(connection.exists(dnTestUser), is(true));
//        userManager.createUser(userName);
//    }
    
//  @Test
//  public void testSetAttribute_shouldPersistAttribute() throws Exception{
//      assertThat(connection.exists(dnTestUser), is(true));
//      
//      assertThat(dnTestAttribute.getName().equals("cn=testAttribute,ou=attributes,cn=testUser,ou=users,ou=userdata,dc=openengsb,dc=org"), is(true));
//      LOGGER.warn(dnTestAttribute.getName());
//      LOGGER.warn("12345678");
//      userManager.setUserAttribute(userName, attributeName, attributeValue);
//      assertThat(connection.exists(dnTestAttribute), is(true));
//      assertThat(connection.exists(dnTestAttribute), is(true));
//  }
    
    //
    //    @Test
    //    public void testGetAllUsers() throws Exception {
    //        userManager.createUser(testUserName);
    //        userManager.createUser(testUserName2);
    //        Collection<String> userList = userManager.getUserList();
    //        assertThat(userList, not(nullValue()));
    //        assertThat(userList.size(), is(2));
    //        assertThat(userList, hasItems(testUserName, testUserName2));
    //    }
    //    
    //    @Test
    //    public void testDeleteAllUsers_shouldLeaveEmptyDirectory() throws Exception {
    //        Collection<String> userList = userManager.getUserList();
    //        assertThat(userList.size(), is(0));
    //        userManager.createUser(testUserName);
    //        userManager.createUser(testUserName2);
    //        userList = userManager.getUserList();
    //        assertThat(userList.size(), is(2));
    //        userManager.deleteUser(testUserName);
    //        userManager.deleteUser(testUserName2);
    //        userList = userManager.getUserList();
    //        assertThat(userList.size(), is(0));
    //    }
    //    
    //    
    //    
    //    @Test
    //    public void testSetCredentials_shouldOverwritePreviousValue() throws Exception{
    //        String pwvalue = "testpw";
    //        userManager.createUser(testUserName);
    //        userManager.setUserCredentials(testUserName, testCredentials, pwvalue);
    //        String pw = userManager.getUserCredentials(testUserName, testCredentials);
    //        assertThat(pw, is(pwvalue));
    //        String newPwValue = "newTestpw";
    //        userManager.setUserCredentials(testUserName, testCredentials, newPwValue);
    //        pw = userManager.getUserCredentials(testUserName, testCredentials);
    //        assertThat(pw, is(newPwValue));
    //    }
    //  
    //    
    //    
    //    @Test
    //    public void testGetCredentials_shouldReturnExpectedValue() throws Exception{
    //        String pwvalue = "testpw";
    //        userManager.createUser(testUserName);
    //        userManager.setUserCredentials(testUserName, testCredentials, pwvalue);
    //        String pw = userManager.getUserCredentials(testUserName, testCredentials);
    //        assertThat(pw, is(pwvalue));
    //    }
    //    
    //    @Test(expected = UserNotFoundException.class)
    //    public void testGetCredentials_expectedUserNotFoundException() throws Exception{
    //        assertThat(connection.exists(dnTestUser), is(false));
    //        userManager.getUserCredentials(testUserName, testCredentials);
    //    }
    //    
    //    @Test(expected = NoSuchCredentialsException.class)
    //    public void testGetCredentials_expectedNoSuchCredentialsException() throws Exception{
    //        userManager.createUser(testUserName);
    //        assertThat(connection.exists(dnTestCredentials), is(false));
    //        userManager.getUserCredentials(testUserName, testCredentials);
    //    }
    //  
    //    @Test
    //    public void testRemoveCredentials_shouldRemoveCredentials() throws Exception{
    //        String pwvalue = "testpw";
    //        userManager.createUser(testUserName);
    //        userManager.setUserCredentials(testUserName, testCredentials, pwvalue);
    //        assertThat(connection.exists(dnTestCredentials), is(true));
    //        userManager.removeUserCredentials(testUserName, testCredentials);
    //        assertThat(connection.exists(dnTestCredentials), is(false));
    //    }
    //    
    //    @Test(expected = UserNotFoundException.class)
    //    public void testRemoveCredentials_expectedUserNotFoundException() throws Exception{
    //        assertThat(connection.exists(dnTestUser), is(false));
    //        userManager.getUserCredentials(testUserName, testCredentials);
    //    }
    //    
    //    @Test
    //    public void testRemoveNonExistingCredentials_shouldDoNothing() throws Exception{
    //        userManager.createUser(testUserName);
    //        assertThat(connection.exists(dnTestCredentials), is(false));
    //        userManager.removeUserCredentials(testUserName, testCredentials);
    //    }
        

        
//        //@Test
//        public void testSetAttribute_shouldUpdateExistingAttribute() throws Exception{
//            userManager.createUser(userName);
//            userManager.setUserAttribute(userName, attributeName, attributeValue);
//            assertThat(connection.exists(dnTestAttribute), is(true));
//            userManager.setUserAttribute(userName, attributeName, new Object[0]);
//            assertThat(connection.exists(dnTestAttribute), is(true));
//            List<Object> attribute = userManager.getUserAttribute(userName, attributeName);
//            assertThat(attribute, not(nullValue()));
//            assertThat(attribute, is(Collections.EMPTY_LIST));
//        }
//        
//        //@Test
//        public void testGetAttribute_shouldHaveCorrectType() throws Exception{
//            userManager.createUser(userName);
//            userManager.setUserAttribute(userName, attributeName, attributeValue);
//            List<Object> attribute = userManager.getUserAttribute(userName, attributeName);
//            assertThat(attribute, not(nullValue()));
//            assertThat(attribute.size(), is(2));
//            Object attribute0 = attribute.get(0);
//            assertThat(attribute0, is(attributeValue[0].getClass()));
//            assertThat((Boolean)attribute0, is(attributeValue[0]));
//            Object attribute1 = attribute.get(1);        
//            assertThat(attribute1, is(attributeValue[1].getClass()));
//            assertThat((String)attribute1, is(attributeValue[1]));
//        }
//        
//        @Test(expected = UserNotFoundException.class)
//        public void testSetAttribute_expectedUserNotFoundException() throws Exception{
//            assertThat(connection.exists(dnTestUser2), is(false));
//            userManager.setUserAttribute(userName2, attributeName, attributeValue);        
//        }
//        
//        @Test(expected = UserNotFoundException.class)
//        public void testRemoveAttribute_expectedUserNotFoundException() throws Exception{
//            LOGGER.warn("expect usernotfoundexception");
//            assertThat(connection.exists(dnTestUser.getName()), is(true));
//            userManager.deleteUser(userName);
//            assertThat(connection.exists(dnTestUser.getName()), is(false));
//            assertThat(connection.exists(dnTestAttribute.getName()), is(false));
//            userManager.removeUserAttribute(userName, attributeName);
//            assertThat(connection.exists(dnTestAttribute.getName()), is(false));
//            LOGGER.warn("test end");
//        }
//        
//        @Test
//        public void testRemoveAttribute_shouldDoNothingBecauseAttributeDoesNotExist() throws Exception{
//            LOGGER.warn("expect NOTHING");
//            userManager.createUser(userName2);
//            assertThat(connection.exists(dnTestUser2.getName()), is(true));
//            assertThat(connection.exists(dnTestAttribute2.getName()), is(false));
//            userManager.removeUserAttribute(userName2, attributeName2);
//            LOGGER.warn("test end");
//        }
//        
//        //@Test(expected = UserNotFoundException.class)
//        public void testGetAttribute_expectedUserNotFoundException() throws Exception{
//            assertThat(connection.exists(dnTestUser), is(false));
//            userManager.getUserAttribute(userName, attributeName);
//        }
//        
//        //@Test(expected = NoSuchAttributeException.class)
//        public void testGetAttribute_expectedNoSuchAttributeException() throws Exception{
//            userManager.createUser(userName);
//            assertThat(connection.exists(dnTestUser), is(true));
//            userManager.getUserAttribute(userName, attributeName);
//        }
        
    //    //TODO add test that checks sort order of retrieved attributes list
    //    //duplicates, null value, empty strings, insertion order
    //    
    //    private interface SubtypePermission extends Permission{
    //        
    //    }
    //    
    //    private class SubtypePermissionImpl implements SubtypePermission{
    //
    //        @Override
    //        public String describe() {
    //            
    //            return null;
    //        }
    //        
    //    }
    //
    //    private class PermissionImpl implements Permission{
    //
    //        @Override
    //        public String describe() {
    //            
    //            return null;
    //        }
    //        
    //    }
    //    
    //    public void bla() throws Exception{
    //        userManager.getAllPermissionsForUser(testUserName, SubtypePermissionImpl.class);
    //        userManager.getAllPermissionsForUser(testUserName, PermissionImpl.class);
    //    }

}
