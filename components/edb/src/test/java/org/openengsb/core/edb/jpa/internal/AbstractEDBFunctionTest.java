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
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.Test;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBStage;

/**
 *
 * @author vauve_000
 */
public class AbstractEDBFunctionTest extends AbstractEDBTest {
	
	private String getSid(JPAStage stage) {
		if(stage != null)
			return stage.getStageId();
		
		return null;
	}
	
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
}
