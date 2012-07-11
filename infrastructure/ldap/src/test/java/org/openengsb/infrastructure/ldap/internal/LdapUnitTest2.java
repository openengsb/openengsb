package org.openengsb.infrastructure.ldap.internal;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.shared.client.api.LdapApiIntegrationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FrameworkRunner.class)
@CreateLdapServer(
    transports = { 
    @CreateTransport(
        protocol = "LDAP"
        //, port=10389
    )
    , @CreateTransport(protocol = "LDAPS")
})
public class LdapUnitTest2 
extends AbstractLdapTestUnit
{

    private LdapNetworkConnection connection;

    @Before
    public void setup() throws Exception
    {
        connection = LdapApiIntegrationUtils.getPooledAdminConnection( getLdapServer() );
    }

    @After
    public void shutdown() throws Exception
    {
        LdapApiIntegrationUtils.releasePooledAdminConnection( connection, getLdapServer() );
    }

//    @ApplyLdifs( { 
//        "dn: ou=bla,ou=system\n" +
//                "changetype: add\n" +
//                "objectclass: organizationalUnit\n" +
//                "objectclass: top\n" +
//                "ou: bla\n\n"
//    })
    @Test
    public void randomTest() throws Exception{
        assertThat(connection, not(nullValue()));
    }
    
}

