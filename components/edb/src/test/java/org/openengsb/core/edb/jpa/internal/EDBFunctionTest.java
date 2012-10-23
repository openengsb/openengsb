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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;

import com.google.common.collect.Maps;

public class EDBFunctionTest extends AbstractEDBTest {    

    @Test
    public void testOpenDatabase_shouldWork() throws Exception {
        assertThat(db, notNullValue());
    }

    @Test
    public void testCommit_shouldWork() throws Exception {
        EDBObject obj = new EDBObject("Tester");
        obj.putEDBObjectEntry("Test", "Hooray");
        EDBCommit ci = db.createEDBCommit(Arrays.asList(obj), null, null);

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
        EDBObject obj = new EDBObject("TestObject");
        obj.putEDBObjectEntry("Bla", "Blabla");
        EDBCommit ci = db.createEDBCommit(Arrays.asList(obj), null, null);

        long time = db.commit(ci);

        List<EDBCommit> commits1 = db.getCommitsByKeyValue("context", "testcontext");
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
        putValue("Lock", "Key", data1);
        putValue("Door", "Bell", data1);
        putValue("Cat", "Spongebob", data1);
        EDBObject v1 = new EDBObject("/history/object", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(createRandomTestObject("/useless/1"));
        ci.insert(v1);

        time1 = db.commit(ci);

        HashMap<String, EDBObjectEntry> data2 = (HashMap<String, EDBObjectEntry>) data1.clone();
        putValue("Lock", "Smith", data2);
        EDBObject v2 = new EDBObject("/history/object", data2);
        ci = getEDBCommit();
        ci.insert(createRandomTestObject("/useless/2"));
        ci.update(v2);

        time2 = db.commit(ci);

        HashMap<String, EDBObjectEntry> data3 = (HashMap<String, EDBObjectEntry>) data2.clone();
        ci = getEDBCommit();
        ci.insert(createRandomTestObject("/useless/3"));
        ci.insert(createRandomTestObject("/useless/4"));
        time3 = db.commit(ci);

        putValue("Cat", "Dog", data3);
        EDBObject v3 = new EDBObject("/history/object", data3);
        ci = getEDBCommit();
        ci.update(v3);
        ci.insert(createRandomTestObject("/useless/5"));

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
        EDBCommit ci = getEDBCommit();
        ci.insert(createRandomTestObject("/deletion/1"));
        long time1 = db.commit(ci);

        ci = getEDBCommit();
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
        putValue("Burger", "Salad", data1);
        putValue("Bla", "Blub", data1);
        putValue("Cheese", "Butter", data1);
        EDBObject v1 = new EDBObject("/history/test/object", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(createRandomTestObject("/useless/test/1"));
        ci.insert(createRandomTestObject("/deletion/test/1"));
        ci.insert(v1);

        time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = Maps.newHashMap(data1);
        putValue("Burger", "Meat", data2);
        EDBObject v2 = new EDBObject("/history/test/object", data2);
        ci = getEDBCommit();
        ci.insert(createRandomTestObject("/useless/test/2"));
        ci.delete("/deletion/test/1");
        ci.update(v2);
        time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data3 = Maps.newHashMap(data2);
        ci = getEDBCommit();
        ci.insert(createRandomTestObject("/useless/test/3"));
        ci.insert(createRandomTestObject("/useless/test/4"));
        time3 = db.commit(ci);
        putValue("Cheese", "Milk", data3);

        EDBObject v3 = new EDBObject("/history/test/object", data3);
        ci = getEDBCommit();
        ci.update(v3);
        ci.insert(createRandomTestObject("/useless/test/5"));

        time4 = db.commit(ci);

        List<EDBLogEntry> log = db.getLog("/history/test/object", time1, time4);
        assertThat(log.size(), is(3));

        checkTimeStamps(Arrays.asList(time1, time2, time3, time4));
    }

    @Test
    public void testDiff_shouldWork() throws Exception {
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1);
        putValue("KeyB", "Value B 1", data1);
        putValue("KeyC", "Value C 1", data1);
        EDBObject v1 = new EDBObject("/diff/object", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = Maps.newHashMap(data1);

        putValue("KeyD", "Value D 1", data2);
        putValue("KeyA", "Value A 2", data2);
        EDBObject v2 = new EDBObject("/diff/object", data2);
        ci = getEDBCommit();
        ci.update(v2);
        time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data3 = Maps.newHashMap(data2);

        data3.remove("KeyB");
        putValue("KeyC", "Value C 3", data3);
        EDBObject v3 = new EDBObject("/diff/object", data3);
        ci = getEDBCommit();
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
        putValue("KeyA", "Value A 1", data1);
        EDBObject v1 = new EDBObject("/ress/object", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        long time1 = db.commit(ci);

        v1 = new EDBObject("/ress/object2", data1);
        ci = getEDBCommit();
        ci.insert(v1);
        ci.delete("/ress/object");
        long time2 = db.commit(ci);

        v1 = new EDBObject("/ress/object", data1);
        ci = getEDBCommit();
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
        putValue("KeyA", "Value A 1", data1);
        EDBObject v1 = new EDBObject("/fail/object", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);
        db.commit(ci);
    }

    @Test
    public void testCommitEDBObjectsInsert_shouldWork() throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/1");
        object.putEDBObjectEntry("bla", "blub");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);

        commitObjects(inserts, null, null);

        object = db.getObject("/commit/test/insert/1");
        assertThat(object.getString("bla"), is("blub"));
        assertThat(object.getObject(EDBConstants.MODEL_VERSION, Integer.class), is(1));
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsInsertDouble_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/2");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);

        commitObjects(inserts, null, null);
        commitObjects(inserts, null, null);
    }

    @Test(expected = EDBException.class)
    public void testIfConflictDetectionIsWorking_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/3");
        object.putEDBObjectEntry("bla", "blub");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);
        commitObjects(inserts, null, null);
        object = db.getObject("/commit/test/insert/3");
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(0));
        object.putEDBObjectEntry("test", "test");
        commitObjects(inserts, Arrays.asList(object), null);
    }

    @Test
    public void testCommitEDBObjectsUpdate_shouldWork() throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/1");
        object.putEDBObjectEntry("testkey", "testvalue");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);

        commitObjects(objects, null, null);

        EDBObject first = db.getObject("/commit/test/update/1");

        objects.clear();
        object.putEDBObjectEntry("testkey", "testvalue1");
        objects.add(object);

        commitObjects(null, objects, null);

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
        commitObjects(objects, null, null);
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(0));
        object.putEDBObjectEntry("test", "test");
        commitObjects(null, objects, null);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsUpdateVerstionConflict2_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/3");
        commitObjects(Arrays.asList(object), null, null);
        EDBObject receive1 = db.getObject("/commit/test/update/3");
        EDBObject receive2 = db.getObject("/commit/test/update/3");
        receive1.putEDBObjectEntry("test1", "test1");
        receive2.putEDBObjectEntry("test1", "test2");
        commitObjects(null, Arrays.asList(receive1), null);
        commitObjects(null, Arrays.asList(receive2), null);
    }

    @Test
    public void testCommitEDBObjectsDelete_shouldWork() throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/1");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        commitObjects(objects, null, null);
        commitObjects(null, null, objects);

        EDBObject entry = db.getObject("/commit/test/delete/1");
        assertThat(entry.isDeleted(), is(true));
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteNonExisting_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/2");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        commitObjects(null, null, objects);
    }

    @Test(expected = EDBException.class)
    public void testCommitEDBObjectsDeleteAlreadyDeleted_shouldThrowException() throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/3");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        commitObjects(objects, null, null);
        commitObjects(null, null, objects);
        commitObjects(null, null, objects);
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessInteger() throws Exception {
        EDBObject object = new EDBObject("test/type/integer");
        object.putEDBObjectEntry("value", Integer.valueOf(42));
        commitObjects(Arrays.asList(object), null, null);
        object = db.getObject("test/type/integer");
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Integer.class.getName()));
        assertThat((Integer) value, is(42));
    }

    @Test
    public void testIfOtherTypesThanStringWorks_shouldProcessBoolean() throws Exception {
        EDBObject object = new EDBObject("test/type/boolean");
        object.putEDBObjectEntry("value", Boolean.TRUE);
        commitObjects(Arrays.asList(object), null, null);
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
        commitObjects(Arrays.asList(object), null, null);
        object = db.getObject("test/type/date");
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Date.class.getName()));
        assertThat((Date) value, is(date));
    }
    
    @Test
    public void testIfCreatedCommitContainsRevisionNumber_shouldReturnNotNull() throws Exception {
        EDBCommit ci = getEDBCommit();
        assertThat(ci.getRevisionNumber(), notNullValue());
    }
    
    @Test(expected = EDBException.class)
    public void testIfWrongParentCausesCommitError_shouldThrowException() throws Exception {
        db.commit(getEDBCommit()); // add one entry so that there is actually a head
        EDBCommit ci = getEDBCommit();
        ci.insert(createRandomTestObject("/wrongparent/1"));
        EDBCommit ci2 = getEDBCommit();
        ci2.insert(createRandomTestObject("/wrongparent/2"));
        db.commit(ci2);
        db.commit(ci);
    }
}
