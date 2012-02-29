package org.openengsb.infrastructure.ldap.internal;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.NetworkSchemaLoader;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.apache.openjpa.persistence.util.SourceCode.ACCESS;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.AbstractPermissionProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.security.PermissionProvider;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.NoSuchAttributeException;
import org.openengsb.core.api.security.service.NoSuchCredentialsException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.openengsb.separateProject.SchemaConstants;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;


public class LdapUnitTest extends AbstractOsgiMockServiceTest{

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUnitTest.class);

    private static UserDataManager userManager;
    private static LdapConnection connection;
    private static String userName1 = "testUser";
    private static String userName2 = "testUser2";
    private static String credentialsName = "password";
    //private static String credentialsName2 = "password";
    private static String attributeName = "testAttribute";
    //private static String attributeName2 = "testAttribute2";
    //    private Object[] attributeValue = new Object[] {new Boolean(true), new String("abc")};
    //    private Object[] attributeValue2 = new Object[] {new String("xyz"), new Boolean(false)};
    private static Dn dnTestUser1;
    private static Dn dnTestUser2;
    private static Dn dnTestCredentials;
    //private static Dn dnTestCredentials2;
    private static Dn dnTestAttribute;
    //  private static Dn dnTestAttribute2;

    public static class PermissionImpl implements Permission{

        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public PermissionImpl(String description){
            this.description = description;
        }

        @Override
        public String describe(){
            return description;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof LdapUnitTest.PermissionImpl){
                return ((LdapUnitTest.PermissionImpl)obj).description.equals(description);
            }
            return false;
        }

    };

    public static class TestPermission implements Permission {
        private String desiredResult;

        public TestPermission() {
        }

        public TestPermission(Access desiredResult) {
            super();
            this.desiredResult = desiredResult.name();
        }

        @Override
        public String describe() {
            return "for testing purposes";
        }

        public String getDesiredResult() {
            return desiredResult;
        }

        public void setDesiredResult(String desiredResult) {
            this.desiredResult = desiredResult;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(desiredResult);
        }

        @Override
        public String toString() {
            return "TestPermission: " + desiredResult;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LdapUnitTest.TestPermission)) {
                return false;
            }
            final LdapUnitTest.TestPermission other = (LdapUnitTest.TestPermission) obj;
            return Objects.equal(desiredResult, other.desiredResult);
        }

    }
    
    
    private static UserDataManager setupUserManager(){
        UserDataManagerLdap m = new UserDataManagerLdap();        
        m.setLdapDao(new LdapDao(connection));
        return m;
    }

    //    private static LdapConnection setupConnection() throws Exception{
    //        LdapConnection c = new LdapNetworkConnection("localhost", 10389);
    //
    //        BindRequest bindRequest = new BindRequestImpl();
    //        bindRequest.setName(new Dn("uid=admin,ou=system"));
    //        bindRequest.setCredentials("secret");
    //
    //        c.setTimeOut(0);
    //        c.connect();
    //        c.bind(bindRequest);
    //        return c;
    //    }

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

        dnTestUser1 = new Dn(String.format("cn=%s,ou=users,ou=userdata,dc=openengsb,dc=org", userName1));
        dnTestUser2 = new Dn(String.format("cn=%s,ou=users,ou=userdata,dc=openengsb,dc=org", userName2));
        Dn ou = new Dn(new Rdn("ou=attributes"),dnTestUser1);
        dnTestAttribute = new Dn(new Rdn(String.format("cn=%s", attributeName)),ou);
        ou = new Dn(new Rdn("ou=attributes"),dnTestUser2);
        //dnTestAttribute2 = new Dn(new Rdn(String.format("cn=%s", attributeName)),ou);
        ou = new Dn(new Rdn("ou=credentials"),dnTestUser1);
        dnTestCredentials = new Dn(new Rdn(String.format("cn=%s", credentialsName)),ou);
        ou = new Dn(new Rdn("ou=credentials"),dnTestUser2);
        //dnTestCredentials2 = new Dn(new Rdn(String.format("cn=%s", credentialsName)),ou);
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
        userManager.deleteUser(userName1);
        userManager.deleteUser(userName2);
        ((UserDataManagerLdap)userManager).getDao().deleteSubtreeExcludingRoot(SchemaConstants.ouGlobalPermissionSets());
        userManager.createUser(userName1);
    }

    /*--------------- users -----------------*/

    @Test
    public void testDeleteNonExistingUser_shouldDoNothing() throws Exception{
        assertThat(connection.exists(dnTestUser2), is(false));
        userManager.deleteUser(userName2);
    }

    @Test(expected = UserExistsException.class)
    public void testCreateExistingUser_shouldThrowUserExistsException() throws Exception{
        assertThat(connection.exists(dnTestUser1), is(true));
        userManager.createUser(userName1);
    }

    @Test(expected = UserExistsException.class)
    public void testCaseSensitivity_shouldThrowUserExistsException() throws Exception{
        String newUserName = "abc";
        Dn dn = new Dn("cn="+newUserName+"ou=users,ou=userdata,dc=openengsb,dc=org");
        assertThat(connection.exists(dn), is(false));
        userManager.createUser(newUserName);
        assertThat(connection.exists(dn), is(true));
        userManager.createUser(newUserName.toUpperCase());
    }

    /*--------------- credentials -----------------*/

    @Test
    public void testSetUserCredentialsNullValue_shouldPersistNullValue() throws Exception{
        assertThat(connection.exists(dnTestUser1), is(true));
        assertThat(connection.exists(dnTestCredentials), is(false));
        userManager.setUserCredentials(userName1, credentialsName, null);
        assertThat(connection.exists(dnTestCredentials), is(true));
        String returnedCredentials = userManager.getUserCredentials(userName1, credentialsName);
        assertThat(returnedCredentials, nullValue());
    }

    @Test
    public void testSetUserCredentialsEmptyString_shouldPersistEmptyString() throws Exception{
        String expectedValue = "";
        assertThat(connection.exists(dnTestUser1), is(true));
        assertThat(connection.exists(dnTestCredentials), is(false));
        userManager.setUserCredentials(userName1, credentialsName, expectedValue);
        assertThat(connection.exists(dnTestCredentials), is(true));
        String returnedCredentials = userManager.getUserCredentials(userName1, credentialsName);
        assertThat(returnedCredentials, is(expectedValue));
    }

    @Test
    public void testSetUserCredentials_shouldPersistCredentials() throws Exception{
        String expectedValue = "abc";
        assertThat(connection.exists(dnTestUser1), is(true));
        assertThat(connection.exists(dnTestCredentials), is(false));
        userManager.setUserCredentials(userName1, credentialsName, expectedValue);
        assertThat(connection.exists(dnTestCredentials), is(true));
        String returnedCredentials = userManager.getUserCredentials(userName1, credentialsName);
        assertThat(returnedCredentials, is(expectedValue));
    }

    @Test
    public void testSetExistingUserCredentials_shouldOverwrite() throws Exception{
        String originalValue = "abc";
        String newValue = "xyz";
        userManager.setUserCredentials(userName1, credentialsName, originalValue);
        String returnedCredentials = userManager.getUserCredentials(userName1, credentialsName);
        assertThat(returnedCredentials, is(originalValue));
        userManager.setUserCredentials(userName1, credentialsName, newValue); //overwrite original value
        returnedCredentials = userManager.getUserCredentials(userName1, credentialsName);
        assertThat(returnedCredentials, is(newValue));
    }

    @Test(expected = UserNotFoundException.class)
    public void testSetCredentialsForNonexistingUser_shouldThrowUserNotFoundException() throws Exception{
        assertThat(connection.exists(dnTestUser2), is(false));
        userManager.setUserCredentials(userName2, "randomName", "randomValue");
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetCredentialsForNonexistingUser_shouldThrowUserNotFoundException() throws Exception{
        assertThat(connection.exists(dnTestUser2), is(false));
        userManager.getUserCredentials(userName2, "randomName");
    }

    @Test(expected = NoSuchCredentialsException.class)
    public void testGetNonexistingCredentialsForExistingUser_shouldThrowNoSuchCredentialsException() throws Exception{
        assertThat(connection.exists(dnTestUser1), is(true));
        userManager.getUserCredentials(userName1, "nonexistingname");
    }

    @Test(expected = UserNotFoundException.class)
    public void testRemoveCredentialsForNonexistingUser_shouldThrowUserNotFoundException() throws Exception{
        assertThat(connection.exists(dnTestUser2), is(false));
        userManager.removeUserCredentials(userName2, "nonexistingname");
    }

    @Test
    public void testRemoveCredentials_shouldRemoveCredentials() throws Exception{
        userManager.setUserCredentials(userName1, credentialsName, "randomValue");
        assertThat(connection.exists(dnTestCredentials), is(true));
        userManager.removeUserCredentials(userName1, credentialsName);
        assertThat(connection.exists(dnTestCredentials), is(false));
    }

    /*--------------- attributes -----------------*/

    @Test
    public void testSetEmptyUserAttribute_shouldPersist() throws Exception {
        Object[] testAtt = new Object[0];
        userManager.setUserAttribute(userName1, attributeName, testAtt);
        assertThat(connection.exists(dnTestAttribute), is(true));
        List<Object> result = userManager.getUserAttribute(userName1, attributeName);
        assertThat(result, not(nullValue()));
        assertThat(result.size(), is(0));
    }    

    @Test
    public void testOverwriteUserAttribute_shouldOverwrite() throws Exception {
        Object[] originalValue = new Object[]{true,"",307708};

        userManager.setUserAttribute(userName1, attributeName, originalValue);
        List<Object> result = userManager.getUserAttribute(userName1, attributeName);

        assertThat((Boolean)result.get(0), is(originalValue[0]));
        assertThat((String)result.get(1), is(originalValue[1]));
        assertThat((Integer)result.get(2), is(originalValue[2]));

        Object[] newValue = new Object[]{"hello",false,5};

        userManager.setUserAttribute(userName1, attributeName, newValue);
        result = userManager.getUserAttribute(userName1, attributeName);

        assertThat((String)result.get(0), is(newValue[0]));
        assertThat((Boolean)result.get(1), is(newValue[1]));
        assertThat((Integer)result.get(2), is(newValue[2]));
    }

    @Test(expected = NoSuchAttributeException.class)
    public void testgetNonexistingUserAttribute_shouldThrowNoSuchAttributeExpcetion() throws Exception {
        assertThat(connection.exists(dnTestUser1), is(true));
        assertThat(connection.exists(dnTestAttribute), is(false));
        userManager.getUserAttribute(userName1, attributeName);
    }

    @Test(expected = UserNotFoundException.class)
    public void testgetUserAttributeFromNonexistingUser_expectedUserNotFoundException() throws Exception {
        userManager.getUserAttribute("non existing user", "random attribute");
    }

    @Test
    public void testRemoveNonexistingUserAttribute_shouldFailSilent() throws Exception {
        userManager.removeUserAttribute(userName1, "random attribute");
    }

    @Test(expected = UserNotFoundException.class)
    public void testRemoveUserAttributeFromNonexistingUser_expectedUserNotFoundException() throws Exception {
        userManager.removeUserAttribute("non existing user", "random attribute");
    }

    /*--------------- permissions -----------------*/

    /*
     * What to test:
     * null description
     * insertion order
     * duplicates
     * nonexisting permission set
     * cycles
     * */




    @Test
    public void testAddPermissionSetToPermissionSet_shouldPersistChildrenSets() throws Exception{
        String parent = "SetA";
        String childB = "setB";
        String childC = "setC";
        Dn hierarchyB = new Dn("cn="+childB+",ou=childrenSets,cn="+parent+",ou=permissionSets,ou=userdata,dc=openengsb,dc=org");
        Dn hierarchyC = new Dn("cn="+childC+",ou=childrenSets,cn="+parent+",ou=permissionSets,ou=userdata,dc=openengsb,dc=org");

        userManager.createPermissionSet(parent);
        userManager.createPermissionSet(childB);
        userManager.createPermissionSet(childC);
        userManager.addPermissionSetToPermissionSet(parent, childB, childC);
        assertThat(connection.exists(hierarchyB), is(true));
        assertThat(connection.exists(hierarchyC), is(true));
    }

    //TODO test addpermissions for non existing user should throw exception
    @Test
    public void testGetPermissionsForUser_shouldReturnPermissions() throws Exception{
        Permission permission = new TestPermission(Access.GRANTED);
        userManager.addPermissionToUser(userName1, permission);
        Collection<Permission> permissions = userManager.getPermissionsForUser(userName1);
        assertThat(permissions, hasItem(permission));
        assertThat(permissions.size(), is(1));
    }
    
    @Test
    public void testCreatePermissionSet_shouldPersistSet() throws Exception{
        String set = "SetA";
        Permission p1 = new TestPermission(Access.GRANTED);
        Permission p2 = new TestPermission(Access.DENIED);
        Permission[] permissions = new Permission[]{p1,p2};
        userManager.createPermissionSet(set, permissions);
        Collection<Permission> result = userManager.getPermissionsFromPermissionSet(set);
        assertThat(result, hasItem(p1));
        assertThat(result, hasItem(p2));
        assertThat(result.size(), is(2));
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("permissionClass", TestPermission.class.getName());
        PermissionProvider permissionProvider = new AbstractPermissionProvider(TestPermission.class){};
        registerService(permissionProvider, props, PermissionProvider.class);
        DefaultOsgiUtilsService osgiServiceUtils = new DefaultOsgiUtilsService();
        osgiServiceUtils.setBundleContext(bundleContext);
        registerService(osgiServiceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(osgiServiceUtils);
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

    //  @Test
    //  public void testSetUserAttribute_shouldAlsoPersistOrder() throws Exception {
    //      Boolean value0 = true;
    //      String value1 = "";
    //      Integer value2 = 307708;
    //
    //      userManager.setUserAttribute(userName, attributeName, value0, value1, value2);
    //      assertThat(connection.exists(dnTestAttribute), is(true));
    //
    //      List<Object> result = userManager.getUserAttribute(userName, attributeName);
    //      assertThat((Boolean)result.get(0), is(value0));
    //      assertThat((String)result.get(1), is(value1));
    //      assertThat((Integer)result.get(2), is(value2));
    //  }

}
