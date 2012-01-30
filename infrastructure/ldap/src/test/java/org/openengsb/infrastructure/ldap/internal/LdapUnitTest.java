package org.openengsb.infrastructure.ldap.internal;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.AddResponse;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.schema.AttributeType;
import org.apache.directory.shared.ldap.model.schema.ObjectClass;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
import org.apache.directory.shared.ldap.model.schema.registries.DefaultSchema;
import org.apache.directory.shared.ldap.model.schema.registries.Schema;
import org.apache.directory.shared.ldap.model.schema.registries.SchemaLoader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.NoSuchAttributeException;
import org.openengsb.core.api.security.service.NoSuchCredentialsException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
//import org.apache.directory.server.core.annotations.ApplyLdifFiles;
//import org.apache.directory.server.core.annotations.ApplyLdifs;
import org.apache.directory.server.core.integ.FrameworkRunner;


//@RunWith(FrameworkRunner.class)
//@CreateLdapServer(
//    transports =
//      { 
//        @CreateTransport(protocol = "LDAP") 
//      })
public class LdapUnitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUnitTest.class);

    private static UserDataManager userManager;
    private static LdapConnection connection;
    private static String testUserName = "testUser";
    private static String testUserName2 = "testUser2";
    private static String testCredentials = "password";
    private static String testAttributeName = "testAttribute";
    private Object[] testAttributeValue = new Object[] {new Boolean(true), new String("abc")};
    private static String dnTestUser;
    private static String dnTestCredentials;
    private static String dnTestAttribute;

    private static UserDataManager setupUserManager(){
        connection = new LdapNetworkConnection("localhost", 10389);
        UserDataManagerLdap userManager = new UserDataManagerLdap();
        userManager.setConnection(connection);


        userManager.loadSchemaManager();
        return userManager;
    }

    @BeforeClass
    //@ApplyLdifFiles ("/home/cc/openengsb_ldap.ldif")
    public static void setUpClass() throws Exception {
        //        dnTestUser = String.format("uid=%s,ou=users,dc=openengsb,dc=org", testUserName);
        //        dnTestCredentials = String.format("cn=%s,ou=credentials,%s", testCredentials, dnTestUser);
        //        dnTestAttribute = String.format("cn=%s,ou=attributes,%s", testAttributeName, dnTestUser);
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

    //@Before
    public void clear() throws Exception{
        userManager.deleteUser(testUserName);
        userManager.deleteUser(testUserName2);
    }

    @Test
    public void testCreateAndDeleteNewUser_shouldSucceed() throws Exception {
        SchemaManager schemaManager;
        //String openengsb = "openengsb";
        
        //Schema openengsbSchema = new DefaultSchema(openengsb);

        connection.loadSchema();
        schemaManager = connection.getSchemaManager();
        schemaManager.initialize();
        
        SchemaLoader loader = schemaManager.getLoader();
        Collection<Schema> schemas = loader.getAllSchemas();
        
        for(Schema s : schemas){
            System.out.println(s);
        }
        
//        String oid = "1.3.6.1.4.1.78048.0.4.2.2.1";
//        AttributeType crazy = schemaManager.getAttributeType(oid);
//        System.out.println(crazy);
//        AttributeType bla = schemaManager.getAttributeType("1.3.6.1.4.1.18060.0.4.1.2.0");
//        System.out.println(bla);
//        
//        schemaManager.getGlobalOidRegistry().getSchemaObject(oid);
        
//        String objectclass = "objectclass";
//        
//        Dn dn = new Dn("openengsb-permissionSetAttributeValue=HELLO,ou=system");
//        
//        Entry e = new DefaultEntry();
//        
//        //assertThat(e.isSchemaAware(), is(true));
//        
//        e.setDn(dn);
//        
//        e.add(objectclass, "top");
//        e.add(objectclass, "openengsb-permissionSetAttribute");
//        e.add("openengsb-permissionSetAttributeValue", "HELLO");
//        
//        AddRequest r = new AddRequestImpl();
//
//        r.setEntry(e);
//        AddResponse addResponse = connection.add(r);

        //assertThat(addResponse.getLdapResult().getResultCode(), is(ResultCodeEnum.SUCCESS));
        



//        AttributeType javaClassNameAttributeType = schemaManager.getAttributeType("javaClassName");
//
//        System.out.println(javaClassNameAttributeType != null);
//        System.out.println(javaClassNameAttributeType);
//
//        AttributeType psav = schemaManager.getAttributeType("openengsb-permissionSetAttributeValue");
//
//        System.out.println(psav != null);
//        System.out.println(psav);
//
//        List<Schema> enabledSchemas = schemaManager.getEnabled();
//
//        System.out.println("enabled");
//        for(Schema s : enabledSchemas){
//            System.out.println(s);
//        }
//
//        List<Schema> disabledSchemas = schemaManager.getDisabled();
//
//        System.out.println("disabled");
//        for(Schema s : disabledSchemas){
//            System.out.println(s);
//        }
//
//        System.out.println("---");
//        System.out.println("exists permissionSetAttribute? " + connection.exists("m-oid=1.3.6.1.4.1.78048.0.4.2.2.1,ou=attributeTypes,cn=openengsb,ou=schema"));
//        System.out.println("exists openengsb schema? " + connection.exists("cn=openengsb,ou=schema"));
//        System.out.println("---");
//        //ObjectClass testobj = schemaManager.getObjectClassRegistry().lookup("1.3.6.1.4.1.8748.0.2.2.6.200");
//        
//        System.out.println("contains oid?" + schemaManager.getGlobalOidRegistry().contains("1.3.6.1.4.1.78048.0.4.2.2.1"));
//        
//        schemaManager.load("core");
//        AttributeType mom = schemaManager.getAttributeType("MotherOfGod");
//        System.out.println("asdfasdfasdfadsfasfasdf");
//        System.out.println(mom != null);
//        System.out.println(mom);
        
        // testatt = schemaManager.("testObjectClass");
        //        
        //        LOGGER.warn(new Boolean(connection.exists("ou=schema")).toString());
        //        LOGGER.warn(new Boolean(connection.exists("m-oid=1.3.6.1.4.9997,ou=attributeTypes,cn=openengsb,ou=schema")).toString());

    }

    //    @Test
    //    public void testDeleteNonExistingUser_shouldDoNothing() throws Exception {
    //        assertThat(connection.exists(dnTestUser), is(false));
    //        userManager.deleteUser(testUserName);
    //        assertThat(connection.exists(dnTestUser), is(false));
    //    }
    //
    //    @Test(expected = UserExistsException.class)
    //    public void testCreateExistingUser_expectedUserExistsException() throws Exception {
    //        userManager.createUser(testUserName);
    //        assertThat(connection.exists(dnTestUser), is(true));
    //        userManager.createUser(testUserName);
    //    }
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
    //    @Test
    //    public void testSetCredentials_shouldPersistCredentials() throws Exception{
    //        userManager.createUser(testUserName);
    //        userManager.setUserCredentials(testUserName, testCredentials, "testpw");
    //        assertThat(connection.exists(dnTestCredentials), is(true));
    //    }
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
    //    @Test(expected = UserNotFoundException.class)
    //    public void testSetCredentials_expectedUserNotFoundException() throws Exception{
    //        assertThat(connection.exists(dnTestUser), is(false));
    //        userManager.setUserCredentials(testUserName, testCredentials, "testpw");
    //    }
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
    //    
    //    @Test
    //    public void testSetAttribute_shouldPersistAttribute() throws Exception{
    //        userManager.createUser(testUserName);
    //        userManager.setUserAttribute(testUserName, testAttributeName, testAttributeValue);
    //        assertThat(connection.exists(dnTestAttribute), is(true));
    //    }
    //    
    //    @Test
    //    public void testSetAttribute_shouldUpdateExistingAttribute() throws Exception{
    //        userManager.createUser(testUserName);
    //        userManager.setUserAttribute(testUserName, testAttributeName, testAttributeValue);
    //        assertThat(connection.exists(dnTestAttribute), is(true));
    //        userManager.setUserAttribute(testUserName, testAttributeName, new Object[0]);
    //        assertThat(connection.exists(dnTestAttribute), is(true));
    //        List<Object> attribute = userManager.getUserAttribute(testUserName, testAttributeName);
    //        assertThat(attribute, not(nullValue()));
    //        assertThat(attribute, is(Collections.EMPTY_LIST));
    //    }
    //    
    //    @Test
    //    public void testGetAttribute_shouldHaveCorrectType() throws Exception{
    //        userManager.createUser(testUserName);
    //        userManager.setUserAttribute(testUserName, testAttributeName, testAttributeValue);
    //        List<Object> attribute = userManager.getUserAttribute(testUserName, testAttributeName);
    //        assertThat(attribute, not(nullValue()));
    //        assertThat(attribute.size(), is(2));
    //        Object attribute0 = attribute.get(0);
    //        assertThat(attribute0, is(testAttributeValue[0].getClass()));
    //        assertThat((Boolean)attribute0, is(testAttributeValue[0]));
    //        Object attribute1 = attribute.get(1);        
    //        assertThat(attribute1, is(testAttributeValue[1].getClass()));
    //        assertThat((String)attribute1, is(testAttributeValue[1]));
    //    }
    //    
    //    @Test(expected = UserNotFoundException.class)
    //    public void testSetAttribute_expectedUserNotFoundException() throws Exception{
    //        assertThat(connection.exists(dnTestUser), is(false));
    //        userManager.setUserAttribute(testUserName, testAttributeName, testAttributeValue);        
    //    }
    //    
    //    @Test(expected = UserNotFoundException.class)
    //    public void testRemoveAttribute_expectedUserNotFoundException() throws Exception{
    //        assertThat(connection.exists(dnTestUser), is(false));
    //        userManager.removeUserAttribute(testUserName, testAttributeName);
    //    }
    //    
    //    @Test
    //    public void testRemoveAttribute_shouldDoNothingBecauseAttributeDoesNotExist() throws Exception{
    //        userManager.createUser(testUserName);
    //        userManager.removeUserAttribute(testUserName, testAttributeName);
    //        assertThat(connection.exists(dnTestUser), is(true));
    //    }
    //    
    //    @Test(expected = UserNotFoundException.class)
    //    public void testGetAttribute_expectedUserNotFoundException() throws Exception{
    //        assertThat(connection.exists(dnTestUser), is(false));
    //        userManager.getUserAttribute(testUserName, testAttributeName);
    //    }
    //    
    //    @Test(expected = NoSuchAttributeException.class)
    //    public void testGetAttribute_expectedNoSuchAttributeException() throws Exception{
    //        userManager.createUser(testUserName);
    //        assertThat(connection.exists(dnTestUser), is(true));
    //        userManager.getUserAttribute(testUserName, testAttributeName);
    //    }
    //    
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
    //            // TODO Auto-generated method stub
    //            return null;
    //        }
    //        
    //    }
    //
    //    private class PermissionImpl implements Permission{
    //
    //        @Override
    //        public String describe() {
    //            // TODO Auto-generated method stub
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
