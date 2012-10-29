/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.edb.jpa.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.dao.DefaultJPADao;
import org.openengsb.core.edb.jpa.internal.dao.JPADao;

public class AbstractEDBTest {
    protected TestEDBService db;

    private static final String[] RANDOMKEYS = new String[]{
        "Product", "Handler", "RandomKey", "UserID", "Code", "Auto"
    };

    @Before
    public void initDB() throws Exception {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getAuthenticatedPrincipal()).thenReturn("testuser");
        Properties props = new Properties();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("edb-test", props);
        EntityManager em = emf.createEntityManager();
        JPADao dao = new DefaultJPADao(em);
        EDBPreCommitHook preCommitHook = new CheckPreCommitHook(dao);
        ContextHolder.get().setCurrentContextId("testcontext");

        db = new TestEDBService(dao, authenticationContext, null, Arrays.asList(preCommitHook), null, null, true, em);
        db.open();
    }

    @After
    public void closeDB() {
        db.close();
    }

    /**
     * Returns an EDBCommit object.
     */
    protected EDBCommit getEDBCommit() {
        return db.createEDBCommit(null, null, null);
    }

    /**
     * Creates a new commit object, adds the given inserts, updates and deletes and commit it.
     */
    protected Long commitObjects(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes) {
        EDBCommit ci = db.createEDBCommit(inserts, updates, deletes);
        return db.commit(ci);
    }

    /**
     * Adds an EDBObjectEntry based on the given key and value to the given map
     */
    protected void putValue(String key, Object value, Map<String, EDBObjectEntry> map) {
        map.put(key, new EDBObjectEntry(key, value, value.getClass()));
    }

    /**
     * Returns a random test EDBObject
     */
    protected EDBObject createRandomTestObject(String oid) {
        Random random = new Random(System.currentTimeMillis());
        EDBObject result = new EDBObject(oid);
        int max = 5;

        for (int i = 0; i < max; ++i) {
            String key = RANDOMKEYS[random.nextInt(RANDOMKEYS.length)] + Integer.toString(i);
            String value = "key value " + Integer.toString(random.nextInt(100));
            result.putEDBObjectEntry(key, value);
        }
        return new EDBObject(oid);
    }

    /**
     * Iterates through the list of timestamps and checks if every timestamp is bigger than 0
     */
    protected void checkTimeStamps(List<Long> timestamps) {
        for (Long timestamp : timestamps) {
            assertThat(timestamp, greaterThan((long) 0));
        }
    }

    /**
     * Returns the EDBObject of the given list with the given oid.
     */
    protected EDBObject getEDBObjectOutOfList(List<EDBObject> objects, String oid) {
        for (EDBObject o : objects) {
            if (o.getOID().equals(oid)) {
                return o;
            }
        }
        return null;
    }

}
