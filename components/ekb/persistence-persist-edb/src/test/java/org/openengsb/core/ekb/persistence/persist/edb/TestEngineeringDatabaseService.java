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

package org.openengsb.core.ekb.persistence.persist.edb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBDiff;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBStage;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.common.EDBConverterUtils;
import org.openengsb.core.ekb.persistence.persist.edb.models.EngineeringObjectModel;
import org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelA;
import org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelB;

/**
 * The TestEngineeringDatabaseService is a simple implementation of the
 * EngineeringDatabaseService for testing the Engineering Object support.
 */
public class TestEngineeringDatabaseService implements EngineeringDatabaseService
{

    private String getModelOid(String modelId) {
        return String.format("%s/%s", ContextHolder.get().getCurrentContextId(), modelId);
    }

    @Override
    public EDBObject getObject(String arg0, Long timestamp) throws EDBException {
        if (arg0.equals(getModelOid("objectA/reference/1"))) {
            EDBObject edbObject = new EDBObject(getModelOid("objectA/reference/1"));
            edbObject.putEDBObjectEntry("nameA", "firstObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelA.class.getName());
            return edbObject;
        }
        if (arg0.equals(getModelOid("objectA/reference/2"))) {
            EDBObject edbObject = new EDBObject(getModelOid("objectA/reference/2"));
            edbObject.putEDBObjectEntry("nameA", "updatedFirstObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelA.class.getName());
            return edbObject;
        }
        if (arg0.equals(getModelOid("objectB/reference/1"))) {
            EDBObject edbObject = new EDBObject(getModelOid("objectB/reference/1"));
            edbObject.putEDBObjectEntry("nameB", "secondObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelB.class.getName());
            return edbObject;
        }
        if (arg0.equals(getModelOid("objectB/reference/2"))) {
            EDBObject edbObject = new EDBObject(getModelOid("objectB/reference/2"));
            edbObject.putEDBObjectEntry("nameB", "updatedSecondObject");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SourceModelB.class.getName());
            return edbObject;
        }
        if (arg0.equals(getModelOid("common/reference/1"))) {
            EDBObject edbObject = new EDBObject(getModelOid("common/reference/1"));
            edbObject.putEDBObjectEntry("modelAId", "objectA/reference/1");
            edbObject.putEDBObjectEntry("modelBId", "objectB/reference/1");
            edbObject.putEDBObjectEntry("nameA", "firstObject");
            edbObject.putEDBObjectEntry("nameB", "secondObject");
            edbObject.putEDBObjectEntry("internalModelName", "common/reference/1");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, EngineeringObjectModel.class.getName());
            return edbObject;
        }
        if (arg0.equals(getModelOid("common/reference/2"))) {
            EDBObject edbObject = new EDBObject(getModelOid("common/reference/2"));
            edbObject.putEDBObjectEntry("modelAId", "objectA/reference/1");
            edbObject.putEDBObjectEntry("nameA", "firstObject");
            edbObject.putEDBObjectEntry("internalModelName", "common/reference/2");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, EngineeringObjectModel.class.getName());
            return edbObject;
        }
        return null;
    }

    @Override
    public EDBObject getObject(String oid) throws EDBException {
        return getObject(oid, (String) null);
    }

    @Override
    public List<EDBObject> query(QueryRequest request) throws EDBException {
        String reference = (String) request.getParameter(EDBConverterUtils.REFERENCE_PREFIX + "%");
        if (reference.equals(getModelOid("objectA/reference/1"))
            || reference.equals(getModelOid("objectB/reference/1"))) {
            EDBObject edbObject = new EDBObject(getModelOid("common/reference/1"));
            edbObject.putEDBObjectEntry("modelAId", "objectA/reference/1");
            edbObject.putEDBObjectEntry("modelBId", "objectB/reference/1");
            edbObject.putEDBObjectEntry("nameA", "firstObject");
            edbObject.putEDBObjectEntry("nameB", "secondObject");
            edbObject.putEDBObjectEntry("internalModelName", "common/reference/1");
            edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, EngineeringObjectModel.class.getName());
            return Arrays.asList(edbObject);
        }
        return new ArrayList<EDBObject>();
    }

    @Override
    public Long commit(EDBCommit arg0) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getCommit(Long arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String arg0, Object arg1) throws EDBException {
        return null;
    }

    @Override
    public EDBDiff getDiff(Long arg0, Long arg1) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHead() throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHead(long arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHistory(String arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHistoryForTimeRange(String arg0, Long arg1, Long arg2) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getLastCommit(Map<String, Object> arg0) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getLastCommitByKeyValue(String arg0, Object arg1) throws EDBException {
        return null;
    }

    @Override
    public List<EDBLogEntry> getLog(String arg0, Long arg1, Long arg2) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getObjects(List<String> arg0) throws EDBException {
        return null;
    }

    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> arg0) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String arg0, Object arg1) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit createEDBCommit(List<EDBObject> arg0, List<EDBObject> arg1, List<EDBObject> arg2)
        throws EDBException {
        return null;
    }

    @Override
    public UUID getCurrentRevisionNumber() throws EDBException {
        return null;
    }

    @Override
    public UUID getLastRevisionNumberOfContext(String contextId) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getCommitByRevision(String revision) throws EDBException {
        return null;
    }

    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request) throws EDBException {
        return null;
    }

    @Override
    public EDBObject getObject(String oid, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getObjects(List<String> oids, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHead(String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHistory(String oid, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHistoryForTimeRange(String oid, Long from, Long to, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getHead(long timestamp, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String key, Object value, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> query, String sid) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getCommit(Long from, String sid) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getLastCommitByKeyValue(String key, Object value, String sid) throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getLastCommit(Map<String, Object> queryMap, String sid) throws EDBException {
        return null;
    }

    @Override
    public EDBDiff getDiff(Long firstTimestamp, Long secondTimestamp, String sid1, String sid2) throws EDBException {
        return null;
    }

    @Override
    public List<String> getResurrectedOIDs(String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> queryMap, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String key, Object value, String sid) 
        throws EDBException {
        return null;
    }

    @Override
    public EDBCommit createEDBCommit(EDBStage stage, List<EDBObject> inserts, 
        List<EDBObject> updates, List<EDBObject> deletes) throws EDBException {
        return null;
    }

    @Override
    public UUID getCurrentRevisionNumber(EDBStage stage) throws EDBException {
        return null;
    }

    @Override
    public List<EDBObject> query(QueryRequest request, String sid) throws EDBException {
        return null;
    }

    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request, String sid) 
        throws EDBException {
        return null;
    }

    @Override
    public EDBCommit getCommitByRevision(String revision, String sid) throws EDBException {
        return null;
    }

    @Override
    public UUID getLastRevisionNumberOfContext(String contextId, String sid) throws EDBException {
        return null;
    }

}
