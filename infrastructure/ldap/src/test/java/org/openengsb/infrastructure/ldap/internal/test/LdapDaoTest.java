package org.openengsb.infrastructure.ldap.internal.test;

import org.apache.directory.ldap.client.api.DefaultSchemaLoader;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.schema.registries.SchemaLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapDaoTest {

    private Dn baseDn;
    private LdapConnection connection;
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDaoTest.class);
    
    private void setupConnection() throws Exception {
        connection = new LdapNetworkConnection("localhost", 10389);
        connection.setTimeOut(0);
        connection.connect();
        LOGGER.info(connection.toString());
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setDn(new Dn("uid=admin,ou=system"));
        bindRequest.setCredentials("secret");
        connection.bind(bindRequest);
        SchemaLoader schemaLoader = new DefaultSchemaLoader(connection);
        ((LdapNetworkConnection)(connection)).loadSchema(schemaLoader);
    }
    
}
