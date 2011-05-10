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

package org.openengsb.core.edb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBDatabaseType;
import org.openengsb.core.api.edb.EDBEntry;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBLogEntry;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EDBObjectDiff;
import org.openengsb.core.edb.internal.Diff;
import org.openengsb.core.edb.internal.JPACommit;
import org.openengsb.core.edb.internal.JPADatabase;

public class JPATestUT {
    // is currently overwritten by a fixed number in initDB
    private static long randSeed = System.currentTimeMillis();
    private static Random rand;
    private Long runTime;
    private static JPADatabase db;

    private static final String[] RANDOMKEYS = new String[]{
        "Product", "Handler", "RandomKey", "UserID", "Code", "Auto"
    };

    private static final String[] RANDOMCOMMITTERS = new String[]{
        "Bernard", "Johnny", "Jack", "Christian", "Latehost", "Panda"
    };

    private static final String[] RANDOMROLES = new String[]{
        "Modeller", "Designer", "Programmer", "Annoying Person", "Bossy Bastard"
    };

    private JPADatabase openDatabase() {
        if (db == null) {
            db = new JPADatabase();
            db.setDatabase("JPATest", EDBDatabaseType.H2);
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
        rand = new Random(randSeed); // 1938491837);
        File f = new File("JPATest.h2.db");
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
        File f = new File("JPATest.h2.db");
        f.delete();
    }

    @Before
    public void setup() {
        // setup stuff
        runTimeStep();
    }

    private EDBObject randomTestObject(String uuid, long runTime) {
        // If you use this the object must not in any way relate to the feature you are testing...
        // Using random strings seems to have an effect on the OS' RW-cache... ?
        // Well... if its cache says the HD has the very same text in the block that's supposed to be written
        // it COULD skip it - though it's unlikely/unsafe... yet... randomizing the unused content could result
        // in "more realistic" HD-access-times
        Map<String, Object> testData = new HashMap<String, Object>();

        // int max = 2 + rand.nextInt( 3);
        int max = 20;

        for (int i = 0; i < max; ++i) {
            String key = RANDOMKEYS[rand.nextInt(RANDOMKEYS.length)] + Integer.toString(i);
            String value = "key value " + Integer.toString(rand.nextInt(100));
            testData.put(key, value);
        }
        return new EDBObject(uuid, runTime, testData);
    }

    private String randomCommitter() {
        return RANDOMCOMMITTERS[rand.nextInt(RANDOMCOMMITTERS.length)];
    }

    private String randomRole() {
        return RANDOMROLES[rand.nextInt(RANDOMROLES.length)];
    }

    @Test
    public void testOpenJPADatabase() {
        JPADatabase db = openDatabase();
        assertTrue(db != null);
    }

    @Test
    public void testCommit() {
        JPADatabase db = openDatabase();
        try {
            JPACommit ci = db.createCommit("TestCommit", "Role", runTime);
            EDBObject obj = new EDBObject("Tester", runTime);
            obj.put("Test", "Hooray");
            ci.add(obj);
            ci.commit();

            obj = null;

            obj = db.getObject("Tester");
            assertTrue("Test object must exist!", obj != null);
            String hooray = (String) obj.get("Test");
            assertTrue("Test-string must exist in the queried object", hooray != null);
            System.out.println("Test should be Hooray: Hooray == " + hooray);
        } catch (EDBException ex) {
            ex.printStackTrace();
            Throwable e = ex;
            while (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
                e = e.getCause();
            }
            fail("Error: " + ex.toString());
        }
    }

    /*
     * @Test public void testManual() { Database db = openDatabase(); try { JPA2Database jdb = (JPA2Database)db;
     * System.out.println("***********************************************************************"); //EDBObject o =
     * jdb.queryTest(
     * "select o from JPAObject o where exists (select v from o.values v where v.key, v.value = 'Test', 'Hooray')");
     * EDBObject o = jdb.queryTest(); //"select o from JPAObject o where 'Test', 'Hooray' member of o.values");
     * System.out.println("***********************************************************************"); } catch
     * (EDBException ex) { fail("Error: " + ex.toString()); } db.close(); }
     */

    @Test
    public void testGetCommits() {
        JPADatabase db = openDatabase();
        try {
            JPACommit ci = db.createCommit("TestCommit2", "Testrole", runTime);
            EDBObject obj = new EDBObject("TestObject", runTime);
            obj.put("Bla", "Blabla");
            ci.add(obj);
            ci.commit();

            List<EDBCommit> commits = db.getCommits("role", "Testrole");
            System.out.println("Found " + Integer.toString(commits.size()) + " commits of role Role");
            assertTrue("1 commit", commits.size() == 1);
            commits = db.getCommits("role", "DoesNotExist");
            System.out.println("Found " + Integer.toString(commits.size()) + " commits of role DoesNotExist");
            assertTrue("1 commit", commits.size() == 0);
            ci = db.getLastCommit("role", "Testrole");
            assertTrue("One commit must exist", ci != null);
            System.out.println("*** Listing that one commit...");
            for (String s : ci.getUIDs()) {
                System.out.println("Object: " + s);
            }
        } catch (EDBException ex) {
            fail("Faild to fetch commit list...");
        }
    }

    @Test
    public void testGetInexistantObjectByUID() {
        JPADatabase db = openDatabase();
        EDBObject obj;
        try {
            obj = db.getObject("/this/object/does/not/exist");
            assertEquals(obj, null);
        } catch (Exception ex) {
            fail("Unexpected error while fetching object: " + ex.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetHistory() {
        JPADatabase db = openDatabase();
        List<EDBObject> history;
        long delCreated = 0;
        long delDeleted = 0;
        // First we create 4 commits, 3 of which will contain the test-object
        // every commit will get a useless additional object with it...
        try {
            HashMap<String, Object> data1 = new HashMap<String, Object>();
            data1.put("Lock", "Key");
            data1.put("Door", "Bell");
            data1.put("Cat", "Spongebob");
            EDBObject v1 = new EDBObject("/history/object", runTime, data1);
            JPACommit ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(randomTestObject("/useless/1", runTime));
            ci.add(randomTestObject("/deletion/1", runTime));
            delCreated = runTime;
            ci.add(v1);
            ci.commit();
            runTimeStep();

            // Now we change stuff:
            HashMap<String, Object> data2 = (HashMap<String, Object>) data1.clone();
            data2.put("Lock", "Smith");
            EDBObject v2 = new EDBObject("/history/object", runTime, data2);
            ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(randomTestObject("/useless/2", runTime));
            ci.delete("/deletion/1");
            delDeleted = runTime;
            ci.add(v2);
            ci.commit();

            // Now the intermediate commit:
            HashMap<String, Object> data3 = (HashMap<String, Object>) data2.clone(); // do this here to waste some
                                                                                     // milliseconds
            runTimeStep();
            ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(randomTestObject("/useless/3", runTime));
            ci.add(randomTestObject("/useless/4", runTime));
            ci.commit();

            // Now we change something else:
            data3.put("Cat", "Dog");
            runTimeStep();
            EDBObject v3 = new EDBObject("/history/object", runTime, data3);
            ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(v3);
            ci.add(randomTestObject("/useless/5", runTime));
            ci.commit();
        } catch (EDBException ex) {
            fail("Error: " + ex.toString());
        }

        try {
            history = db.getHistory("/history/object");
            EDBObject[] obj = new EDBObject[3];
            obj[0] = history.get(0);
            for (int i = 1; i < 3; ++i) {
                obj[i] = history.get(i);
                System.out.println("History List: " + Long.toString(obj[i].getTimestamp())
                        + " > " + Long.toString(obj[i - 1].getTimestamp()) + "?");
                assertTrue("History list must be ordered", obj[i].getTimestamp() > obj[i - 1].getTimestamp());
            }
            for (EDBObject o : history) {
                System.out.println(o.toString());
            }
            assertTrue(obj[0].getString("Lock").equals("Key"));
            assertTrue(obj[1].getString("Lock").equals("Smith"));
            assertTrue(obj[2].getString("Lock").equals("Smith"));
            assertTrue(obj[0].getString("Cat").equals("Spongebob"));
            assertTrue(obj[1].getString("Cat").equals("Spongebob"));
            assertTrue(obj[2].getString("Cat").equals("Dog"));

            history = db.getHistory("/deletion/1");
            System.out.println("Found " + Integer.toString(history.size()) + " entries for /deletion/1");
            System.out.println("Should be 2 with timestamps: " + Long.toString(delCreated) + " and "
                    + Long.toString(delDeleted));
            for (EDBObject o : history) {
                System.out.println("Timestamp: " + Long.toString(o.getTimestamp()));
            }
            assertEquals("2 entries for the delted object: creation and deletion", history.size(), 2);
            assertFalse("First object is not a deletion!", history.get(0).isDeleted());
            assertTrue("Second entry is a deletion!", history.get(1).isDeleted());
        } catch (EDBException ex) {
            fail("History test failed: " + ex.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetLog() {
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
            JPACommit ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(randomTestObject("/useless/test/1", runTime));
            ci.add(randomTestObject("/deletion/test/1", runTime));
            ci.add(v1);
            ci.commit();
            runTimeStep();

            // Now we change stuff:
            HashMap<String, Object> data2 = (HashMap<String, Object>) data1.clone();
            data2.put("Burger", "Meat");
            EDBObject v2 = new EDBObject("/history/test/object", runTime, data2);
            ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(randomTestObject("/useless/test/2", runTime));
            ci.delete("/deletion/test/1");
            ci.add(v2);
            ci.commit();

            // Now the intermediate commit:
            HashMap<String, Object> data3 = (HashMap<String, Object>) data2.clone(); // do this here to waste some
                                                                                     // milliseconds
            runTimeStep();
            ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(randomTestObject("/useless/test/3", runTime));
            ci.add(randomTestObject("/useless/test/4", runTime));
            ci.commit();

            // Now we change something else:
            data3.put("Cheese", "Milk");
            runTimeStep();
            EDBObject v3 = new EDBObject("/history/test/object", runTime, data3);
            ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(v3);
            ci.add(randomTestObject("/useless/test/5", runTime));
            ci.commit();

            List<EDBObject> history = db.getHistory("/history/test/object");
            System.out.println("Objects for /history/test/object: " + Integer.toString(history.size()));
            assertTrue("History needs to be available!", history != null);
            assertTrue("History needs to be non-empty!", history.size() > 1);
            assertTrue("History must contain valid objects", history.get(0) != null);
            System.out.println("Checkpoint");
            System.out.println("Timestamp: " + history.get(0).getTimestamp());
            from = history.get(0).getLong("@timestamp");
            System.out.println("Checkpoint");
            to = history.get(history.size() - 1).getLong("@timestamp");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("getHistory failed, didn't even get to try getLog: " + ex.toString());
        }
        System.out.println("Trying to fetch Log from " + Long.toString(from) + " to " + Long.toString(to));
        assertTrue("'from' timestamp makes sense", from != -1);
        assertTrue("'to' timestamp makes sense", to != -1);

        try {
            log = db.getLog("/history/test/object", from, to);
            System.out.println("Found " + Integer.toString(log.size()) + " log entries...");
            for (EDBLogEntry l : log) {
                System.out.println("Commit " + l.getCommit().getTimestamp());
            }
        } catch (Exception ex) {
            fail("Failed to get log entries " + ex.toString());
        }
    }

    @SuppressWarnings("serial")
    @Test
    public void testQueries() {
        JPADatabase db = openDatabase();
        try {
            HashMap<String, Object> data1 = new HashMap<String, Object>();
            data1.put("A", "B");
            data1.put("Cow", "Milk");
            data1.put("Dog", "Food");
            EDBObject v1 = new EDBObject("/test/query1", runTime, data1);
            JPACommit ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(v1);
            ci.commit();
            runTimeStep();

            HashMap<String, Object> data2 = new HashMap<String, Object>();
            data2.put("Cow", "Milk");
            data2.put("House", "Garden");
            v1 = new EDBObject("/test/query2", runTime, data2);
            ci = db.createCommit(randomCommitter(), randomRole(), runTime);
            ci.add(v1);
            ci.commit();
            runTimeStep();

            List<EDBObject> list = db.query("A", "B");
            assertTrue("There's one object with A:B", list.size() == 1);
            list = db.query(new HashMap<String, Object>() {
                {
                    put("A", "B");
                    put("Dog", "Food");
                }
            });
            assertTrue("There's one object with A:B,Dog:Food", list.size() == 1);
            list = db.query(new HashMap<String, Object>() {
                {
                    put("Cow", "Milk");
                }
            });
            assertTrue("There must be two objects with Cow:Milk", list.size() == 2);
            list = db.query(new HashMap<String, Object>() {
                {
                    put("A", "B");
                    put("Cow", "Milk");
                    put("House", "Garden");
                }
            });
            assertTrue("There's must not be an object in HEAD with A:B,Cow:Milk", list.size() == 0);

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
    public void testDiff() {
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
            ci.commit();
            timeA = runTime;

            HashMap<String, Object> data2 = (HashMap<String, Object>) data1.clone(); // do this here to waste some
                                                                                     // milliseconds
            runTimeStep();

            data2.put("KeyD", "Value D 1");
            data2.put("KeyA", "Value A 2");
            EDBObject v2 = new EDBObject("/diff/object", runTime, data2);
            ci = db.createCommit("Blub", "Testing", runTime);
            ci.add(v2);
            ci.commit();
            timeB = runTime;

            runTimeStep();
            HashMap<String, Object> data3 = (HashMap<String, Object>) data2.clone(); // do this here to waste some
                                                                                     // milliseconds
            runTimeStep();

            data3.remove("KeyB");
            data3.put("KeyC", "Value C 3");
            EDBObject v3 = new EDBObject("/diff/object", runTime, data3);
            ci = db.createCommit("Blub", "Testing", runTime);
            ci.add(v3);
            ci.commit();
            timeC = runTime;
        } catch (EDBException ex) {
            fail("Failed to prepare commits for comparison!");
        }

        runTimeStep();
        try {
            System.out.println("Diff Test: Times: "
                    + Long.toString(timeA) + ", "
                    + Long.toString(timeB) + ", "
                    + Long.toString(timeC));

            Diff diffAb = db.getDiff(timeA, timeB);
            Diff diffBc = db.getDiff(timeB, timeC);
            Diff diffAc = db.getDiff(timeA, timeC);

            System.out.println("Difference count A..B: " + Integer.toString(diffAb.getDifferenceCount()));
            System.out.println("Difference count B..C: " + Integer.toString(diffBc.getDifferenceCount()));
            System.out.println("Difference count A..C: " + Integer.toString(diffAc.getDifferenceCount()));

            System.out.println("  A..B:");
            printDiffObject(diffAb);
            System.out.println("  B..C:");
            printDiffObject(diffBc);
            System.out.println("  A..C:");
            printDiffObject(diffAc);

            assertTrue("One change from A to B", diffAb.getDifferenceCount() == 1);
            assertTrue("One change from B to C", diffBc.getDifferenceCount() == 1);
            assertTrue("One change from A to C", diffAc.getDifferenceCount() == 1);
        } catch (EDBException ex) {
            fail("Failed to fetch the diff between the newly created objects!");
        }
    }

    private void printDiffObject(Diff difference) {
        Map<String, EDBObjectDiff> diff = difference.getObjectDiffs();
        for (Map.Entry<String, EDBObjectDiff> e : diff.entrySet()) {
            String uid = e.getKey();
            System.out.println("    Found a difference for object: " + uid);

            EDBObjectDiff odiff = e.getValue();
            Map<String, EDBEntry> diffMap = odiff.getDiffMap();
            for (Map.Entry<String, EDBEntry> de : diffMap.entrySet()) {
                String key = de.getKey();
                EDBEntry entry = de.getValue();
                System.out.println("      Entry: '" + key + "' from: '" + entry.getBefore() + "' to: '" 
                    + entry.getAfter() + "'");
            }
        }
    }

    @Test
    public void testGetResurrectedUIDs() throws Exception {
        JPADatabase db = openDatabase();

        HashMap<String, Object> data1 = new HashMap<String, Object>();
        data1.put("KeyA", "Value A 1");
        EDBObject v1 = new EDBObject("/ress/object", runTime, data1);
        JPACommit ci = db.createCommit("Blub", "Testing", runTime);
        ci.add(v1);
        ci.commit();

        runTimeStep();

        v1 = new EDBObject("/ress/object2", runTime, data1);
        ci = db.createCommit("Blub", "Testing", runTime);
        ci.add(v1);
        ci.delete("/ress/object");
        ci.commit();

        runTimeStep();

        v1 = new EDBObject("/ress/object", runTime, data1);
        ci = db.createCommit("Blub", "Testing", runTime);
        ci.delete("/ress/object2");
        ci.add(v1);
        ci.commit();

        List<String> uids = db.getResurrectedUIDs();
        assertTrue(uids.contains("/ress/object"));
        assertFalse(uids.contains("/ress/object2"));
    }
}
