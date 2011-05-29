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

package org.openengsb.core.edb.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBLogEntry;
import org.openengsb.core.api.edb.EDBObject;

public class JPATestIT {
    // is currently overwritten by a fixed number in initDB
    private static long randSeed = System.currentTimeMillis();
    private Long runTime;
    private static JPADatabase db;
    private static Utils utils;

    private JPADatabase openDatabase() {
        if (db == null) {
            db = new JPADatabase();
            try {
                db.open();
            } catch (Exception ex) {
                db = null;
                fail("Cannot open database: " + ex.toString());
            }
        }
        return db;
    }

    private void runTimeStep() {
        Long old = runTime;
        while (old == runTime) {
            runTime = System.currentTimeMillis();
        }
    }

    @BeforeClass
    public static void initDB() {
        randSeed = 1295369697576L;
        utils = new Utils(randSeed);
        File f = new File("TEST.h2.db");
        if (!f.exists()) {
            return;
        }
        if (!f.canWrite() || !f.delete()) {
            fail("Cannot remove previous test database!");
        }
    }

    @AfterClass
    public static void closeDB() {
        db.close();
        File f = new File("TEST.h2.db");
        f.delete();
    }

    @Before
    public void setup() {
        // setup stuff
        runTimeStep();
    }    

    @Test
    public void testOpenDatabase_shouldWork() {
        JPADatabase db = openDatabase();

        assertThat(db, notNullValue());
    }

    @Test
    public void testCommit_shouldWork() {
        JPADatabase db = openDatabase();
        try {
            JPACommit ci = db.createCommit("TestCommit", "Role", runTime);
            EDBObject obj = new EDBObject("Tester", runTime);
            obj.put("Test", "Hooray");
            ci.add(obj);

            db.commit(ci);

            obj = null;
            obj = db.getObject("Tester");
            String hooray = (String) obj.get("Test");

            assertThat(obj, notNullValue());
            assertThat(hooray, notNullValue());
        } catch (EDBException ex) {
            fail("Error: " + ex.toString());
        }
    }

    @Test
    public void testGetCommits_shouldWork() {
        JPADatabase db = openDatabase();
        try {
            JPACommit ci = db.createCommit("TestCommit2", "Testrole", runTime);
            EDBObject obj = new EDBObject("TestObject", runTime);
            obj.put("Bla", "Blabla");
            ci.add(obj);

            db.commit(ci);
            List<EDBCommit> commits1 = db.getCommits("role", "Testrole");
            List<EDBCommit> commits2 = db.getCommits("role", "DoesNotExist");

            assertThat(commits1.size(), is(1));
            assertThat(commits2.size(), is(0));
        } catch (EDBException ex) {
            fail("Faild to fetch commit list..." + ex.getLocalizedMessage());
        }
    }

    @Test(expected = EDBException.class)
    public void testGetInexistantObject_shouldThrowException() throws Exception {
        JPADatabase db = openDatabase();
        db.getObject("/this/object/does/not/exist");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetHistoryAndCheckForElements_shouldWork() throws Exception {
        JPADatabase db = openDatabase();
        try {
            HashMap<String, Object> data1 = new HashMap<String, Object>();
            data1.put("Lock", "Key");
            data1.put("Door", "Bell");
            data1.put("Cat", "Spongebob");
            EDBObject v1 = new EDBObject("/history/object", runTime, data1);
            JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(utils.createRandomTestObject("/useless/1", runTime));
            ci.add(v1);

            db.commit(ci);

            runTimeStep();

            // Now we change stuff:
            HashMap<String, Object> data2 = (HashMap<String, Object>) data1.clone();
            data2.put("Lock", "Smith");
            EDBObject v2 = new EDBObject("/history/object", runTime, data2);
            ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(utils.createRandomTestObject("/useless/2", runTime));
            ci.add(v2);
            db.commit(ci);

            HashMap<String, Object> data3 = (HashMap<String, Object>) data2.clone();

            runTimeStep();
            ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(utils.createRandomTestObject("/useless/3", runTime));
            ci.add(utils.createRandomTestObject("/useless/4", runTime));
            db.commit(ci);

            // Now we change something else:
            data3.put("Cat", "Dog");
            runTimeStep();
            EDBObject v3 = new EDBObject("/history/object", runTime, data3);
            ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(v3);
            ci.add(utils.createRandomTestObject("/useless/5", runTime));
            db.commit(ci);
        } catch (EDBException ex) {
            fail("Error: " + ex.toString());
        }

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
    }

    @Test
    public void testHistoryOfDeletion_shouldWork() throws Exception {
        JPADatabase db = openDatabase();

        JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
        ci.add(utils.createRandomTestObject("/deletion/1", runTime));
        db.commit(ci);

        runTimeStep();
        ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
        ci.delete("/deletion/1");
        db.commit(ci);

        List<EDBObject> history = db.getHistory("/deletion/1");

        assertThat(history.size(), is(2));
        assertThat(history.get(0).isDeleted(), is(false));
        assertThat(history.get(1).isDeleted(), is(true));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetLog_shouldWork() throws Exception {
        JPADatabase db = openDatabase();
        List<EDBLogEntry> log;
        long from = -1;
        long to = -1;
        try {
            HashMap<String, Object> data1 = new HashMap<String, Object>();
            data1.put("Burger", "Salad");
            data1.put("Bla", "Blub");
            data1.put("Cheese", "Butter");
            EDBObject v1 = new EDBObject("/history/test/object", runTime, data1);
            from = runTime;
            JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(utils.createRandomTestObject("/useless/test/1", runTime));
            ci.add(utils.createRandomTestObject("/deletion/test/1", runTime));
            ci.add(v1);
            db.commit(ci);
            runTimeStep();

            // Now we change stuff:
            HashMap<String, Object> data2 = (HashMap<String, Object>) data1.clone();
            data2.put("Burger", "Meat");
            EDBObject v2 = new EDBObject("/history/test/object", runTime, data2);
            ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(utils.createRandomTestObject("/useless/test/2", runTime));
            ci.delete("/deletion/test/1");
            ci.add(v2);
            db.commit(ci);

            HashMap<String, Object> data3 = (HashMap<String, Object>) data2.clone();
            runTimeStep();
            ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(utils.createRandomTestObject("/useless/test/3", runTime));
            ci.add(utils.createRandomTestObject("/useless/test/4", runTime));
            db.commit(ci);

            // Now we change something else:
            data3.put("Cheese", "Milk");
            runTimeStep();
            EDBObject v3 = new EDBObject("/history/test/object", runTime, data3);
            ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(v3);
            ci.add(utils.createRandomTestObject("/useless/test/5", runTime));
            to = runTime;
            db.commit(ci);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("getHistory failed, didn't even get to try getLog: " + ex.toString());
        }

        log = db.getLog("/history/test/object", from, to);
        assertThat(log.size(), is(3));
    }

    @SuppressWarnings("serial")
    @Test
    public void testQueryWithSomeAspects_shouldWork() {
        JPADatabase db = openDatabase();
        try {
            HashMap<String, Object> data1 = new HashMap<String, Object>();
            data1.put("A", "B");
            data1.put("Cow", "Milk");
            data1.put("Dog", "Food");
            EDBObject v1 = new EDBObject("/test/query1", runTime, data1);
            JPACommit ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(v1);
            db.commit(ci);
            runTimeStep();

            HashMap<String, Object> data2 = new HashMap<String, Object>();
            data2.put("Cow", "Milk");
            data2.put("House", "Garden");
            v1 = new EDBObject("/test/query2", runTime, data2);
            ci = db.createCommit(utils.getRandomCommitter(), utils.getRandomRole(), runTime);
            ci.add(v1);
            db.commit(ci);
            runTimeStep();

            List<EDBObject> list1 = db.query("A", "B");
            List<EDBObject> list2 = db.query(new HashMap<String, Object>() {
                {
                    put("A", "B");
                    put("Dog", "Food");
                }
            });

            List<EDBObject> list3 = db.query(new HashMap<String, Object>() {
                {
                    put("Cow", "Milk");
                }
            });

            List<EDBObject> list4 = db.query(new HashMap<String, Object>() {
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

            // removed because of the by jpa not supported regex command
            // list = db.query(new HashMap<String, Object>() {
            // {
            // put("Cat", "Dog");
            // put("Lock", Pattern.compile("Smith|Key"));
            // }
            // });
            // assertTrue("There's one object in HEAD with Cat:Dog,Lock:/Smith|Key/", list.size() == 1);
        } catch (EDBException ex) {
            fail("DB error: " + ex.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDiff_shouldWork() throws Exception {
        JPADatabase db = openDatabase();
        long timeA = 0;
        long timeB = 0;
        long timeC = 0;
        try {
            HashMap<String, Object> data1 = new HashMap<String, Object>();
            data1.put("KeyA", "Value A 1");
            data1.put("KeyB", "Value B 1");
            data1.put("KeyC", "Value C 1");
            EDBObject v1 = new EDBObject("/diff/object", runTime, data1);
            JPACommit ci = db.createCommit("Blub", "Testing", runTime);
            ci.add(v1);
            db.commit(ci);
            timeA = runTime;

            HashMap<String, Object> data2 = (HashMap<String, Object>) data1.clone();
            runTimeStep();

            data2.put("KeyD", "Value D 1");
            data2.put("KeyA", "Value A 2");
            EDBObject v2 = new EDBObject("/diff/object", runTime, data2);
            ci = db.createCommit("Blub", "Testing", runTime);
            ci.add(v2);
            db.commit(ci);
            timeB = runTime;

            runTimeStep();
            HashMap<String, Object> data3 = (HashMap<String, Object>) data2.clone();

            data3.remove("KeyB");
            data3.put("KeyC", "Value C 3");
            EDBObject v3 = new EDBObject("/diff/object", runTime, data3);
            ci = db.createCommit("Blub", "Testing", runTime);
            ci.add(v3);
            db.commit(ci);
            timeC = runTime;
        } catch (EDBException ex) {
            fail("Failed to prepare commits for comparison!" + ex.getLocalizedMessage());
        }

        runTimeStep();

        Diff diffAb = db.getDiff(timeA, timeB);
        Diff diffBc = db.getDiff(timeB, timeC);
        Diff diffAc = db.getDiff(timeA, timeC);

        assertThat(diffAb.getDifferenceCount(), is(1));
        assertThat(diffBc.getDifferenceCount(), is(1));
        assertThat(diffAc.getDifferenceCount(), is(1));
    }

    @Test
    public void testGetResurrectedOIDs_shouldWork() throws Exception {
        JPADatabase db = openDatabase();

        HashMap<String, Object> data1 = new HashMap<String, Object>();
        data1.put("KeyA", "Value A 1");
        EDBObject v1 = new EDBObject("/ress/object", runTime, data1);
        JPACommit ci = db.createCommit("Blub", "Testing", runTime);
        ci.add(v1);
        db.commit(ci);

        runTimeStep();

        v1 = new EDBObject("/ress/object2", runTime, data1);
        ci = db.createCommit("Blub", "Testing", runTime);
        ci.add(v1);
        ci.delete("/ress/object");
        db.commit(ci);

        runTimeStep();

        v1 = new EDBObject("/ress/object", runTime, data1);
        ci = db.createCommit("Blub", "Testing", runTime);
        ci.delete("/ress/object2");
        ci.add(v1);
        db.commit(ci);

        List<String> oids = db.getResurrectedOIDs();

        assertThat(oids.contains("/ress/object"), is(true));
        assertThat(oids.contains("/ress/object2"), is(false));
    }

    @Test(expected = EDBException.class)
    public void testCommitTwiceSameCommit_shouldThrowError() throws Exception {
        JPADatabase db = openDatabase();

        HashMap<String, Object> data1 = new HashMap<String, Object>();
        data1.put("KeyA", "Value A 1");
        EDBObject v1 = new EDBObject("/fail/object", runTime, data1);
        JPACommit ci = db.createCommit("Blub", "Testing", runTime);
        ci.add(v1);
        db.commit(ci);
        db.commit(ci);
    }
}
