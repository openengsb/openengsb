package org.openengsb.persistence;

public class PersistenceEndpointExistTest extends PersistenceEndpointTest {
    @Override
    protected PersistenceInternal getPersistenceImpl() {
        return new PersistenceInternalExistXmlDB();
    }
}
