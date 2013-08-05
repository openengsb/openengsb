/*
 * Copyright 2013 vauve_000.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.edb.jpa.internal;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.Test;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.api.EDBStage;

/**
 *
 * @author vauve_000
 */
public class AbstractEDBFunctionTest extends AbstractEDBTest {
	
	private void assertStage(EDBStage actual, EDBStage expected) {
		if(expected == null)
		{
			return;
		}
		
		assertThat(actual, notNullValue());
		assertThat(actual.getStageId(), is(expected.getStageId()));
		assertThat(actual.getCreator(), is(expected.getCreator()));
	}
	
    protected void testCommit_shouldWork(JPAStage stage) throws Exception {
		
        EDBObject obj = new EDBObject("Tester", stage);
        obj.putEDBObjectEntry("Test", "Hooray");
        EDBCommit ci = db.createEDBCommit(stage, Arrays.asList(obj), null, null);

        long time = db.commit(ci);

        obj = null;
        obj = db.getObject("Tester", getSid(stage));
        String hooray = obj.getString("Test");

        assertThat(obj, notNullValue());
        assertThat(hooray, notNullValue());

		assertStage(obj.getEDBStage(), stage);
		
        checkTimeStamps(Arrays.asList(time));
    }
	
    protected void testGetCommits_shouldWork(JPAStage stage) throws Exception {
        EDBObject obj = new EDBObject("TestObject", stage);
        obj.putEDBObjectEntry("Bla", "Blabla");
        EDBCommit ci = db.createEDBCommit(stage, Arrays.asList(obj), null, null);

        long time = db.commit(ci);

        List<EDBCommit> commits1 = db.getCommitsByKeyValue("context", "testcontext", getSid(stage));
        List<EDBCommit> commits2 = db.getCommitsByKeyValue("context", "DoesNotExist", getSid(stage));

        assertThat(commits1.size(), is(1));
        assertThat(commits2.size(), is(0));
		
		assertStage(commits1.get(0).getEDBStage(), stage);

        checkTimeStamps(Arrays.asList(time));
    }
	
    protected void testGetHistoryAndCheckForElements_shouldWork(JPAStage stage) throws Exception {
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        long time4 = 0;
        HashMap<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("Lock", "Key", data1, stage);
        putValue("Door", "Bell", data1, stage);
        putValue("Cat", "Spongebob", data1, stage);
        EDBObject v1 = new EDBObject("/history/object", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/useless/1", stage));
        ci.insert(v1);

        time1 = db.commit(ci);

        HashMap<String, EDBObjectEntry> data2 = (HashMap<String, EDBObjectEntry>) data1.clone();
        putValue("Lock", "Smith", data2, stage);
        EDBObject v2 = new EDBObject("/history/object", data2, stage);
        ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/useless/2", stage));
        ci.update(v2);

        time2 = db.commit(ci);

        HashMap<String, EDBObjectEntry> data3 = (HashMap<String, EDBObjectEntry>) data2.clone();
        ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/useless/3", stage));
        ci.insert(createRandomTestObject("/useless/4", stage));
        time3 = db.commit(ci);

        putValue("Cat", "Dog", data3, stage);
        EDBObject v3 = new EDBObject("/history/object", data3, stage);
        ci = getEDBCommit(stage);
        ci.update(v3);
        ci.insert(createRandomTestObject("/useless/5", stage));

        time4 = db.commit(ci);

        List<EDBObject> history = db.getHistory("/history/object", getSid(stage));

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
		
		assertStage(history.get(0).getEDBStage(), stage);
		assertStage(history.get(1).getEDBStage(), stage);
		assertStage(history.get(2).getEDBStage(), stage);

        checkTimeStamps(Arrays.asList(time1, time2, time3, time4));
    }
	
	protected void testHistoryOfDeletion_shouldWork(JPAStage stage) throws Exception {
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/deletion/1", stage));
        long time1 = db.commit(ci);

        ci = getEDBCommit(stage);
        ci.delete("/deletion/1");
        long time2 = db.commit(ci);

        List<EDBObject> history = db.getHistory("/deletion/1", getSid(stage));

        assertThat(history.size(), is(2));
        assertThat(history.get(0).isDeleted(), is(false));
        assertThat(history.get(1).isDeleted(), is(true));
		
		assertStage(history.get(0).getEDBStage(), stage);
		assertStage(history.get(1).getEDBStage(), stage);

        checkTimeStamps(Arrays.asList(time1, time2));
    }
	
	protected void testGetLog_shouldWork(JPAStage stage) throws Exception {
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        long time4 = 0;
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("Burger", "Salad", data1, stage);
        putValue("Bla", "Blub", data1, stage);
        putValue("Cheese", "Butter", data1, stage);
        EDBObject v1 = new EDBObject("/history/test/object", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/useless/test/1", stage));
        ci.insert(createRandomTestObject("/deletion/test/1", stage));
        ci.insert(v1);

        time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = Maps.newHashMap(data1);
        putValue("Burger", "Meat", data2, stage);
        EDBObject v2 = new EDBObject("/history/test/object", data2, stage);
        ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/useless/test/2", stage));
        ci.delete("/deletion/test/1");
        ci.update(v2);
        time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data3 = Maps.newHashMap(data2);
        ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/useless/test/3", stage));
        ci.insert(createRandomTestObject("/useless/test/4", stage));
        time3 = db.commit(ci);
        putValue("Cheese", "Milk", data3, stage);

        EDBObject v3 = new EDBObject("/history/test/object", data3, stage);
        ci = getEDBCommit(stage);
        ci.update(v3);
        ci.insert(createRandomTestObject("/useless/test/5", stage));

        time4 = db.commit(ci);

        List<EDBLogEntry> log = db.getLog("/history/test/object", time1, time4, getSid(stage));
        assertThat(log.size(), is(3));

        checkTimeStamps(Arrays.asList(time1, time2, time3, time4));
    }
	
	protected void testDiff_shouldWork(JPAStage stage) throws Exception {
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1, stage);
        putValue("KeyB", "Value B 1", data1, stage);
        putValue("KeyC", "Value C 1", data1, stage);
        EDBObject v1 = new EDBObject("/diff/object", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v1);
        time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = Maps.newHashMap(data1);

        putValue("KeyD", "Value D 1", data2, stage);
        putValue("KeyA", "Value A 2", data2, stage);
        EDBObject v2 = new EDBObject("/diff/object", data2, stage);
        ci = getEDBCommit(stage);
        ci.update(v2);
        time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data3 = Maps.newHashMap(data2);

        data3.remove("KeyB");
        putValue("KeyC", "Value C 3", data3, stage);
        EDBObject v3 = new EDBObject("/diff/object", data3, stage);
        ci = getEDBCommit(stage);
        ci.update(v3);
        time3 = db.commit(ci);

        checkTimeStamps(Arrays.asList(time1, time2, time3));

        Diff diffAb = db.getDiff(time1, time2, getSid(stage), getSid(stage));
        Diff diffBc = db.getDiff(time2, time3, getSid(stage), getSid(stage));
        Diff diffAc = db.getDiff(time1, time3, getSid(stage), getSid(stage));

        assertThat(diffAb.getDifferenceCount(), is(1));
        assertThat(diffBc.getDifferenceCount(), is(1));
        assertThat(diffAc.getDifferenceCount(), is(1));
    }
	
	protected void testGetResurrectedOIDs_shouldWork(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1, stage);
        EDBObject v1 = new EDBObject("/ress/object", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v1);
        long time1 = db.commit(ci);

        v1 = new EDBObject("/ress/object2", data1, stage);
        ci = getEDBCommit(stage);
        ci.insert(v1);
        ci.delete("/ress/object");
        long time2 = db.commit(ci);

        v1 = new EDBObject("/ress/object", data1, stage);
        ci = getEDBCommit(stage);
        ci.delete("/ress/object2");
        ci.update(v1);
        long time3 = db.commit(ci);

        List<String> oids = db.getResurrectedOIDs(getSid(stage));

        assertThat(oids.contains("/ress/object"), is(true));
        assertThat(oids.contains("/ress/object2"), is(false));

        checkTimeStamps(Arrays.asList(time1, time2, time3));
    }
	
	protected void testCommitTwiceSameCommit_shouldThrowError(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1, stage);
        EDBObject v1 = new EDBObject("/fail/object", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v1);
        db.commit(ci);
        db.commit(ci);
    }
	
    protected void testCommitEDBObjectsInsert_shouldWork(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/1", stage);
        object.putEDBObjectEntry("bla", "blub");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);

        commitObjects(inserts, null, null, stage);

        object = db.getObject("/commit/test/insert/1", getSid(stage));
        assertThat(object.getString("bla"), is("blub"));
        assertThat(object.getObject(EDBConstants.MODEL_VERSION, Integer.class), is(1));
    }

    protected void testCommitEDBObjectsInsertDouble_shouldThrowException(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/2", stage);
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);

        commitObjects(inserts, null, null, stage);
        commitObjects(inserts, null, null, stage);
    }
	
	protected void testIfConflictDetectionIsWorking_shouldThrowException(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/insert/3", stage);
        object.putEDBObjectEntry("bla", "blub");
        List<EDBObject> inserts = new ArrayList<EDBObject>();
        inserts.add(object);
        commitObjects(inserts, null, null, stage);
        object = db.getObject("/commit/test/insert/3", getSid(stage));
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(0));
        object.putEDBObjectEntry("test", "test");
        commitObjects(inserts, Arrays.asList(object), null, stage);
    }
	
	protected void testCommitEDBObjectsUpdate_shouldWork(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/1", stage);
        object.putEDBObjectEntry("testkey", "testvalue");
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);

        commitObjects(objects, null, null, stage);

        EDBObject first = db.getObject("/commit/test/update/1", getSid(stage));

        objects.clear();
        object.putEDBObjectEntry("testkey", "testvalue1");
        objects.add(object);

        commitObjects(null, objects, null, stage);

        EDBObject second = db.getObject("/commit/test/update/1", getSid(stage));

        assertThat(first.getObject(EDBConstants.MODEL_VERSION, Integer.class), is(1));
        assertThat(first.getString("testkey"), is("testvalue"));
        assertThat(second.getObject(EDBConstants.MODEL_VERSION, Integer.class), is(2));
        assertThat(second.getString("testkey"), is("testvalue1"));
    }
	
	protected void testCommitEDBObjectsUpdateVerstionConflict_shouldThrowException(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/2", stage);
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        commitObjects(objects, null, null, stage);
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(0));
        object.putEDBObjectEntry("test", "test");
        commitObjects(null, objects, null, stage);
    }
	
	protected void testCommitEDBObjectsUpdateVerstionConflict2_shouldThrowException(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/update/3", stage);
        commitObjects(Arrays.asList(object), null, null, stage);
        EDBObject receive1 = db.getObject("/commit/test/update/3", getSid(stage));
        EDBObject receive2 = db.getObject("/commit/test/update/3", getSid(stage));
        receive1.putEDBObjectEntry("test1", "test1");
        receive2.putEDBObjectEntry("test1", "test2");
        commitObjects(null, Arrays.asList(receive1), null, stage);
        commitObjects(null, Arrays.asList(receive2), null, stage);
    }
	
	protected void testCommitEDBObjectsDelete_shouldWork(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/1", stage);
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        commitObjects(objects, null, null, stage);
        commitObjects(null, null, objects, stage);

        EDBObject entry = db.getObject("/commit/test/delete/1", getSid(stage));
        assertThat(entry.isDeleted(), is(true));
    }
	
    protected void testCommitEDBObjectsDeleteNonExisting_shouldThrowException(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/2", stage);
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        commitObjects(null, null, objects, stage);
    }

    protected void testCommitEDBObjectsDeleteAlreadyDeleted_shouldThrowException(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("/commit/test/delete/3", stage);
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.add(object);
        commitObjects(objects, null, null, stage);
        commitObjects(null, null, objects, stage);
        commitObjects(null, null, objects, stage);
    }
	
    protected void testIfOtherTypesThanStringWorks_shouldProcessInteger(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("test/type/integer", stage);
        object.putEDBObjectEntry("value", Integer.valueOf(42));
        commitObjects(Arrays.asList(object), null, null, stage);
        object = db.getObject("test/type/integer", getSid(stage));
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Integer.class.getName()));
        assertThat((Integer) value, is(42));
    }

    protected void testIfOtherTypesThanStringWorks_shouldProcessBoolean(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("test/type/boolean", stage);
        object.putEDBObjectEntry("value", Boolean.TRUE);
        commitObjects(Arrays.asList(object), null, null, stage);
        object = db.getObject("test/type/boolean", getSid(stage));
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Boolean.class.getName()));
        assertThat((Boolean) value, is(Boolean.TRUE));
    }
	
	protected void testIfOtherTypesThanStringWorks_shouldProcessDate(JPAStage stage) throws Exception {
        EDBObject object = new EDBObject("test/type/date", stage);
        Date date = new Date();
        object.putEDBObjectEntry("value", date);
        commitObjects(Arrays.asList(object), null, null, stage);
        object = db.getObject("test/type/date", getSid(stage));
        Object value = object.getObject("value");
        assertThat(value.getClass().getName(), is(Date.class.getName()));
        assertThat((Date) value, is(date));
    }
	
	protected void testIfWrongParentCausesCommitError_shouldThrowException(JPAStage stage) throws Exception {
        db.commit(getEDBCommit(stage)); // add one entry so that there is actually a head
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(createRandomTestObject("/wrongparent/1", stage));
        EDBCommit ci2 = getEDBCommit(stage);
        ci2.insert(createRandomTestObject("/wrongparent/2", stage));
        db.commit(ci2);
        db.commit(ci);
    }
}
