package org.openengsb.infrastructure.ldap.internal;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class LdapActivator implements BundleActivator {
    
    LdapConnection connection;

    @Override
    public void start(BundleContext context) throws Exception {
        connection = new LdapNetworkConnection( "localhost", 10389 );
        connection.connect();
        
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        connection.close();

    }

}
