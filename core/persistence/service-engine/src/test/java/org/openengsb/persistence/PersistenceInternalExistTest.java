package org.openengsb.persistence;


public class PersistenceInternalExistTest extends PersistenceInternalTest {

    @Override
    protected PersistenceInternal getPersistenceImpl() throws Exception {
        PersistenceInternalExistXmlDB p = new PersistenceInternalExistXmlDB();
        p.reset();
        return p;
    }

}
