/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The AASTI licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.openengsb.core.edb.jpa.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBStage;
import org.openengsb.core.edb.api.hooks.EDBBeginCommitHook;
import org.openengsb.core.edb.api.hooks.EDBErrorHook;
import org.openengsb.core.edb.api.hooks.EDBPostCommitHook;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.dao.JPADao;
import org.openengsb.core.edb.jpa.internal.util.EDBUtils;

/**
 * The implementation of the EngineeringDatabaseService, extending the AbstractEDBService
 */
public class EDBService extends AbstractEDBService {

    private JPADao dao;
    private AuthenticationContext authenticationContext;

    public EDBService(JPADao dao, AuthenticationContext authenticationContext,
            List<EDBBeginCommitHook> beginCommitHooks, List<EDBPreCommitHook> preCommitHooks,
            List<EDBPostCommitHook> postCommitHooks, List<EDBErrorHook> errorHooks,
            Boolean revisionCheckEnabled) {
        super(beginCommitHooks, preCommitHooks, postCommitHooks, errorHooks, revisionCheckEnabled, EDBService.class);
        this.dao = dao;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public Long commit(EDBCommit commit) throws EDBException {
        return performCommitLogic(commit);
    }

    @Override
    public EDBObject getObject(String oid) throws EDBException {
        getLogger().debug("loading newest JPAObject with the oid {}", new Object[]{oid});
        JPAObject temp = dao.getJPAObject(oid, null);
        return EDBUtils.convertJPAObjectToEDBObject(temp);
    }

    @Override
    public EDBObject getObject(String oid, String sid) throws EDBException {
        getLogger().debug("loading newest JPAObject with the oid {} and sid {}", new Object[]{oid, sid});
        JPAObject temp = dao.getJPAObject(oid, sid);
        return EDBUtils.convertJPAObjectToEDBObject(temp);
    }

    @Override
    public EDBObject getObject(String oid, Long timestamp) throws EDBException {
        getLogger().debug("loading JPAObject with the oid {} for timestamp {}", oid, timestamp);
        JPAObject temp = dao.getJPAObject(oid, timestamp);
        return EDBUtils.convertJPAObjectToEDBObject(temp);
    }

    @Override
    public List<EDBObject> getObjects(List<String> oids) throws EDBException {
        return this.getObjects(oids, null);
    }

    @Override
    public List<EDBObject> getObjects(List<String> oids, String sid) throws EDBException {
        List<JPAObject> objects = dao.getJPAObjects(oids, sid);
        return EDBUtils.convertJPAObjectsToEDBObjects(objects);
    }

    @Override
    public List<EDBObject> getHistory(String oid) throws EDBException {
        return this.getHistory(oid, null);
    }

    @Override
    public List<EDBObject> getHistory(String oid, String sid) throws EDBException {
        getLogger().debug("loading history of JPAStageObject with the oid {} and sid {}", new Object[]{oid, sid});
        List<JPAObject> objects = dao.getJPAObjectHistory(oid, sid);
        return EDBUtils.convertJPAObjectsToEDBObjects(objects);
    }

    @Override
    public List<EDBObject> getHistoryForTimeRange(String oid, Long from, Long to) throws EDBException {
        return this.getHistoryForTimeRange(oid, from, to, null);
    }

    @Override
    public List<EDBObject> getHistoryForTimeRange(String oid, Long from, Long to, String sid) throws EDBException {
        getLogger().debug("loading JPAObject with the oid {} and sid {} from "
                + "the timestamp {} to the timestamp {}", new Object[]{oid, sid, from, to});
        List<JPAObject> objects = dao.getJPAObjectHistory(oid, sid, from, to);
        return EDBUtils.convertJPAObjectsToEDBObjects(objects);
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to) throws EDBException {
        return this.getLog(oid, from, to, null);
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to, String sid) throws EDBException {
        getLogger().debug("loading the log of JPAObject with the oid {}, sid {} from timestamp {} to {}", 
                new Object[]{oid, sid, from, to});
        List<EDBObject> history = getHistoryForTimeRange(oid, from, to, sid);
        List<JPACommit> commits = dao.getJPACommit(oid, sid, from, to);
        if (history.size() != commits.size()) {
            throw new EDBException("inconsistent log " + Integer.toString(commits.size()) + " commits for "
                    + Integer.toString(history.size()) + " history entries");
        }
        List<EDBLogEntry> log = new ArrayList<EDBLogEntry>();
        for (int i = 0; i < history.size(); ++i) {
            log.add(new LogEntry(commits.get(i), history.get(i)));
        }
        return log;
    }

    @Override
    public List<EDBObject> getHead() throws EDBException {
        return this.getHead(null);
    }

    @Override
    public List<EDBObject> getHead(String sid) throws EDBException {
        return dao.getJPAHead(System.currentTimeMillis(), sid).getEDBObjects();
    }

    @Override
    public List<EDBObject> getHead(long timestamp) throws EDBException {
        return this.getHead(timestamp, null);
    }

    @Override
    public List<EDBObject> getHead(long timestamp, String sid) throws EDBException {
        getLogger().debug("load the elements of the JPAHead with the timestamp {}", timestamp);
        JPAHead head = dao.getJPAHead(timestamp, sid);
        if (head != null) {
            return head.getEDBObjects();
        }
        throw new EDBException("Failed to get head for timestamp " + Long.toString(timestamp));
    }

    @Override
    public List<EDBObject> query(QueryRequest request) throws EDBException {
        return this.query(request, null);
    }

    @Override
    public List<EDBObject> query(QueryRequest request, String sid) throws EDBException {
        getLogger().debug("Query for objects based on the request: {}", request);
        try {
            return EDBUtils.convertJPAObjectsToEDBObjects(dao.query(request, sid));
        } catch (Exception ex) {
            throw new EDBException("Failed to query for objects with the given map", ex);
        }
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String key, Object value) throws EDBException {
        return getCommitsByKeyValue(key, value, null);
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String key, Object value, String sid) throws EDBException {
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return getCommits(queryMap, sid);
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> queryMap) throws EDBException {
        return getCommits(queryMap, null);
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> queryMap, String sid) throws EDBException {
        List<JPACommit> commits = dao.getCommits(queryMap, sid);
        return new ArrayList<EDBCommit>(commits);
    }

    @Override
    public JPACommit getLastCommitByKeyValue(String key, Object value) throws EDBException {
        return this.getLastCommitByKeyValue(key, value, null);
    }

    @Override
    public JPACommit getLastCommitByKeyValue(String key, Object value, String sid) throws EDBException {
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return getLastCommit(queryMap, sid);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> queryMap) throws EDBException {
        return this.getLastCommit(queryMap, null);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> queryMap, String sid) throws EDBException {
        JPACommit result = dao.getLastCommit(queryMap, sid);
        return result;
    }

    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request) throws EDBException {
        return this.getRevisionsOfMatchingCommits(request, null);
    }

    @Override
    public List<CommitMetaInfo> getRevisionsOfMatchingCommits(CommitQueryRequest request, String sid) throws
            EDBException {
        getLogger().debug("Request revisions of matching commits for the request {}", request);
        return dao.getRevisionsOfMatchingCommits(request, sid);
    }

    @Override
    public UUID getCurrentRevisionNumber() throws EDBException {
        return this.getCurrentRevisionNumber(null);
    }

    @Override
    public UUID getCurrentRevisionNumber(EDBStage stage) throws EDBException {
        String sid = null;
        if (stage != null) {
            sid = stage.getStageId();
        }

        try {
            return getCommit(System.currentTimeMillis(), sid).getRevisionNumber();
        } catch (EDBException e) {
            getLogger().debug("There was no commit so far, so the current revision number is null");
            return null;
        }
    }

    @Override
    public UUID getLastRevisionNumberOfContext(String contextId) throws EDBException {
        return this.getLastRevisionNumberOfContext(contextId, null);
    }

    @Override
    public UUID getLastRevisionNumberOfContext(String contextId, String sid) throws EDBException {
        try {
            return getLastCommitByKeyValue("context", contextId, sid).getRevisionNumber();
        } catch (EDBException e) {
            getLogger().debug("There was no commit so far under this context, so null is returned");
        }
        return null;
    }

    @Override
    public JPACommit getCommit(Long from) throws EDBException {
        return this.getCommit(from, null);
    }

    @Override
    public JPACommit getCommit(Long from, String sid) throws EDBException {
        List<JPACommit> commits = dao.getJPACommit(from, sid);
        if (commits == null || commits.size() == 0) {
            throw new EDBException("There is no commit for this timestamp");
        } else if (commits.size() > 1) {
            throw new EDBException("There are more than one commit for one timestamp");
        }
        return commits.get(0);
    }

    @Override
    public EDBCommit getCommitByRevision(String revision) throws EDBException {
        return this.getCommitByRevision(revision, null);
    }

    @Override
    public EDBCommit getCommitByRevision(String revision, String sid) throws EDBException {
        return dao.getJPACommit(revision, sid);
    }

    @Override
    public Diff getDiff(Long firstTimestamp, Long secondTimestamp) throws EDBException {
        return this.getDiff(firstTimestamp, secondTimestamp, null, null);
    }

    @Override
    public Diff getDiff(Long firstTimestamp, Long secondTimestamp, String sid1, String sid2) throws EDBException {
        List<EDBObject> headA = getHead(firstTimestamp, sid1);
        List<EDBObject> headB = getHead(secondTimestamp, sid2);

        return new Diff(getCommit(firstTimestamp, sid1), getCommit(secondTimestamp, sid2), headA, headB);
    }

    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        return this.getResurrectedOIDs(null);
    }

    @Override
    public List<String> getResurrectedOIDs(String sid) throws EDBException {
        return dao.getResurrectedOIDs(sid);
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> queryMap) throws EDBException {
        return this.getStateOfLastCommitMatching(queryMap, null);
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(Map<String, Object> queryMap, String sid) throws EDBException {
        JPACommit ci = getLastCommit(queryMap, sid);
        return getHead(ci.getTimestamp());
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String key, Object value) throws EDBException {
        return this.getStateOfLastCommitMatchingByKeyValue(key, value, null);
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String key, Object value, String sid) 
        throws EDBException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(key, value);
        return getStateOfLastCommitMatching(query, sid);
    }

    @Override
    public EDBCommit createEDBCommit(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes) 
        throws EDBException {
        return this.createEDBCommit(null, inserts, updates, deletes);
    }

    @Override
    public EDBCommit createEDBCommit(EDBStage stage, List<EDBObject> inserts, List<EDBObject> updates,
            List<EDBObject> deletes) throws EDBException {
        String committer = getAuthenticatedUser();
        String contextId = getActualContextId();
        JPACommit commit = new JPACommit(committer, contextId);
        commit.setEDBStage(stage);
        getLogger().debug("creating commit for committer {} with contextId {}", committer, contextId);
        commit.insertAll(inserts);
        commit.updateAll(updates);
        commit.deleteAll(deletes);
        commit.setHeadRevisionNumber(getCurrentRevisionNumber(stage));
        return commit;
    }

    /**
     * Returns the actual authenticated user.
     */
    private String getAuthenticatedUser() {
        return (String) authenticationContext.getAuthenticatedPrincipal();
    }

    /**
     * Returns the actual context id.
     */
    private String getActualContextId() {
        return ContextHolder.get().getCurrentContextId();
    }

}
