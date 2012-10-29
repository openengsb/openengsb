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
import java.util.List;

import javax.persistence.EntityManager;

import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.edb.api.hooks.EDBBeginCommitHook;
import org.openengsb.core.edb.api.hooks.EDBErrorHook;
import org.openengsb.core.edb.api.hooks.EDBPostCommitHook;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.util.EDBUtils;
import org.osgi.service.blueprint.container.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractEDBService is used to encapsulate the commit logic of the EDB.
 */
public abstract class AbstractEDBService implements EngineeringDatabaseService {
    protected EntityManager entityManager;
    private Logger logger;
    private Boolean revisionCheckEnabled;
    private List<EDBErrorHook> errorHooks;
    private List<EDBPostCommitHook> postCommitHooks;
    private List<EDBPreCommitHook> preCommitHooks;
    private List<EDBBeginCommitHook> beginCommitHooks;

    public AbstractEDBService(List<EDBBeginCommitHook> beginCommitHooks, List<EDBPreCommitHook> preCommitHooks,
            List<EDBPostCommitHook> postCommitHooks, List<EDBErrorHook> errorHooks, Boolean revisionCheckEnabled,
            Class<?> implementingClass) {
        this.beginCommitHooks = beginCommitHooks != null ? beginCommitHooks : new ArrayList<EDBBeginCommitHook>();
        this.preCommitHooks = preCommitHooks != null ? preCommitHooks : new ArrayList<EDBPreCommitHook>();
        this.postCommitHooks = postCommitHooks != null ? postCommitHooks : new ArrayList<EDBPostCommitHook>();
        this.errorHooks = errorHooks != null ? errorHooks : new ArrayList<EDBErrorHook>();
        this.revisionCheckEnabled = revisionCheckEnabled;
        logger = LoggerFactory.getLogger(implementingClass);
    }

    /**
     * Performs the actual commit logic for the EDB, including the hooks and the revision checking.
     */
    protected Long performCommitLogic(EDBCommit commit) throws EDBException {
        if (commit.isCommitted()) {
            throw new EDBException("EDBCommit is already commitet.");
        }
        if (revisionCheckEnabled && commit.getParentRevisionNumber() != null
                && !commit.getParentRevisionNumber().equals(getCurrentRevisionNumber())) {
            throw new EDBException("EDBCommit do not have the correct head revision number.");
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
            try {
                beginTransaction();
                persistCommitChanges(commit, timestamp);
                commitTransaction();
            } catch (Exception ex) {
                try {
                    rollbackTransaction();
                } catch (Exception e) {
                    throw new EDBException("Failed to rollback transaction to EDB", e);
                }
                throw new EDBException("Failed to commit transaction to EDB", ex);
            }
            return timestamp;
        }
    }

    /**
     * Add all the changes which are done through the given commit object to the entity manager.
     */
    private void persistCommitChanges(EDBCommit commit, Long timestamp) {
        commit.setTimestamp(timestamp);
        addModifiedObjectsToEntityManager(commit.getObjects(), timestamp);
        commit.setCommitted(true);
        logger.debug("persisting JPACommit");
        entityManager.persist(commit);
        logger.debug("mark the deleted elements as deleted");
        updateDeletedObjectsThroughEntityManager(commit.getDeletions(), timestamp);
    }

    /**
     * Updates all modified EDBObjects with the timestamp and persist them through the entity manager.
     */
    private void addModifiedObjectsToEntityManager(List<EDBObject> modified, Long timestamp) {
        for (EDBObject update : modified) {
            update.updateTimestamp(timestamp);
            entityManager.persist(EDBUtils.convertEDBObjectToJPAObject(update));
        }
    }

    /**
     * Updates all deleted objects with the timestamp, mark them as deleted and persist them through the entity manager.
     */
    private void updateDeletedObjectsThroughEntityManager(List<String> oids, Long timestamp) {
        for (String id : oids) {
            EDBObject o = new EDBObject(id);
            o.updateTimestamp(timestamp);
            o.setDeleted(true);
            JPAObject j = EDBUtils.convertEDBObjectToJPAObject(o);
            entityManager.persist(j);
        }
    }

    /**
     * Runs all registered begin commit hooks on the EDBCommit object. Logs exceptions which occurs in the hooks, except
     * for ServiceUnavailableExceptions and EDBExceptions. If an EDBException occurs, it is thrown and so returned to
     * the calling instance.
     */
    private void runBeginCommitHooks(EDBCommit commit) throws EDBException {
        for (EDBBeginCommitHook hook : beginCommitHooks) {
            try {
                hook.onStartCommit(commit);
            } catch (ServiceUnavailableException e) {
                // Ignore
            } catch (EDBException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Error while performing EDBBeginCommitHook", e);
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
        for (EDBPreCommitHook hook : preCommitHooks) {
            try {
                hook.onPreCommit(commit);
            } catch (ServiceUnavailableException e) {
                // Ignore
            } catch (EDBException e) {
                exception = e;
                break;
            } catch (Exception e) {
                logger.error("Error while performing EDBPreCommitHook", e);
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
                logger.error("Error while performing EDBErrorHook", e);
            }
        }
        throw exception;
    }

    /**
     * Runs all registered post commit hooks on the EDBCommit object. Logs exceptions which occurs in the hooks, except
     * for ServiceUnavailableExceptions.
     */
    private void runEDBPostHooks(EDBCommit commit) {
        for (EDBPostCommitHook hook : postCommitHooks) {
            try {
                hook.onPostCommit(commit);
            } catch (ServiceUnavailableException e) {
                // Ignore
            } catch (Exception e) {
                logger.error("Error while performing EDBPostCommitHook", e);
            }
        }
    }

    protected abstract void beginTransaction();

    protected abstract void commitTransaction();

    protected abstract void rollbackTransaction();
    
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public Logger getLogger() {
        return logger;
    }
}
