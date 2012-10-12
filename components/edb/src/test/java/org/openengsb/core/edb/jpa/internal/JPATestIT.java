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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;

import com.google.common.collect.Maps;

public class JPATestIT {
    private static JPADatabase db;
    private static Utils utils;

    @Before
    public void initDB() throws Exception {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        utils = new Utils();
        db = new JPADatabase();
        db.setAuthenticationContext(authenticationContext);
        Properties props = new Properties();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("edb-test", props);
        EntityManager em = emf.createEntityManager();
        db.open(em);
        EDBPreCommitHook preCommitHook = new CheckPreCommitHook(em);
        db.setPreCommitHooks(Arrays.asList(preCommitHook));
    }

    @After
    public void closeDB() {
        db.close();
    }

    @Test
    public void testOpenDatabase_shouldWork() throws Exception {
        assertThat(db, notNullValue());
    }

    @Test
    public void testCommit_shouldWork() throws Exception {
        JPACommit ci = db.createCommit("TestCommit", "Role");
        EDBObject obj = new EDBObject("Tester");
        obj.putEDBObjectEntry("Test", "Hooray");
        ci.insert(obj);

        long time = db.commit(ci);

        obj = null;
        obj = db.getObject("Tester");
        String hooray = obj.getString("Test");

        assertThat(obj, notNullValue());
        assertThat(hooray, notNullValue());

        checkTimeStamps(Arrays.asList(time));
    }

    @Test
    public void testGetCommits_shouldWork() throws Exception {
        JPACommit ci = db.createCommit("TestCommit2", "Testcontext");
        EDBObject obj = new EDBObject("TestObject");
        obj.putEDBObjectEntry("Bla", "Blabla");
        ci.insert(obj);

        long time = db.commit(ci);

        List<EDBCommit> commits1 = db.getCommitsByKeyValue("context", "Testcontext");
        List<EDBCommit> commits2 = db.getCommitsByKeyValue("context", "DoesNotExist");

        assertThat(commits1.size(), is(1));
        assertThat(commits2.size(), is(0));

        checkTimeStamps(Arrays.asList(time));
    }

    @Test(expected = EDBException.class)
    public void testGetInexistantObject_shouldThrowException() throws Exception {
        db.getObject("/this/object/does/not/exist");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetHistoryAndCheckForElements_shouldWork() throws Exception {
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        long time4 = 0;
        HashMap<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("Lock", "Key", data1);
        Utils.putValue("Door", "Bell", data1);
        Utils.putValue("Cat", "Spongebob", data1);
        EDBObject v1 = new EDBObject("/history/object", data1);
        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(utils.createRandomTestObject("/useless/1"));
        ci.insert(v1);

        time1 = db.commit(ci);

        HashMap<String, EDBObjectEntry> data2 = (HashMap<String, EDBObjectEntry>) data1.clone();
        Utils.putValue("Lock", "Smith", data2);
        EDBObject v2 = new EDBObject("/history/object", data2);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(utils.createRandomTestObject("/useless/2"));
        ci.update(v2);

        time2 = db.commit(ci);

        HashMap<String, EDBObjectEntry> data3 = (HashMap<String, EDBObjectEntry>) data2.clone();
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(utils.createRandomTestObject("/useless/3"));
        ci.insert(utils.createRandomTestObject("/useless/4"));
        time3 = db.commit(ci);

        Utils.putValue("Cat", "Dog", data3);
        EDBObject v3 = new EDBObject("/history/object", data3);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.update(v3);
        ci.insert(utils.createRandomTestObject("/useless/5"));

        time4 = db.commit(ci);

        List<EDBObject> history = db.getHistory("/history/object");

        boolean ordered = true;
        for (int i = 1; i < 3; i++) {
            if (history.get(i - 1).getTimestamp() > history.get(i).getTimestamp()) {
                ordered = false;
            }
        }
        assertThat(ordered, is(true));
        assertThat(history.get(0).getString("Lock"), is("Key"));
        assertThat(history.get(0).getString("Cat"), is("Spongebob"));

        assertThat(history.get(1).getString("Lock"), is("Smith"));
        assertThat(history.get(1).getString("Cat"), is("Spongebob"));

        assertThat(history.get(2).getString("Lock"), is("Smith"));
        assertThat(history.get(2).getString("Cat"), is("Dog"));

        checkTimeStamps(Arrays.asList(time1, time2, time3, time4));
    }

    @Test
    public void testHistoryOfDeletion_shouldWork() throws Exception {
        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(utils.createRandomTestObject("/deletion/1"));
        long time1 = db.commit(ci);

        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.delete("/deletion/1");
        long time2 = db.commit(ci);

        List<EDBObject> history = db.getHistory("/deletion/1");

        assertThat(history.size(), is(2));
        assertThat(history.get(0).isDeleted(), is(false));
        assertThat(history.get(1).isDeleted(), is(true));

        checkTimeStamps(Arrays.asList(time1, time2));
    }

    @Test
    public void testGetLog_shouldWork() throws Exception {
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        long time4 = 0;
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("Burger", "Salad", data1);
        Utils.putValue("Bla", "Blub", data1);
        Utils.putValue("Cheese", "Butter", data1);
        EDBObject v1 = new EDBObject("/history/test/object", data1);
        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(utils.createRandomTestObject("/useless/test/1"));
        ci.insert(utils.createRandomTestObject("/deletion/test/1"));
        ci.insert(v1);

        time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = Maps.newHashMap(data1);
        Utils.putValue("Burger", "Meat", data2);
        EDBObject v2 = new EDBObject("/history/test/object", data2);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(utils.createRandomTestObject("/useless/test/2"));
        ci.delete("/deletion/test/1");
        ci.update(v2);
        time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data3 = Maps.newHashMap(data2);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(utils.createRandomTestObject("/useless/test/3"));
        ci.insert(utils.createRandomTestObject("/useless/test/4"));
        time3 = db.commit(ci);
        Utils.putValue("Cheese", "Milk", data3);

        EDBObject v3 = new EDBObject("/history/test/object", data3);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.update(v3);
        ci.insert(utils.createRandomTestObject("/useless/test/5"));

        time4 = db.commit(ci);

        List<EDBLogEntry> log = db.getLog("/history/test/object", time1, time4);
        assertThat(log.size(), is(3));

        checkTimeStamps(Arrays.asList(time1, time2, time3, time4));
    }

    @SuppressWarnings("serial")
    @Test
    public void testQueryWithSomeAspects_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("A", "B", data1);
        Utils.putValue("Cow", "Milk", data1);
        Utils.putValue("Dog", "Food", data1);
        EDBObject v1 = new EDBObject("/test/query1", data1);
        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(v1);
        long time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("Cow", "Milk", data2);
        Utils.putValue("House", "Garden", data2);
        v1 = new EDBObject("/test/query2", data2);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(v1);
        long time2 = db.commit(ci);

        List<EDBObject> list1 = db.queryByKeyValue("A", "B");
        List<EDBObject> list2 = db.queryByMap(new HashMap<String, Object>() {
            {
                put("A", "B");
                put("Dog", "Food");
            }
        });

        List<EDBObject> list3 = db.queryByMap(new HashMap<String, Object>() {
            {
                put("Cow", "Milk");
            }
        });

        List<EDBObject> list4 = db.queryByMap(new HashMap<String, Object>() {
            {
                put("A", "B");
                put("Cow", "Milk");
                put("House", "Garden");
            }
        });

        assertThat(list1.size(), is(1));
        assertThat(list2.size(), is(1));
        assertThat(list3.size(), is(2));
        assertThat(list4.size(), is(0));

        checkTimeStamps(Arrays.asList(time1, time2));

        // removed because of the by jpa not supported regex command
        // list = db.query(new HashMap<String, Object>() {
        // {
        // put("Cat", "Dog");
        // put("Lock", Pattern.compile("Smith|Key"));
        // }
        // });
        // assertTrue("There's one object in HEAD with Cat:Dog,Lock:/Smith|Key/", list.size() == 1);
    }

    @Test
    public void testDiff_shouldWork() throws Exception {
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 1", data1);
        Utils.putValue("KeyB", "Value B 1", data1);
        Utils.putValue("KeyC", "Value C 1", data1);
        EDBObject v1 = new EDBObject("/diff/object", data1);
        JPACommit ci = db.createCommit("Blub", "Testing");
        ci.insert(v1);
        time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = Maps.newHashMap(data1);

        Utils.putValue("KeyD", "Value D 1", data2);
        Utils.putValue("KeyA", "Value A 2", data2);
        EDBObject v2 = new EDBObject("/diff/object", data2);
        ci = db.createCommit("Blub", "Testing");
        ci.update(v2);
        time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data3 = Maps.newHashMap(data2);

        data3.remove("KeyB");
        Utils.putValue("KeyC", "Value C 3", data3);
        EDBObject v3 = new EDBObject("/diff/object", data3);
        ci = db.createCommit("Blub", "Testing");
        ci.update(v3);
        time3 = db.commit(ci);

        checkTimeStamps(Arrays.asList(time1, time2, time3));

        Diff diffAb = db.getDiff(time1, time2);
        Diff diffBc = db.getDiff(time2, time3);
        Diff diffAc = db.getDiff(time1, time3);

        assertThat(diffAb.getDifferenceCount(), is(1));
        assertThat(diffBc.getDifferenceCount(), is(1));
        assertThat(diffAc.getDifferenceCount(), is(1));
    }

    @Test
    public void testGetResurrectedOIDs_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 1", data1);
        EDBObject v1 = new EDBObject("/ress/object", data1);
        JPACommit ci = db.createCommit("Blub", "Testing");
        ci.insert(v1);
        long time1 = db.commit(ci);

        v1 = new EDBObject("/ress/object2", data1);
        ci = db.createCommit("Blub", "Testing");
        ci.insert(v1);
        ci.delete("/ress/object");
        long time2 = db.commit(ci);

        v1 = new EDBObject("/ress/object", data1);
        ci = db.createCommit("Blub", "Testing");
        ci.delete("/ress/object2");
        ci.update(v1);
        long time3 = db.commit(ci);

        List<String> oids = db.getResurrectedOIDs();

        assertThat(oids.contains("/ress/object"), is(true));
        assertThat(oids.contains("/ress/object2"), is(false));

        checkTimeStamps(Arrays.asList(time1, time2, time3));
    }

    @Test(expected = EDBException.class)
    public void testCommitTwiceSameCommit_shouldThrowError() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 1", data1);
        EDBObject v1 = new EDBObject("/fail/object", data1);
        JPACommit ci = db.createCommit("Blub", "Testing");
        ci.insert(v1);
        db.commit(ci);
        db.commit(ci);
    }

    @Test
    public void testQueryOfOldVersion_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 1", data1v1);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data1v1);
        EDBObject v11 = new EDBObject("pre:/test/object1", data1v1);
        JPACommit ci = db.createCommit("Blub", "Testing");
        ci.insert(v11);
        Map<String, EDBObjectEntry> data2v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 2", data2v1);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data2v1);
        EDBObject v12 = new EDBObject("pre:/test/object2", data2v1);
        ci.insert(v12);
        Map<String, EDBObjectEntry> data3v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 3", data3v1);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data3v1);
        EDBObject v13 = new EDBObject("pre:/test/object3", data3v1);
        ci.insert(v13);

        long time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data1v2 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 1", data1v2);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data1v2);
        EDBObject v21 = new EDBObject("pre:/test/object1", data1v2);
        ci = db.createCommit("Blub", "Testing");
        ci.update(v21);
        Map<String, EDBObjectEntry> data2v2 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 2", data2v2);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data2v2);
        EDBObject v22 = new EDBObject("pre:/test/object2", data2v2);
        ci.update(v22);
        Map<String, EDBObjectEntry> data4v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 4", data4v1);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data4v1);
        EDBObject v23 = new EDBObject("pre:/test/object4", data4v1);
        ci.update(v23);

        long time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data1v3 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 1", data1v3);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data1v3);
        EDBObject v31 = new EDBObject("pre:/test/object1", data1v3);
        ci = db.createCommit("Blub", "Testing");
        ci.update(v31);
        Map<String, EDBObjectEntry> data2v3 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 2a", data2v3);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data2v3);
        EDBObject v32 = new EDBObject("pre:/test/object2", data2v3);
        ci.update(v32);
        Map<String, EDBObjectEntry> data4v2 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("pre:KeyA", "pre:Value A 4", data4v2);
        Utils.putValue("pre:KeyB", "pre:Value A 1", data4v2);
        EDBObject v33 = new EDBObject("pre:/test/object4", data4v2);
        ci.update(v33);

        long time3 = db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pre:KeyB", "pre:Value A 1");
        List<EDBObject> result = db.query(map, time2);

        boolean b1 = false;
        boolean b2 = false;
        boolean b3 = false;

        for (EDBObject e : result) {
            if (e.getString("pre:KeyA").equals("pre:Value A 1")) {
                b1 = true;
            }
            if (e.getString("pre:KeyA").equals("pre:Value A 2")) {
                b2 = true;
            }
            if (e.getString("pre:KeyA").equals("pre:Value A 3")) {
                b3 = true;
            }
        }

        assertThat(b1, is(true));
        assertThat(b2, is(true));
        assertThat(b3, is(true));
        assertThat(time1 > 0, is(true));
        assertThat(time2 > 0, is(true));
        assertThat(time3 > 0, is(true));
    }

    @Test
    public void testQueryWithTimestamp_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("K", "B", data1);
        Utils.putValue("Cow", "Milk", data1);
        Utils.putValue("Dog", "Food", data1);
        EDBObject v1 = new EDBObject("/test/querynew1", data1);
        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew1", data1);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.update(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("K", "B", data1);
        Utils.putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew2", data1);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(v1);
        db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("K", "B");
        List<EDBObject> result = db.query(map, System.currentTimeMillis());
        assertThat(result.size(), is(1));
    }

    @Test
    public void testQueryWithTimestampAndEmptyMap_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("K", "B", data1);
        Utils.putValue("Cow", "Milk", data1);
        Utils.putValue("Dog", "Food", data1);
        EDBObject v1 = new EDBObject("/test/querynew3", data1);
        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew3", data1);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.update(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("K", "B", data1);
        Utils.putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew4", data1);
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(v1);
        db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        List<EDBObject> result = db.query(map, System.currentTimeMillis());
        EDBObject result1 = getEDBObjectOutOfList(result, "/test/querynew3");
        EDBObject result2 = getEDBObjectOutOfList(result, "/test/querynew4");
        assertThat(result.size(), is(2));
        assertThat(result1.containsKey("K"), is(false));
        assertThat(result2.containsKey("Dog"), is(true));
    }

    private EDBObject getEDBObjectOutOfList(List<EDBObject> objects, String oid) {
        for (EDBObject o : objects) {
            if (o.getOID().equals(oid)) {
                return o;
            }
        }
        return null;
    }

    @Test
    public void testQueryOfLastKnownVersion_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 1", data1v1);
        Utils.putValue("KeyB", "Value A 1", data1v1);
        EDBObject v11 = new EDBObject("/test/object1", data1v1);
        JPACommit ci = db.createCommit("Blub", "Testing");
        ci.insert(v11);
        Map<String, EDBObjectEntry> data2v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 2", data2v1);
        Utils.putValue("KeyB", "Value A 1", data2v1);
        EDBObject v12 = new EDBObject("/test/object2", data2v1);
        ci.insert(v12);
        Map<String, EDBObjectEntry> data3v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 3", data3v1);
        Utils.putValue("KeyB", "Value A 1", data3v1);
        EDBObject v13 = new EDBObject("/test/object3", data3v1);
        ci.insert(v13);

        long time1 = db.commit(ci);

        ci = db.createCommit("Blub", "Testing");
        Map<String, EDBObjectEntry> data1v2 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 1", data1v2);
        Utils.putValue("KeyB", "Value A 1", data1v2);
        EDBObject v21 = new EDBObject("/test/object1", data1v2);
        ci.update(v21);
        Map<String, EDBObjectEntry> data2v2 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 2", data2v2);
        Utils.putValue("KeyB", "Value A 1", data2v2);
        EDBObject v22 = new EDBObject("/test/object2", data2v2);
        ci.update(v22);

        long time2 = db.commit(ci);

        ci = db.createCommit("Blub", "Testing");
        Map<String, EDBObjectEntry> data2v3 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 2a", data2v3);
        Utils.putValue("KeyB", "Value A 1", data2v3);
        EDBObject v32 = new EDBObject("/test/object2", data2v3);
        ci.update(v32);
        Map<String, EDBObjectEntry> data4v1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("KeyA", "Value A 4", data4v1);
        Utils.putValue("KeyB", "Value A 1", data4v1);
        EDBObject v33 = new EDBObject("/test/object4", data4v1);
        ci.insert(v33);

        long time3 = db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("KeyB", "Value A 1");
        List<EDBObject> result = db.query(map, time3);

        boolean b1 = false;
        boolean b2 = false;
        boolean b3 = false;
        boolean b4 = false;

        for (EDBObject e : result) {
            if (e.getString("KeyA").equals("Value A 1")) {
                b1 = true;
            }
            if (e.getString("KeyA").equals("Value A 2a")) {
                b2 = true;
            }
            if (e.getString("KeyA").equals("Value A 3")) {
                b3 = true;
            }

            if (e.getString("KeyA").equals("Value A 4")) {
                b4 = true;
            }
        }

        assertThat(b1, is(true));
        assertThat(b2, is(true));
        assertThat(b3, is(true));
        assertThat(b4, is(true));
        assertThat(time1 > 0, is(true));
        assertThat(time2 > 0, is(true));
        assertThat(time3 > 0, is(true));
    }

    /**
     * iterates through the list of timestamps and checks if every timestamp is bigger than 0
     */
    private void checkTimeStamps(List<Long> timestamps) {
        for (Long timestamp : timestamps) {
            assertThat(timestamp, greaterThan((long) 0));
        }
    }

    @Test
    public void testCommitEDBObjectsInsert_shouldWork() throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/1");
        object.putEDBObjectEntry("bla", "blub");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);

        db.commitEDBObjects(inserts, null, null);

        object = db.getObject("/commit/test/insert/1");
        assertThat(object.getString("bla"), is("blub"));
        assertThat(object.getObject(EDBConstants.MODEL_VERSION, Integer.class), is(1));
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsInsertDouble_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/2");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);

        db.commitEDBObjects(inserts, null, null);
        db.commitEDBObjects(inserts, null, null);
    }

    @Test(expected = EDBException.class)
    public void testIfConflictDetectionIsWorking_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/3");
        object.putEDBObjectEntry("bla", "blub");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);

        db.commitEDBObjects(inserts, null, null);

        object = db.getObject("/commit/test/insert/3");
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(0));
        object.putEDBObjectEntry("test", "test");

        db.commitEDBObjects(null, Arrays.asList(object), null);
    }

    @Test
    public void testCommitEDBObjectsUpdate_shouldWork() throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/1");
        object.putEDBObjectEntry("testkey", "testvalue");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);

        db.commitEDBObjects(objects, null, null);

        EDBObject first = db.getObject("/commit/test/update/1");

        objects.clear();
        object.putEDBObjectEntry("testkey", "testvalue1");
        objects.add(object);

        db.commitEDBObjects(null, objects, null);

        EDBObject second = db.getObject("/commit/test/update/1");

        assertThat(first.getObject(EDBConstants.MODEL_VERSION, Integer.class), is(1));
        assertThat(first.getString("testkey"), is("testvalue"));
        assertThat(second.getObject(EDBConstants.MODEL_VERSION, Integer.class), is(2));
        assertThat(second.getString("testkey"), is("testvalue1"));
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsUpdateVerstionConflict_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/2");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        db.commitEDBObjects(objects, null, null);
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(0));
        object.putEDBObjectEntry("test", "test");
        db.commitEDBObjects(null, objects, null);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsUpdateVerstionConflict2_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/3");
        db.commitEDBObjects(Arrays.asList(object), null, null);
        EDBObject receive1 = db.getObject("/commit/test/update/3");
        EDBObject receive2 = db.getObject("/commit/test/update/3");
        receive1.putEDBObjectEntry("test1", "test1");
        receive2.putEDBObjectEntry("test1", "test2");
        db.commitEDBObjects(null, Arrays.asList(receive1), null);
        db.commitEDBObjects(null, Arrays.asList(receive2), null);
    }

    @Test
    public void testCommitEDBObjectsDelete_shouldWork() throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/1");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        db.commitEDBObjects(objects, null, null);
        db.commitEDBObjects(null, null, objects);

        EDBObject entry = db.getObject("/commit/test/delete/1");
        assertThat(entry.isDeleted(), is(true));
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteNonExisting_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/2");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        db.commitEDBObjects(null, null, objects);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteAlreadyDeleted_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/3");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        db.commitEDBObjects(objects, null, null);
        db.commitEDBObjects(null, null, objects);
        db.commitEDBObjects(null, null, objects);
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessInteger() throws Exception {
        EDBObject object = new EDBObject("test/type/integer");
        object.putEDBObjectEntry("value", Integer.valueOf(42));
        db.commitEDBObjects(Arrays.asList(object), null, null);
        object = db.getObject("test/type/integer");
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Integer.class.getName()));
        assertThat((Integer) value, is(42));
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessBoolean() throws Exception {
        EDBObject object = new EDBObject("test/type/boolean");
        object.putEDBObjectEntry("value", Boolean.TRUE);
        db.commitEDBObjects(Arrays.asList(object), null, null);
        object = db.getObject("test/type/boolean");
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Boolean.class.getName()));
        assertThat((Boolean) value, is(Boolean.TRUE));
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessDate() throws Exception {
        EDBObject object = new EDBObject("test/type/date");
        Date date = new Date();
        object.putEDBObjectEntry("value", date);
        db.commitEDBObjects(Arrays.asList(object), null, null);
        object = db.getObject("test/type/date");
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Date.class.getName()));
        assertThat((Date) value, is(date));
    }
    
    @Test
    public void testIfQueryingWithLikeWorks_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        Utils.putValue("bla_kdjer", "test", data1);
        EDBObject v1 = new EDBObject("/test/query/8", data1);
        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole());
        ci.insert(v1);
        db.commit(ci);
        List<EDBObject> result = db.queryByKeyValue("bla%", "test");
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/8"));
    }
}
