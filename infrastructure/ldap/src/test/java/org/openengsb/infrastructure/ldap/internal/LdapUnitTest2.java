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
import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
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
import org.openengsb.infrastructure.ldap.internal.model.LdapDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LdapUnitTest2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUnitTest2.class);

    private static UserDataManager userManager;
    private static LdapConnection connection;
    private static String testUserName = "testUser";
    private static String testUserName2 = "testUser2";
    private static String testCredentialsName = "password";
    private static String testCredentialsName2 = "password";
    private static String testAttributeName = "testAttribute";
    private static String testAttributeName2 = "testAttribute2";
    private Object[] testAttributeValue = new Object[] {new Boolean(true), new String("abc")};
    private Object[] testAttributeValue2 = new Object[] {new String("xyz"), new Boolean(false)};
    private static String testPermissionSetName = "set1";
    private static String testPermissionSetName2 = "set2";
    
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
    
    private static Dn dnTestUser;
    private static Dn dnTestUser2;
    private static Dn dnTestCredentials;
    private static Dn dnTestCredentials2;
    private static Dn dnTestAttribute;
    private static Dn dnTestAttribute2;
    private static Dn dnTestPermissionSet;
    private static Dn dnTestPermission;


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
        
        SchemaManager schemaManager = c.getSchemaManager();
        String oid = schemaManager.getAttributeTypeRegistry().getOidByName("openengsb-maxId");
        
        System.out.println(oid);
        
        return c;
    }

    private static void setupTests() throws Exception{
        
        dnTestUser = new Dn(String.format("cn=%s,ou=users,ou=userdata,dc=openengsb,dc=org", testUserName));
        dnTestUser2 = new Dn(String.format("cn=%s,ou=users,ou=userdata,dc=openengsb,dc=org", testUserName2));
        Dn ou = new Dn(new Rdn("ou=attributes"),dnTestUser);
        dnTestAttribute = new Dn(new Rdn(String.format("cn=%s", testAttributeName)),ou);
        ou = new Dn(new Rdn("ou=attributes"),dnTestUser2);
        dnTestAttribute2 = new Dn(new Rdn(String.format("cn=%s", testAttributeName)),ou);
        ou = new Dn(new Rdn("ou=credentials"),dnTestUser);
        dnTestCredentials = new Dn(new Rdn(String.format("cn=%s", testCredentialsName)),ou);
        ou = new Dn(new Rdn("ou=credentials"),dnTestUser2);
        dnTestCredentials2 = new Dn(new Rdn(String.format("cn=%s", testCredentialsName)),ou);
        dnTestPermissionSet = new Dn("cn",testPermissionSetName,"ou=permissionSets,ou=userdata,dc=openengsb,dc=org");
        ou = new Dn(new Rdn("ou=direct"),dnTestPermissionSet);
        //dnTestPermission = new Dn(new Rdn(),ou);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //connection = setupConnection();
        
        connection = setupNetworkConnection();
        userManager = setupUserManager();
        setupTests();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        connection.unBind();
        connection.close();
    }

    @Before
    public void doBefore() throws Exception{
        userManager.deleteUser(testUserName);
        userManager.deleteUser(testUserName2);
        userManager.createUser(testUserName);
        userManager.setUserAttribute(testUserName, testAttributeName, testAttributeValue);
    }
    
    //@After
    public void doAfter() throws Exception{
        userManager.deleteUser(testUserName);
        userManager.deleteUser(testUserName2);
    }
    
    @Test
    public void randomTest() throws Exception{
        String description = "Horsepower MAX";
        Permission p = new PermissionImpl(description);
        userManager.createPermissionSet(testPermissionSetName, p);
        assertThat(connection.exists(dnTestPermissionSet), is(true));
    }



    
}

