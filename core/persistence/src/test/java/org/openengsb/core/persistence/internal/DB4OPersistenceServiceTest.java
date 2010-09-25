package org.openengsb.core.persistence.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.openengsb.core.persistence.PersistenceService;
import org.openengsb.core.persistence.PersistenceServiceTest;

public class DB4OPersistenceServiceTest extends PersistenceServiceTest {

    private DB4OPersistenceService persistence;

    @Override
    protected PersistenceService createPersitenceService() throws Exception {
        persistence = new DB4OPersistenceService();
        persistence.setDbFile("db.data");
        persistence.init();
        return persistence;
    }

    @After
    public void tearDown() throws IOException {
        persistence.shutdown();
        FileUtils.forceDelete(new File("db.data"));
    }

}
