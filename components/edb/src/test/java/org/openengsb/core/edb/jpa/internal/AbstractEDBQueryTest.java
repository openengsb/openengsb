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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;

/**
 *
 * @author vauve_000
 */
public abstract class AbstractEDBQueryTest extends AbstractEDBTest
{
	protected void testQueryWithSomeAspects_shouldWork(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("A", "B", data1, stage);
        putValue("Cow", "Milk", data1, stage);
        putValue("Dog", "Food", data1, stage);
        EDBObject v1 = new EDBObject("/test/query1", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v1);
        long time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = new HashMap<String, EDBObjectEntry>();
        putValue("Cow", "Milk", data2, stage);
        putValue("House", "Garden", data2, stage);
        v1 = new EDBObject("/test/query2", data2, stage);
        ci = getEDBCommit();
        ci.insert(v1);
        long time2 = db.commit(ci);

        List<EDBObject> list1 = db.queryByKeyValue("A", "B", getSid(stage));
        List<EDBObject> list2 = db.queryByMap(new HashMap<String, Object>() {
            {
                put("A", "B");
                put("Dog", "Food");
            }
        }, getSid(stage));

        List<EDBObject> list3 = db.queryByMap(new HashMap<String, Object>() {
            {
                put("Cow", "Milk");
            }
        }, getSid(stage));

        List<EDBObject> list4 = db.queryByMap(new HashMap<String, Object>() {
            {
                put("A", "B");
                put("Cow", "Milk");
                put("House", "Garden");
            }
        }, getSid(stage));

        assertThat(list1.size(), is(1));
        assertThat(list2.size(), is(1));
        assertThat(list3.size(), is(2));
        assertThat(list4.size(), is(0));

        checkTimeStamps(Arrays.asList(time1, time2));
    }
	
	protected void testQueryOfOldVersion_shouldWork(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 1", data1v1, stage);
        putValue("pre:KeyB", "pre:Value A 1", data1v1, stage);
        EDBObject v11 = new EDBObject("pre:/test/object1", data1v1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v11);
        Map<String, EDBObjectEntry> data2v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 2", data2v1, stage);
        putValue("pre:KeyB", "pre:Value A 1", data2v1, stage);
        EDBObject v12 = new EDBObject("pre:/test/object2", data2v1, stage);
        ci.insert(v12);
        Map<String, EDBObjectEntry> data3v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 3", data3v1, stage);
        putValue("pre:KeyB", "pre:Value A 1", data3v1, stage);
        EDBObject v13 = new EDBObject("pre:/test/object3", data3v1, stage);
        ci.insert(v13);

        long time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data1v2 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 1", data1v2, stage);
        putValue("pre:KeyB", "pre:Value A 1", data1v2, stage);
        EDBObject v21 = new EDBObject("pre:/test/object1", data1v2, stage);
        ci = getEDBCommit(stage);
        ci.update(v21);
        Map<String, EDBObjectEntry> data2v2 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 2", data2v2, stage);
        putValue("pre:KeyB", "pre:Value A 1", data2v2, stage);
        EDBObject v22 = new EDBObject("pre:/test/object2", data2v2, stage);
        ci.update(v22);
        Map<String, EDBObjectEntry> data4v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 4", data4v1, stage);
        putValue("pre:KeyB", "pre:Value A 1", data4v1, stage);
        EDBObject v23 = new EDBObject("pre:/test/object4", data4v1, stage);
        ci.update(v23);

        long time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data1v3 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 1", data1v3, stage);
        putValue("pre:KeyB", "pre:Value A 1", data1v3, stage);
        EDBObject v31 = new EDBObject("pre:/test/object1", data1v3, stage);
        ci = getEDBCommit(stage);
        ci.update(v31);
        Map<String, EDBObjectEntry> data2v3 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 2a", data2v3, stage);
        putValue("pre:KeyB", "pre:Value A 1", data2v3, stage);
        EDBObject v32 = new EDBObject("pre:/test/object2", data2v3, stage);
        ci.update(v32);
        Map<String, EDBObjectEntry> data4v2 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 4", data4v2, stage);
        putValue("pre:KeyB", "pre:Value A 1", data4v2, stage);
        EDBObject v33 = new EDBObject("pre:/test/object4", data4v2, stage);
        ci.update(v33);

        long time3 = db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pre:KeyB", "pre:Value A 1");
        List<EDBObject> result = db.query(map, time2, getSid(stage));

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
	
	protected void testQueryWithTimestamp_shouldWork(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1, stage);
        putValue("Cow", "Milk", data1, stage);
        putValue("Dog", "Food", data1, stage);
        EDBObject v1 = new EDBObject("/test/querynew1", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("Dog", "Food", data1, stage);
        v1 = new EDBObject("/test/querynew1", data1, stage);
        ci = getEDBCommit(stage);
        ci.update(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1, stage);
        putValue("Dog", "Food", data1, stage);
        v1 = new EDBObject("/test/querynew2", data1, stage);
        ci = getEDBCommit(stage);
        ci.insert(v1);
        db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("K", "B");
        List<EDBObject> result = db.query(map, System.currentTimeMillis(), getSid(stage));
        assertThat(result.size(), is(1));
    }
	
	protected void testQueryWithTimestampAndEmptyMap_shouldWork(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1, stage);
        putValue("Cow", "Milk", data1, stage);
        putValue("Dog", "Food", data1, stage);
        EDBObject v1 = new EDBObject("/test/querynew3", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("Dog", "Food", data1, stage);
        v1 = new EDBObject("/test/querynew3", data1, stage);
        ci = getEDBCommit(stage);
        ci.update(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1, stage);
        putValue("Dog", "Food", data1, stage);
        v1 = new EDBObject("/test/querynew4", data1, stage);
        ci = getEDBCommit(stage);
        ci.insert(v1);
        db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        List<EDBObject> result = db.query(map, System.currentTimeMillis(), getSid(stage));
        EDBObject result1 = getEDBObjectOutOfList(result, "/test/querynew3");
        EDBObject result2 = getEDBObjectOutOfList(result, "/test/querynew4");
        assertThat(result.size(), is(2));
        assertThat(result1.containsKey("K"), is(false));
        assertThat(result2.containsKey("Dog"), is(true));
    }
	
	protected void testQueryOfLastKnownVersion_shouldWork(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1v1, stage);
        putValue("KeyB", "Value A 1", data1v1, stage);
        EDBObject v11 = new EDBObject("/test/object1", data1v1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v11);
        Map<String, EDBObjectEntry> data2v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 2", data2v1, stage);
        putValue("KeyB", "Value A 1", data2v1, stage);
        EDBObject v12 = new EDBObject("/test/object2", data2v1, stage);
        ci.insert(v12);
        Map<String, EDBObjectEntry> data3v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 3", data3v1, stage);
        putValue("KeyB", "Value A 1", data3v1, stage);
        EDBObject v13 = new EDBObject("/test/object3", data3v1, stage);
        ci.insert(v13);

        long time1 = db.commit(ci);

        ci = getEDBCommit(stage);
        Map<String, EDBObjectEntry> data1v2 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1v2, stage);
        putValue("KeyB", "Value A 1", data1v2, stage);
        EDBObject v21 = new EDBObject("/test/object1", data1v2, stage);
        ci.update(v21);
        Map<String, EDBObjectEntry> data2v2 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 2", data2v2, stage);
        putValue("KeyB", "Value A 1", data2v2, stage);
        EDBObject v22 = new EDBObject("/test/object2", data2v2, stage);
        ci.update(v22);

        long time2 = db.commit(ci);

        ci = getEDBCommit(stage);
        Map<String, EDBObjectEntry> data2v3 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 2a", data2v3, stage);
        putValue("KeyB", "Value A 1", data2v3, stage);
        EDBObject v32 = new EDBObject("/test/object2", data2v3, stage);
        ci.update(v32);
        Map<String, EDBObjectEntry> data4v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 4", data4v1, stage);
        putValue("KeyB", "Value A 1", data4v1, stage);
        EDBObject v33 = new EDBObject("/test/object4", data4v1, stage);
        ci.insert(v33);

        long time3 = db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("KeyB", "Value A 1");
        List<EDBObject> result = db.query(map, time3, getSid(stage));

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
	
	protected void testIfQueryingWithLikeWorks_shouldWork(JPAStage stage) throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("bla_kdjer", "test", data1, stage);
        EDBObject v1 = new EDBObject("/test/query/8", data1, stage);
        EDBCommit ci = getEDBCommit(stage);
        ci.insert(v1);
        db.commit(ci);
        List<EDBObject> result = db.queryByKeyValue("bla%", "test", getSid(stage));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/8"));
    }
}
