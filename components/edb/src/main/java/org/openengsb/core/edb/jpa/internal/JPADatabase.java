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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.edb.api.hooks.EDBBeginCommitHook;
import org.openengsb.core.edb.api.hooks.EDBErrorHook;
import org.openengsb.core.edb.api.hooks.EDBPostCommitHook;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.dao.DefaultJPADao;
import org.openengsb.core.edb.jpa.internal.dao.JPADao;
import org.osgi.service.blueprint.container.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPADatabase implements EngineeringDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JPADatabase.class);
    private EntityTransaction utx;
    private EntityManager entityManager;
    private JPADao dao;
    private AuthenticationContext authenticationContext;

    private List<EDBErrorHook> errorHooks;
    private List<EDBPostCommitHook> postCommitHooks;
    private List<EDBPreCommitHook> preCommitHooks;
    private List<EDBBeginCommitHook> beginCommitHooks;

    /**
     * this is just for testing the JPADatabase. Should only be called in the corresponding test class.
     */
    public void open(EntityManager entityManager) throws EDBException {
        LOGGER.debug("starting to open EDB for testing via JPA");
        setEntityManager(entityManager);
        utx = entityManager.getTransaction();
        LOGGER.debug("starting of EDB successful");
    }

    /**
     * this is just for testing the JPADatabase. Should only be called in the corresponding test class.
     */
    public void close() {
        entityManager.close();
        utx = null;
        entityManager = null;
    }

    @Override
    public JPACommit createCommit(String committer, String contextId) {
        LOGGER.debug("creating commit for committer {} with contextId {}", committer, contextId);
        return new JPACommit(committer, contextId);
    }

    @Override
    public Long commit(EDBCommit commit) throws EDBException {
        if (commit.isCommitted()) {
            throw new EDBException("EDBCommit was already commitet!");
        }
        runBeginCommitHooks(commit);
        EDBException exception = runPreCommitHooks(commit);
        if (exception != null) {
            return runErrorHooks(commit, exception);
        }
        Long timestamp = performCommit(commit);
        runEDBPostHooks(commit);

        return timestamp;
    }

    /**
     * Does the actual commit work (JPA related actions) and returns the timestamp when the commit was done. Throws an
     * EDBException if an error occurs.
     */
    private Long performCommit(EDBCommit commit) throws EDBException {
        synchronized (entityManager) {
            long timestamp = System.currentTimeMillis();
            commit.setTimestamp(timestamp);
            for (EDBObject update : commit.getObjects()) {
                update.updateTimestamp(timestamp);
                entityManager.persist(EDBUtils.convertEDBObjectToJPAObject(update));
            }

            try {
                if (utx != null) {
                    utx.begin();
                }
                commit.setCommitted(true);
                LOGGER.debug("persisting JPACommit");
                entityManager.persist(commit);

                LOGGER.debug("setting the deleted elements as deleted");
                for (String id : commit.getDeletions()) {
                    EDBObject o = new EDBObject(id);
                    o.updateTimestamp(timestamp);
                    o.putEDBObjectEntry("isDeleted", new Boolean(true), Boolean.class.getName());
                    JPAObject j = EDBUtils.convertEDBObjectToJPAObject(o);
                    entityManager.persist(j);
                }

                if (utx != null) {
                    utx.commit();
                }
            } catch (Exception ex) {
                try {
                    if (utx != null) {
                        utx.rollback();
                    }
                } catch (Exception e) {
                    throw new EDBException("Failed to rollback transaction to DB", e);
                }
                throw new EDBException("Failed to commit transaction to DB", ex);
            }
            return timestamp;
        }
    }

    /**
     * Runs all registered begin commit hooks on the EDBCommit object. Logs exceptions which occurs in the hooks, except
     * for ServiceUnavailableExceptions and EDBExceptions. If an EDBException occurs, it is thrown and so returned to
     * the calling instance.
     */
    private void runBeginCommitHooks(EDBCommit commit) throws EDBException {
        if (beginCommitHooks != null) {
            for (EDBBeginCommitHook hook : beginCommitHooks) {
                try {
                    hook.onStartCommit(commit);
                } catch (ServiceUnavailableException e) {
                    // Ignore
                } catch (EDBException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.error("Error while performing EDBBeginCommitHook", e);
                }
            }
        }
    }

    /**
     * Runs all registered pre commit hooks on the EDBCommit object. Logs exceptions which occurs in the hooks, except
     * for ServiceUnavailableExceptions and EDBExceptions. If an EDBException occurs, the function returns this
     * exception.
     */
    private EDBException runPreCommitHooks(EDBCommit commit) {
        EDBException exception = null;
        if (preCommitHooks != null) {
            for (EDBPreCommitHook hook : preCommitHooks) {
                try {
                    hook.onPreCommit(commit);
                } catch (ServiceUnavailableException e) {
                    // Ignore
                } catch (EDBException e) {
                    exception = e;
                    break;
                } catch (Exception e) {
                    LOGGER.error("Error while performing EDBPreCommitHook", e);
                }
            }
        }
        return exception;
    }

    /**
     * Runs all registered error hooks on the EDBCommit object. Logs exceptions which occurs in the hooks, except for
     * ServiceUnavailableExceptions and EDBExceptions. If an EDBException occurs, the function overrides the cause of
     * the error with the new Exception. If an error hook returns a new EDBCommit, the EDB tries to persist this commit
     * instead.
     */
    private Long runErrorHooks(EDBCommit commit, EDBException exception) throws EDBException {
        if (errorHooks != null) {
            for (EDBErrorHook hook : errorHooks) {
                try {
                    EDBCommit newCommit = hook.onError(commit, exception);
                    if (newCommit != null) {
                        return commit(newCommit);
                    }
                } catch (ServiceUnavailableException e) {
                    // Ignore
                } catch (EDBException e) {
                    exception = e;
                    break;
                } catch (Exception e) {
                    LOGGER.error("Error while performing EDBErrorHook", e);
                }
            }
        }
        throw exception;
    }

    /**
     * Runs all registered post commit hooks on the EDBCommit object. Logs exceptions which occurs in the hooks, except
     * for ServiceUnavailableExceptions.
     */
    private void runEDBPostHooks(EDBCommit commit) {
        if (postCommitHooks != null) {
            for (EDBPostCommitHook hook : postCommitHooks) {
                try {
                    hook.onPostCommit(commit);
                } catch (ServiceUnavailableException e) {
                    // Ignore
                } catch (Exception e) {
                    LOGGER.error("Error while performing EDBPostCommitHook", e);
                }
            }
        }
    }

    @Override
    public EDBObject getObject(String oid) throws EDBException {
        LOGGER.debug("loading newest JPAObject with the oid {}", oid);
        JPAObject temp = dao.getJPAObject(oid);
        return EDBUtils.convertJPAObjectToEDBObject(temp);
    }

    @Override
    public List<EDBObject> getObjects(List<String> oids) throws EDBException {
        List<JPAObject> objects = dao.getJPAObjects(oids);
        List<EDBObject> result = new ArrayList<EDBObject>();
        for (JPAObject object : objects) {
            result.add(EDBUtils.convertJPAObjectToEDBObject(object));
        }
        return result;
    }

    @Override
    public List<EDBObject> getHistory(String oid) throws EDBException {
        LOGGER.debug("loading history of JPAObject with the oid {}", oid);
        List<JPAObject> jpa = dao.getJPAObjectHistory(oid);
        return generateEDBObjectList(jpa);
    }

    @Override
    public List<EDBObject> getHistory(String oid, Long from, Long to) throws EDBException {
        LOGGER.debug("loading JPAObject with the oid {} from "
                + "the timestamp {} to the timestamp {}", new Object[]{ oid, from, to });
        List<JPAObject> jpa = dao.getJPAObjectHistory(oid, from, to);
        return generateEDBObjectList(jpa);
    }

    /**
     * transforms a list of JPAObjects to a List of EDBObjects
     */
    private List<EDBObject> generateEDBObjectList(List<JPAObject> jpaObjects) {
        List<EDBObject> result = new ArrayList<EDBObject>();
        for (JPAObject object : jpaObjects) {
            result.add(EDBUtils.convertJPAObjectToEDBObject(object));
        }
        return result;
    }

    @Override
    public List<EDBLogEntry> getLog(String oid, Long from, Long to) throws EDBException {
        LOGGER.debug("loading the log of JPAObject with the oid {} from "
                + "the timestamp {} to the timestamp {}", new Object[]{ oid, from, to });
        List<EDBObject> history = getHistory(oid, from, to);
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

    /**
     * loads the JPAHead with the given timestamp
     */
    private JPAHead loadHead(long timestamp) throws EDBException {
        LOGGER.debug("load the JPAHead with the timestamp {}", timestamp);
        return dao.getJPAHead(timestamp);
    }

    @Override
    public List<EDBObject> getHead() throws EDBException {
        return dao.getJPAHead(System.currentTimeMillis()).getEDBObjects();
    }

    @Override
    public List<EDBObject> getHead(long timestamp) throws EDBException {
        LOGGER.debug("load the elements of the JPAHead with the timestamp {}", timestamp);
        JPAHead head = loadHead(timestamp);
        if (head != null) {
            return head.getEDBObjects();
        }
        throw new EDBException("Failed to get head for timestamp " + Long.toString(timestamp));
    }

    @Override
    public List<EDBObject> query(String key, Object value) throws EDBException {
        LOGGER.debug("query for objects with key = {} and value = {}", key, value);
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(key, value);
        return query(queryMap);
    }

    @Override
    public List<EDBObject> query(Map<String, Object> queryMap) throws EDBException {
        try {
            return generateEDBObjectList(new ArrayList<JPAObject>(dao.query(queryMap)));
        } catch (Exception ex) {
            throw new EDBException("failed to query for objects with the given map", ex);
        }
    }

    @Override
    public List<EDBObject> query(Map<String, Object> queryMap, Long timestamp) throws EDBException {
        try {
            return generateEDBObjectList(new ArrayList<JPAObject>(dao.query(queryMap, timestamp)));
        } catch (Exception ex) {
            throw new EDBException("failed to query for objects with the given map", ex);
        }
    }

    @Override
    public List<EDBCommit> getCommits(String key, Object value) throws EDBException {
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
    public JPACommit getLastCommit(String key, Object value) throws EDBException {
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
    public List<EDBObject> getStateOfLastCommitMatching(String key, Object value) throws EDBException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put(key, value);
        return getStateOfLastCommitMatching(query);
    }

    /**
     * Returns the actual authenticated user. If this class is called from JUnit, the string "testuser" is returned.
     */
    private String getAuthenticatedUser() {
        // if JPADatabase is called via integration tests
        String username = (String) authenticationContext.getAuthenticatedPrincipal();
        if (username == null) {
            return "testuser";
        }
        return username;
    }

    /**
     * Returns the actual context id. If this class is called from JUnit, the string "testcontext" is returned.
     */
    private String getActualContextId() {
        // if JPADatabase is called via integration tests
        if (ContextHolder.get() == null) {
            return "testcontext";
        }
        return ContextHolder.get().getCurrentContextId();
    }

    @Override
    public void commitEDBObjects(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes)
        throws EDBException {
        JPACommit commit = createCommit(getAuthenticatedUser(), getActualContextId());

        if (inserts != null) {
            for (EDBObject insert : inserts) {
                commit.insert(insert);
            }
        }
        if (updates != null) {
            for (EDBObject update : updates) {
                commit.update(update);
            }
        }
        if (deletes != null) {
            for (EDBObject delete : deletes) {
                commit.delete(delete.getOID());
            }
        }
        commit(commit);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        dao = new DefaultJPADao(entityManager);
    }

    public void setErrorHooks(List<EDBErrorHook> errorHooks) {
        this.errorHooks = errorHooks;
    }

    public void setPostCommitHooks(List<EDBPostCommitHook> postCommitHooks) {
        this.postCommitHooks = postCommitHooks;
    }

    public void setPreCommitHooks(List<EDBPreCommitHook> preCommitHooks) {
        this.preCommitHooks = preCommitHooks;
    }

    public void setBeginCommitHooks(List<EDBBeginCommitHook> beginCommitHooks) {
        this.beginCommitHooks = beginCommitHooks;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

}
