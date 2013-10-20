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
import java.util.UUID;
import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
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
public class TestTools implements Tools {

    private TestEDBService db;

    @Override
    public void setDb(TestEDBService db) {
        this.db = db;
    }

    @Override
    public EDBCommit createEDBCommit(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes) {
        return db.createEDBCommit(inserts, updates, deletes);
    }

    @Override
    public EDBObject getEDBObject(String oid) {
        return db.getObject(oid);
    }

    @Override
    public EDBObject createEDBObject(String oid, Map<String, EDBObjectEntry> data) {
        return new EDBObject(oid, data);
    }

    @Override
    public EDBObject createEDBObject(String oid) {
        return new EDBObject(oid);
    }

    @Override
    public void assertStage(EDBStage actual) {
        Assert.isNull(actual);
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String key, Object value) {
        return db.getCommitsByKeyValue(key, value);
    }

    @Override
    public List<EDBObject> getHistory(String oid) {
        return db.getHistory(oid);
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to) {
        return db.getLog(oid, from, to);
    }

    @Override
    public Diff getDiff(Long firstTimestamp, Long secondTimestamp) {
        return db.getDiff(firstTimestamp, secondTimestamp);
    }

    @Override
    public List<String> getResurrectedOIDs() {
        return db.getResurrectedOIDs();
    }

    @Override
    public List<EDBObject> query(QueryRequest request) {
        return db.query(request);
    }

    @Override
    public EDBCommit getCommitByRevision(String revision) {
        return db.getCommitByRevision(revision);
    }

    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request) {
        return db.getRevisionsOfMatchingCommits(request);
    }

    @Override
    public UUID getLastRevisionNumberOfContext(String contextId) {
        return db.getLastRevisionNumberOfContext(contextId);
    }
}
