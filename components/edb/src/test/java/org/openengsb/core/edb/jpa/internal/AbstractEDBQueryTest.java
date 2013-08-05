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
}
