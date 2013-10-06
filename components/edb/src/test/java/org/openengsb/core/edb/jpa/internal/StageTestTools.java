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

import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.api.EDBStage;
import org.springframework.util.Assert;

/**
 *
 * @author vauve_000
 */
public class StageTestTools implements Tools {

	private TestEDBService db;
	
	protected String getSid() {
		return "stage1";
	}
	
	protected JPAStage getStage() {
		JPAStage stage = new JPAStage();
		stage.setStageId(getSid());
		stage.setCreator("sveti");
		return stage;
	}
	
	@Override
	public void setDb(TestEDBService db)
	{
		this.db = db;
	}

	@Override
	public EDBCommit createEDBCommit(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes)
	{
		return db.createEDBCommit(getStage(), inserts, updates, deletes);
	}

	@Override
	public EDBObject getEDBObject(String oid)
	{
		return db.getObject(oid, getSid());
	}

	@Override
	public EDBObject createEDBObject(String oid, Map<String, EDBObjectEntry> data)
	{
		return new EDBObject(oid, data, getStage());
	}

	@Override
	public EDBObject createEDBObject(String oid)
	{
		return new EDBObject(oid, getStage());
	}

	@Override
	public void assertStage(EDBStage actual)
	{
		JPAStage expected = getStage();
		
		assertThat(actual, notNullValue());
		assertThat(actual.getStageId(), is(expected.getStageId()));
		assertThat(actual.getCreator(), is(expected.getCreator()));
	}

	@Override
	public List<EDBCommit> getCommitsByKeyValue(String key, Object value)
	{
		return db.getCommitsByKeyValue(key, value, getSid());
	}

	@Override
	public List<EDBObject> getHistory(String oid)
	{
		return db.getHistory(oid, getSid());
	}

	@Override
	public List<EDBLogEntry> getLog(String oid, Long from, Long to)
	{
		return db.getLog(oid, from, to, getSid());
	}

	@Override
	public Diff getDiff(Long firstTimestamp, Long secondTimestamp)
	{
		return db.getDiff(firstTimestamp, secondTimestamp, getSid(), getSid());
	}

	@Override
	public List<String> getResurrectedOIDs()
	{
		return db.getResurrectedOIDs(getSid());
	}

	@Override
	public List<EDBObject> queryByKeyValue(String key, Object value)
	{
		return db.queryByKeyValue(key, value, getSid());
	}

	@Override
	public List<EDBObject> query(Map<String, Object> queryMap, Long timestamp)
	{
		return db.query(queryMap, timestamp, getSid());
	}

	@Override
	public List<EDBObject> queryByMap(Map<String, Object> queryMap)
	{
		return db.queryByMap(queryMap, getSid());
	}
}
