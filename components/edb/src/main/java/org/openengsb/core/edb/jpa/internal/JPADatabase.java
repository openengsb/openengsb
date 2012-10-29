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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.hooks.EDBBeginCommitHook;
import org.openengsb.core.edb.api.hooks.EDBErrorHook;
import org.openengsb.core.edb.api.hooks.EDBPostCommitHook;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.dao.JPADao;
import org.openengsb.core.edb.jpa.internal.util.EDBUtils;

public class JPADatabase extends AbstractEDBService {
    private JPADao dao;
    private AuthenticationContext authenticationContext;
    
    public JPADatabase(JPADao dao, AuthenticationContext authenticationContext,
            List<EDBBeginCommitHook> beginCommitHooks, List<EDBPreCommitHook> preCommitHooks,
            List<EDBPostCommitHook> postCommitHooks, List<EDBErrorHook> errorHooks,
            Boolean revisionCheckEnabled) {
        super(beginCommitHooks, preCommitHooks, postCommitHooks, errorHooks, revisionCheckEnabled, JPADatabase.class);
        this.dao = dao;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public Long commit(EDBCommit commit) throws EDBException {
        return performCommitLogic(commit);
    }    

    /**
     * Only here for the TestEDBService where there is a real implementation for this method.
     */
    protected void beginTransaction() {
    }

    /**
     * Only here for the TestEDBService where there is a real implementation for this method.
     */
    protected void commitTransaction() {
    }

    /**
     * Only here for the TestEDBService where there is a real implementation for this method.
     */
    protected void rollbackTransaction() {
    }

    @Override
    public EDBObject getObject(String oid) throws EDBException {
        getLogger().debug("loading newest JPAObject with the oid {}", oid);
        JPAObject temp = dao.getJPAObject(oid);
        return EDBUtils.convertJPAObjectToEDBObject(temp);
    }

    @Override
    public List<EDBObject> getObjects(List<String> oids) throws EDBException {
        List<JPAObject> objects = dao.getJPAObjects(oids);
        return EDBUtils.convertJPAObjectsToEDBObjects(objects);
    }

    @Override
    public List<EDBObject> getHistory(String oid) throws EDBException {
        getLogger().debug("loading history of JPAObject with the oid {}", oid);
        List<JPAObject> objects = dao.getJPAObjectHistory(oid);
        return EDBUtils.convertJPAObjectsToEDBObjects(objects);
    }

    @Override
    public List<EDBObject> getHistoryForTimeRange(String oid, Long from, Long to) throws EDBException {
        getLogger().debug("loading JPAObject with the oid {} from "
                + "the timestamp {} to the timestamp {}", new Object[]{ oid, from, to });
        List<JPAObject> objects = dao.getJPAObjectHistory(oid, from, to);
        return EDBUtils.convertJPAObjectsToEDBObjects(objects);
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to) throws EDBException {
        getLogger().debug("loading the log of JPAObject with the oid {} from "
                + "the timestamp {} to the timestamp {}", new Object[]{ oid, from, to });
        List<EDBObject> history = getHistoryForTimeRange(oid, from, to);
        List<JPACommit> commits = dao.getJPACommit(oid, from, to);
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
        return dao.getJPAHead(System.currentTimeMillis()).getEDBObjects();
    }

    @Override
    public List<EDBObject> getHead(long timestamp) throws EDBException {
        getLogger().debug("load the elements of the JPAHead with the timestamp {}", timestamp);
        JPAHead head = dao.getJPAHead(timestamp);
        if (head != null) {
            return head.getEDBObjects();
        }
        throw new EDBException("Failed to get head for timestamp " + Long.toString(timestamp));
    }

    @Override
    public List<EDBObject> queryByKeyValue(String key, Object value) throws EDBException {
        getLogger().debug("query for objects with key = {} and value = {}", key, value);
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return queryByMap(queryMap);
    }

    @Override
    public List<EDBObject> queryByMap(Map<String, Object> queryMap) throws EDBException {
        try {
            return EDBUtils.convertJPAObjectsToEDBObjects(dao.query(queryMap));
        } catch (Exception ex) {
            throw new EDBException("failed to query for objects with the given map", ex);
        }
    }

    @Override
    public List<EDBObject> query(Map<String, Object> queryMap, Long timestamp) throws EDBException {
        try {
            return EDBUtils.convertJPAObjectsToEDBObjects(dao.query(queryMap, timestamp));
        } catch (Exception ex) {
            throw new EDBException("failed to query for objects with the given map", ex);
        }
    }

    @Override
    public List<EDBCommit> getCommitsByKeyValue(String key, Object value) throws EDBException {
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return getCommits(queryMap);
    }

    @Override
    public List<EDBCommit> getCommits(Map<String, Object> queryMap) throws EDBException {
        List<JPACommit> commits = dao.getCommits(queryMap);
        return new ArrayList<EDBCommit>(commits);
    }

    @Override
    public JPACommit getLastCommitByKeyValue(String key, Object value) throws EDBException {
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return getLastCommit(queryMap);
    }

    @Override
    public JPACommit getLastCommit(Map<String, Object> queryMap) throws EDBException {
        JPACommit result = dao.getLastCommit(queryMap);
        return result;
    }

    @Override
    public UUID getCurrentRevisionNumber() throws EDBException {
        try {
            return getCommit(System.currentTimeMillis()).getRevisionNumber();
        } catch (EDBException e) {
            getLogger().debug("There was no commit so far, so the current revision number is null");
            return null;
        }
    }

    @Override
    public JPACommit getCommit(Long from) throws EDBException {
        List<JPACommit> commits = dao.getJPACommit(from);
        if (commits == null || commits.size() == 0) {
            throw new EDBException("there is no commit for this timestamp");
        } else if (commits.size() > 1) {
            throw new EDBException("there are more than one commit for one timestamp");
        }
        return commits.get(0);
    }

    @Override
    public Diff getDiff(Long firstTimestamp, Long secondTimestamp) throws EDBException {
        List<EDBObject> headA = getHead(firstTimestamp);
        List<EDBObject> headB = getHead(secondTimestamp);

        return new Diff(getCommit(firstTimestamp), getCommit(secondTimestamp), headA, headB);
    }

    @Override
    public List<String> getResurrectedOIDs() throws EDBException {
        return dao.getResurrectedOIDs();
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatching(
            Map<String, Object> queryMap) throws EDBException {
        JPACommit ci = getLastCommit(queryMap);
        return getHead(ci.getTimestamp());
    }

    @Override
    public List<EDBObject> getStateOfLastCommitMatchingByKeyValue(String key, Object value) throws EDBException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(key, value);
        return getStateOfLastCommitMatching(query);
    }

    @Override
    public EDBCommit createEDBCommit(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes)
        throws EDBException {
        String committer = getAuthenticatedUser();
        String contextId = getActualContextId();
        JPACommit commit = new JPACommit(committer, contextId);
        getLogger().debug("creating commit for committer {} with contextId {}", committer, contextId);
        commit.insertAll(inserts);
        commit.updateAll(updates);
        commit.deleteAll(deletes);
        commit.setHeadRevisionNumber(getCurrentRevisionNumber());
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
