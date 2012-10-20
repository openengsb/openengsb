package org.openengsb.infrastructure.ldap.internal.test;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.infrastructure.ldap.internal.LdapDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapDaoTest {

    private Dn baseDn;
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDaoTest.class);
    private LdapDao dao;
    
    @Before
    public void doBefore() throws LdapInvalidDnException {
        baseDn = new Dn("ou=test,dc=openengsb,dc=org");
        dao = new LdapDao("localhost", 10389);
        dao.connect("uid=admin,ou=system", "secret");
    }
    
    @After
    public void tearDown() {
        dao.disconnect();
    }
    
    @Test
    public void testStore_expectPersistedEntry(){
        
    }
    
}
