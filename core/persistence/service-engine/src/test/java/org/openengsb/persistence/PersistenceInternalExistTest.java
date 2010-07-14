package org.openengsb.persistence;

public class PersistenceInternalExistTest extends PersistenceInternalTest {

    public PersistenceInternalExistTest(Class<?> objectClass, Object o1, Object sample1, Object udpated1) {
        super(objectClass, o1, sample1, udpated1);
    }

    @Override
    protected PersistenceInternal getPersistenceImpl() throws Exception {
        PersistenceInternalExistXmlDB p = new PersistenceInternalExistXmlDB();
        p.reset();
        return p;
    }

}
