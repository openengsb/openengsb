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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.api.EDBStage;

public class EDBStagedTestData implements EDBTestDataGenerator {
    private TestEDBService db;

    public EDBStagedTestData(TestEDBService db) {
        this.db = db;
    }

    @Override
    public EDBCommit createEDBCommit(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes) {
        return db.createEDBCommit(getStage(), inserts, updates, deletes);
    }
    
    private JPAStage getStage() {
        JPAStage stage = new JPAStage();
        stage.setStageId("stage1");
        stage.setCreator("test");
        return stage;
    }

    @Override
    public EDBObject getEDBObject(String oid) {
        return db.getObject(oid, "stage1");
    }

    @Override
    public EDBObject createEDBObject(String oid, Map<String, EDBObjectEntry> data) {
        return new EDBObject(oid, data, getStage());
    }

    @Override
    public EDBObject createEDBObject(String oid) {
        return new EDBObject(oid, getStage());
    }

    @Override
    public void assertStage(EDBStage actual) {
        JPAStage expected = getStage();

        assertThat(actual, notNullValue());
        assertThat(actual.getStageId(), is(expected.getStageId()));
        assertThat(actual.getCreator(), is(expected.getCreator()));
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String key, Object value) {
        return db.getCommitsByKeyValue(key, value, "stage1");
    }

    @Override
    public List<EDBObject> getHistory(String oid) {
        return db.getHistory(oid, "stage1");
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to) {
        return db.getLog(oid, from, to, "stage1");
    }

    @Override
    public Diff getDiff(Long firstTimestamp, Long secondTimestamp) {
        return db.getDiff(firstTimestamp, secondTimestamp, "stage1", "stage1");
    }

    @Override
    public List<String> getResurrectedOIDs() {
        return db.getResurrectedOIDs("stage1");
    }

    @Override
    public List<EDBObject> query(QueryRequest request) {
        return db.query(request, "stage1");
    }

    @Override
    public EDBCommit getCommitByRevision(String revision) {
        return db.getCommitByRevision(revision, "stage1");
    }

    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request) {
        return db.getRevisionsOfMatchingCommits(request, "stage1");
    }

    @Override
    public UUID getLastRevisionNumberOfContext(String contextId) {
        return db.getLastRevisionNumberOfContext(contextId, "stage1");
    }
}
