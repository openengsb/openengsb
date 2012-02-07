package org.openengsb.infrastructure.ldap.internal;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.NetworkSchemaLoader;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.infrastructure.ldap.internal.dao.LdapDao;

public abstract class EpicTestClass {
    
    protected UserDataManager userManager;
    protected LdapConnection connection;
    protected String testUserName = "testUser";
    protected String testUserName2 = "testUser2";
    
    private void setupUserManager(){
        final UserDataManagerLdapNEW userManager = new UserDataManagerLdapNEW();        
        userManager.setLdapDao(new LdapDao(connection));
        this.userManager = userManager;
    }

    private void setupConnection() throws Exception{
        final LdapNetworkConnection c = new LdapNetworkConnection("localhost", 10389);
        
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(new Dn("uid=admin,ou=system"));
        bindRequest.setCredentials("secret");
        
        c.setTimeOut(0);
        c.connect();
        c.bind(bindRequest);
        
        NetworkSchemaLoader networkSchemaLoader = new NetworkSchemaLoader(c);
        
        c.loadSchema(networkSchemaLoader);
        this.connection = c;
    }

}
