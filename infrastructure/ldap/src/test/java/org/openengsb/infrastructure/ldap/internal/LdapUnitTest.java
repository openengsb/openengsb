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

package org.openengsb.infrastructure.ldap.internal;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Persistence;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.NetworkSchemaLoader;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//
//import org.openengsb.core.api.security.model.Permission;
//import org.openengsb.core.api.security.service.UserDataManager;
//import org.openengsb.core.api.security.service.UserNotFoundException;
//import org.openengsb.core.services.internal.security.EntryUtils;
//import org.openengsb.core.services.internal.security.UserDataManagerImpl;
//import org.openengsb.core.services.internal.security.model.UserData;
//import org.openengsb.core.test.AbstractOsgiMockServiceTest;
//import org.openengsb.core.util.DefaultOsgiUtilsService;
//import org.openengsb.domain.authorization.AuthorizationDomain.Access;
//import org.openengsb.labs.delegation.service.ClassProvider;
//import org.openengsb.labs.delegation.service.Constants;
//import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;
//
//

//import org.openengsb.core.api.AbstractPermissionProvider;
import org.openengsb.core.api.OsgiUtilsService;
//import org.openengsb.core.api.security.PermissionProvider;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.NoSuchAttributeException;
import org.openengsb.core.api.security.service.NoSuchCredentialsException;
import org.openengsb.core.api.security.service.PermissionSetNotFoundException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.services.internal.UserDataManagerImplTest.TestPermission;
import org.openengsb.core.services.internal.security.EntryUtils;
import org.openengsb.core.services.internal.security.UserDataManagerImpl;
import org.openengsb.core.services.internal.security.model.UserData;
//import org.openengsb.core.common.OpenEngSBCoreServices;
//import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;
import org.openengsb.separateProject.SchemaConstants;
import org.openengsb.separateProject.UserDataManagerLdap;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class LdapUnitTest extends AbstractOsgiMockServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUnitTest.class);

    private static UserDataManager userManager;
    private static LdapConnection connection;
    private static String testUser1 = "testUser";
    private static Dn dnTestUser1;

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

    
    @Before
    public void setUp() throws Exception {
        EntryUtils.setUtilsService(new DefaultOsgiUtilsService(bundleContext));
        setupUserManager();

    }

    private void setupUserManager1() {
        final UserDataManagerLdap userManager = new UserDataManagerLdap();
        userManager.setEntityManager(entityManager);
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                entityManager.getTransaction().begin();
                Object result;
                try {
                    result = method.invoke(userManager, args);
                } catch (InvocationTargetException e) {
                    entityManager.getTransaction().rollback();
                    throw e.getCause();
                }
                entityManager.getTransaction().commit();
                return result;
            }
        };
        this.userManager =
            (UserDataManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{ UserDataManager.class }, invocationHandler);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.PROVIDED_CLASSES_KEY, TestPermission.class.getName());
        props.put(Constants.DELEGATION_CONTEXT_KEY, org.openengsb.core.api.Constants.DELEGATION_CONTEXT_PERMISSIONS);
        ClassProvider permissionProvider =
            new ClassProviderImpl(bundle, Sets.newHashSet(TestPermission.class.getName()));
        registerService(permissionProvider, props, ClassProvider.class);

    }
    
    
    private static UserDataManager setupUserManager() {
        UserDataManagerLdap m = new UserDataManagerLdap();
        m.setLdapDao(new LdapDao(connection));
        return m;
    }

    private static LdapNetworkConnection setupNetworkConnection() throws Exception {
        LdapNetworkConnection c = new LdapNetworkConnection("localhost", 10389);
        c.setTimeOut(0);
        c.connect();
        LOGGER.info(c.toString());
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(new Dn("uid=admin,ou=system"));
        bindRequest.setCredentials("secret");
        c.bind(bindRequest);
        NetworkSchemaLoader nsl = new NetworkSchemaLoader(c);
        c.loadSchema(nsl);
        return c;
    }

    private static void setupTests() throws Exception {
        dnTestUser1 = new Dn(String.format("cn=%s,ou=users,ou=userdata,dc=openengsb,dc=org", testUser1));
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//        
//        connection = setupNetworkConnection();
//        userManager = setupUserManager();
//        setupTests();
//    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        connection.unBind();
        connection.close();
    }

    @Before
    // TODO make some resetServer() method or even better applyLdifs
    public void doBefore() throws Exception {
        EntryUtils.setUtilsService(new DefaultOsgiUtilsService(bundleContext));
        connection = setupNetworkConnection();
        userManager = setupUserManager();
        setupTests();
        clearDIT();
    }

    private void clearDIT() {
        ((UserDataManagerLdap) userManager).getDao().deleteSubtreeExcludingRoot(SchemaConstants.ouUsers());
        ((UserDataManagerLdap) userManager).getDao().deleteSubtreeExcludingRoot(
            SchemaConstants.ouGlobalPermissionSets());
    }

    /*--------------- users -----------------*/

    @Test
    public void testDeleteNonExistingUser_shouldDoNothing() throws Exception {
        assertThat(connection.exists(dnTestUser1), is(false));
        userManager.deleteUser(testUser1);
    }

    @Test(expected = UserExistsException.class)
    public void testCreateExistingUser_shouldThrowUserExistsException() throws Exception {
        userManager.createUser(testUser1);
        assertThat(connection.exists(dnTestUser1), is(true));
        userManager.createUser(testUser1);
    }

    @Test
    public void testGetAllUsers_shouldReturnAllUsers() throws Exception {
        String testUser2 = "testUser2";
        String testUser3 = "testUser3";
        userManager.createUser(testUser1);
        userManager.createUser(testUser2);
        userManager.createUser(testUser3);
        Collection<String> userList = userManager.getUserList();
        assertThat(userList, not(nullValue()));
        assertThat(userList.size(), is(3));
        assertThat(userList, hasItems(testUser1, testUser2, testUser3));
    }

    /*--------------- credentials -----------------*/

    @Test
    public void testSetUserCredentialsNullValue_shouldPersistNullValue() throws Exception {
        String credentialsName = "testCredentials";
        userManager.createUser(testUser1);
        userManager.setUserCredentials(testUser1, credentialsName, null);
        String returnedCredentials = userManager.getUserCredentials(testUser1, credentialsName);
        assertThat(returnedCredentials, nullValue());
    }

    @Test
    public void testSetUserCredentialsEmptyString_shouldPersistEmptyString() throws Exception {
        String credentialsName = "testCredentials";
        String credentialsValue = "";
        userManager.createUser(testUser1);
        userManager.setUserCredentials(testUser1, credentialsName, credentialsValue);
        String returnedCredentials = userManager.getUserCredentials(testUser1, credentialsName);
        assertThat(returnedCredentials, is(credentialsValue));
    }

    @Test
    public void testSetUserCredentials_shouldPersistCredentials() throws Exception {
        String credentialsName = "testCredentials";
        String credentialsValue = "testValue";
        userManager.createUser(testUser1);
        userManager.setUserCredentials(testUser1, credentialsName, credentialsValue);
        String returnedCredentials = userManager.getUserCredentials(testUser1, credentialsName);
        assertThat(returnedCredentials, is(credentialsValue));
    }

    @Test
    public void testSetExistingUserCredentials_shouldOverwrite() throws Exception {
        String credentialsName = "testCredentials";
        String originalValue = "abc";
        String newValue = "xyz";
        userManager.createUser(testUser1);
        userManager.setUserCredentials(testUser1, credentialsName, originalValue);
        String returnedCredentials = userManager.getUserCredentials(testUser1, credentialsName);
        assertThat(returnedCredentials, is(originalValue));
        userManager.setUserCredentials(testUser1, credentialsName, newValue); // overwrite original value
        returnedCredentials = userManager.getUserCredentials(testUser1, credentialsName);
        assertThat(returnedCredentials, is(newValue));
    }

    @Test(expected = UserNotFoundException.class)
    public void testSetCredentialsForNonexistingUser_shouldThrowUserNotFoundException() throws Exception {
        assertThat(connection.exists(dnTestUser1), is(false));
        userManager.setUserCredentials(testUser1, "randomName", "randomValue");
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetCredentialsForNonexistingUser_shouldThrowUserNotFoundException() throws Exception {
        assertThat(connection.exists(dnTestUser1), is(false));
        userManager.getUserCredentials(testUser1, "randomName");
    }

    @Test(expected = NoSuchCredentialsException.class)
    public void testGetNonexistingCredentialsForExistingUser_shouldThrowNoSuchCredentialsException() throws Exception {
        userManager.createUser(testUser1);
        assertThat(connection.exists(dnTestUser1), is(true));
        userManager.getUserCredentials(testUser1, "nonexistingname");
    }

    @Test(expected = UserNotFoundException.class)
    public void testRemoveCredentialsForNonexistingUser_shouldThrowUserNotFoundException() throws Exception {
        assertThat(connection.exists(dnTestUser1), is(false));
        userManager.removeUserCredentials(testUser1, "nonexistingname");
    }

    @Test
    public void testRemoveCredentials_shouldRemoveCredentials() throws Exception {
        userManager.createUser(testUser1);
        String testCredentials = "testCredentials";
        Dn dnTestCredentials = SchemaConstants.userCredentials(testUser1, testCredentials);
        userManager.setUserCredentials(testUser1, testCredentials, "randomValue");
        assertThat(connection.exists(dnTestCredentials), is(true));
        userManager.removeUserCredentials(testUser1, testCredentials);
        assertThat(connection.exists(dnTestCredentials), is(false));
    }

    /*--------------- attributes -----------------*/

    /* attribute values may be empty (ie array with size 0) but not null. */

    @Test
    public void testSetEmptyUserAttribute_shouldPersist() throws Exception {
        String attributeName = "testAttribute";
        Object[] attributeValue = new Object[0];
        userManager.createUser(testUser1);
        userManager.setUserAttribute(testUser1, attributeName, attributeValue);
        List<Object> result = userManager.getUserAttribute(testUser1, attributeName);
        assertThat(result, not(nullValue()));
        assertThat(result.size(), is(0));
    }

    @Test
    public void testOverwriteUserAttribute_shouldOverwrite() throws Exception {
        String attributeName = "testAttribute";
        Object[] originalValue = new Object[] { true, "", 307708 };
        Object[] newValue = new Object[] { "hello", false, 5 };

        userManager.createUser(testUser1);

        userManager.setUserAttribute(testUser1, attributeName, originalValue);
        List<Object> result = userManager.getUserAttribute(testUser1, attributeName);
        assertThat((Boolean) result.get(0), is(originalValue[0]));
        assertThat((String) result.get(1), is(originalValue[1]));
        assertThat((Integer) result.get(2), is(originalValue[2]));

        userManager.setUserAttribute(testUser1, attributeName, newValue);
        result = userManager.getUserAttribute(testUser1, attributeName);
        assertThat((String) result.get(0), is(newValue[0]));
        assertThat((Boolean) result.get(1), is(newValue[1]));
        assertThat((Integer) result.get(2), is(newValue[2]));
    }

    @Test(expected = NoSuchAttributeException.class)
    public void testgetNonexistingUserAttribute_shouldThrowNoSuchAttributeExpcetion() throws Exception {
        userManager.createUser(testUser1);
        userManager.getUserAttribute(testUser1, "non existing attribute");
    }

    @Test(expected = UserNotFoundException.class)
    public void testgetUserAttributeFromNonexistingUser_expectedUserNotFoundException() throws Exception {
        userManager.getUserAttribute("non existing user", "random attribute");
    }

    @Test
    public void testRemoveNonexistingUserAttribute_shouldFailSilent() throws Exception {
        userManager.createUser(testUser1);
        userManager.removeUserAttribute(testUser1, "random attribute");
    }

    @Test(expected = UserNotFoundException.class)
    public void testRemoveUserAttributeFromNonexistingUser_expectedUserNotFoundException() throws Exception {
        userManager.removeUserAttribute("non existing user", "random attribute");
    }

    /*--------------- permissions -----------------*/

    /*
     * What to test: null description insertion order duplicates nonexisting permission set cycles
     */

    @Test
    public void testAddCyclicPermissionSetHierarchy_shouldPersistCycleAndShouldTerminate() throws Exception {
        String parent = "SetA";
        String child = "SetB";
        String grandchild = "SetC";
        userManager.createPermissionSet(parent);
        userManager.createPermissionSet(child);
        userManager.createPermissionSet(grandchild);
        userManager.addPermissionSetToPermissionSet(parent, child);
        userManager.addPermissionSetToPermissionSet(child, grandchild);
        userManager.addPermissionSetToPermissionSet(grandchild, parent);

        List<String> childOfParent = new LinkedList<String>(userManager.getPermissionSetsFromPermissionSet(parent));
        List<String> childOfChild = new LinkedList<String>(userManager.getPermissionSetsFromPermissionSet(child));
        List<String> childOfGrandchild = new LinkedList<String>(
            userManager.getPermissionSetsFromPermissionSet(grandchild));

        assertThat(childOfParent.size(), is(1));
        assertThat(childOfChild.size(), is(1));
        assertThat(childOfGrandchild.size(), is(1));

        assertThat(childOfParent, hasItem(child));
        assertThat(childOfChild, hasItem(grandchild));
        assertThat(childOfGrandchild, hasItem(parent));
    }

    @Test
    public void testAddPermissionSetToPermissionSet_shouldPersistChildrenSetsAndPreserverOrder() throws Exception {
        String parent = "SetA";
        String[] childrenSets = new String[] { "setB", "setC", "111", "777", "aaa", "222" };
        userManager.createPermissionSet(parent);
        for (String s : childrenSets) {
            userManager.createPermissionSet(s);
        }
        userManager.addPermissionSetToPermissionSet(parent, childrenSets);
        String[] result = userManager.getPermissionSetsFromPermissionSet(parent).toArray(new String[0]);
        assertThat(Arrays.equals(childrenSets, result), is(true));
    }

    @Test
    public void testAddPermissionSetToUser_shouldPersistSetsAndPreserverOrder() throws Exception {
        String[] sets = new String[] { "setB", "setC", "111", "777", "aaa", "222" };
        for (String s : sets) {
            userManager.createPermissionSet(s);
        }
        userManager.createUser(testUser1);
        userManager.addPermissionSetToUser(testUser1, sets);
        String[] result = userManager.getPermissionSetsFromUser(testUser1).toArray(new String[0]);
        assertThat(Arrays.equals(sets, result), is(true));
    }

    @Test
    public void testGetPermissionsForUser_shouldReturnPermissions() throws Exception {
        Permission permission = new TestPermission(Access.GRANTED);
        userManager.createUser(testUser1);
        userManager.addPermissionToUser(testUser1, permission);
        Collection<Permission> permissions = userManager.getPermissionsForUser(testUser1);
        assertThat(permissions, hasItem(permission));
        assertThat(permissions.size(), is(1));
    }

    @Test
    public void testCreatePermissionSet_shouldPersistSetAndPreserveOrder() throws Exception {
        String set = "SetA";
        Permission p1 = new TestPermission(Access.GRANTED);
        Permission p2 = new TestPermission(Access.DENIED);
        Permission p3 = new TestPermission(Access.ABSTAINED);
        Permission p4 = new TestPermission(Access.GRANTED);
        Permission[] permissions = new Permission[] { p1, p2, p3, p4 };
        userManager.createPermissionSet(set, permissions);
        List<Permission> result = new LinkedList<Permission>(userManager.getPermissionsFromPermissionSet(set));
        assertThat(result.get(0), is(p1));
        assertThat(result.get(1), is(p2));
        assertThat(result.get(2), is(p3));
        assertThat(result.get(3), is(p4));
        assertThat(result.size(), is(4));
    }

    @Test(expected = UserNotFoundException.class)
    public void testAddPermissionsToNonexistingUser_expectedUserNotFoundException() throws Exception {
        Permission p = new TestPermission(Access.GRANTED);
        userManager.addPermissionToUser("nonExistingUser", p);
    }

    @Test(expected = UserNotFoundException.class)
    public void testAddPermissionSetToNonexistingUser_expectedUserNotFoundException() throws Exception {
        String set = "setA";
        userManager.createPermissionSet(set);
        userManager.addPermissionSetToUser("nonExistingUser", set);
    }

    @Test(expected = PermissionSetNotFoundException.class)
    public void testAddNonExistingPermissionSet_expectedPermissionSetNotFoundException() throws Exception {
        String set = "setA";
        userManager.addPermissionSetToUser("nonExistingUser", set);
    }

    @Test(expected = PermissionSetNotFoundException.class)
    public void testAddPermissionsToNonexistingSet_expectedPermissionSetNotFoundException() throws Exception {
        Permission p = new TestPermission(Access.GRANTED);
        userManager.addPermissionToSet("nonExistingSet", p);
    }

    /*--------------- permission set attributes -----------------*/

    @Test
    public void testSetPermissionSetAttribute_shouldPersistAttribute() throws Exception {
        String permissionSet = "setA";
        String attributeName = "testAttribute";
        String attributeValue = "testValue";
        userManager.createPermissionSet(permissionSet);
        userManager.setPermissionSetAttribute(permissionSet, attributeName, attributeValue);
        String result = userManager.getPermissionSetAttribute(permissionSet, attributeName);
        assertThat(result, is(attributeValue));
    }

    @Test(expected = PermissionSetNotFoundException.class)
    public void testSetPermissionSetAttributeToNenexistingSet_expectPermissionSetNotFoundException() throws Exception {
        userManager.setPermissionSetAttribute("non existing set", "random name", "random value");
    }

    @Test(expected = NoSuchAttributeException.class)
    public void testGetNonExistingPermissionSetAttribute_expectedNoSuchAttributeException() throws Exception {
        String permissionSet = "setA";
        userManager.createPermissionSet(permissionSet);
        userManager.getPermissionSetAttribute(permissionSet, "random name");
    }

//    @Override
//    protected void setBundleContext(BundleContext bundleContext) {
//        Dictionary<String, Object> properties = new Hashtable<String, Object>();
//        properties.put("permissionClass", TestPermission.class.getName());
//        PermissionProvider permissionProvider = new AbstractPermissionProvider(TestPermission.class) {
//        };
//        registerService(permissionProvider, properties, PermissionProvider.class);
//        DefaultOsgiUtilsService osgiServiceUtils = new DefaultOsgiUtilsService();
//        osgiServiceUtils.setBundleContext(bundleContext);
//        registerService(osgiServiceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
//        OpenEngSBCoreServices.setOsgiServiceUtils(osgiServiceUtils);
//    }

}
