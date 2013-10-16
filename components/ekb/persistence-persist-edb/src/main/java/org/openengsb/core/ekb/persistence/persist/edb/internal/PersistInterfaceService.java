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

package org.openengsb.core.ekb.persistence.persist.edb.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.edb.api.EDBCheckException;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBConcurrentException;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.ModelPersistException;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.SanityCheckException;
import org.openengsb.core.ekb.api.SanityCheckReport;
import org.openengsb.core.ekb.api.hooks.EKBErrorHook;
import org.openengsb.core.ekb.api.hooks.EKBPostCommitHook;
import org.openengsb.core.ekb.api.hooks.EKBPreCommitHook;
import org.openengsb.core.ekb.common.ConvertedCommit;
import org.openengsb.core.ekb.common.EDBConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * Implementation of the PersistInterface service. It's main responsibilities are the saving of models and the sanity
 * checks of these.
 */
public class PersistInterfaceService implements PersistInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistInterfaceService.class);
    private EngineeringDatabaseService edbService;
    private EDBConverter edbConverter;
    private List<EKBPreCommitHook> preCommitHooks;
    private List<EKBPostCommitHook> postCommitHooks;
    private List<EKBErrorHook> errorHooks;
    private ContextLockingMode mode;
    private Set<String> activeWritingContexts;

    public PersistInterfaceService(EngineeringDatabaseService edbService, EDBConverter edbConverter,
            List<EKBPreCommitHook> preCommitHooks, List<EKBPostCommitHook> postCommitHooks,
            List<EKBErrorHook> errorHooks, String contextLockingMode) {
        this.edbService = edbService;
        this.edbConverter = edbConverter;
        this.preCommitHooks = preCommitHooks;
        this.postCommitHooks = postCommitHooks;
        this.errorHooks = errorHooks;
        this.activeWritingContexts = new HashSet<String>();
        try {
            this.mode = ContextLockingMode.valueOf(contextLockingMode);
        } catch (IllegalArgumentException e) {
            this.mode = ContextLockingMode.DEACTIVATED;
            LOGGER.error("Unknown mode setting. The context locking mechanism will be deactivated.", e);
        }
    }

    @Override
    public void commit(EKBCommit commit) throws SanityCheckException, EKBException {
        LOGGER.debug("Commit of models was called");
        runPersistingLogic(commit, true);
        LOGGER.debug("Commit of models was successful");
    }

    @Override
    public void forceCommit(EKBCommit commit) throws EKBException {
        LOGGER.debug("Force commit of models was called");
        runPersistingLogic(commit, false);
        LOGGER.debug("Force commit of models was successful");
    }

    @Override
    public SanityCheckReport check(EKBCommit commit) throws SanityCheckException, EKBException {
        LOGGER.debug("Sanity checks of models was called");
        SanityCheckReport report = performSanityChecks(commit);
        LOGGER.debug("Sanity checks of models passed successful");
        return report;
    }

    @Override
    public void revertCommit(String revision, UUID expectedContextParentRevision) throws EKBException {
        String contextId = "";
        try {
            EDBCommit commit = edbService.getCommitByRevision(revision);
            contextId = commit.getContextId();
            lockContext(contextId, expectedContextParentRevision);
            EDBCommit newCommit = edbService.createEDBCommit(new ArrayList<EDBObject>(),
                new ArrayList<EDBObject>(), new ArrayList<EDBObject>());
            for (EDBObject reverted : commit.getObjects()) {
                // need to be done in order to avoid problems with conflict detection
                reverted.remove(EDBConstants.MODEL_VERSION);
                newCommit.update(reverted);
            }
            for (String delete : commit.getDeletions()) {
                newCommit.delete(delete);
            }
            newCommit.setComment(String.format("revert [%s] %s", commit.getRevisionNumber().toString(),
                commit.getComment() != null ? commit.getComment() : ""));
            edbService.commit(newCommit);
        } catch (EDBException e) {
            throw new EKBException("Unable to revert to the given revision " + revision, e);
        } finally {
            releaseContext(contextId);
        }
    }

    /**
     * Runs the logic of the PersistInterface. Does the sanity checks if check is set to true and does the persisting of
     * models if persist is set to true.
     */
    private void runPersistingLogic(EKBCommit commit, boolean check)
        throws SanityCheckException, EKBException {
        String contextId = ContextHolder.get().getCurrentContextId();
        try {
            lockContext(contextId, commit.getParentRevisionNumber());
            runEKBPreCommitHooks(commit);
            if (check) {
                performSanityChecks(commit);
            }
            EKBException exception = null;
            ConvertedCommit converted = edbConverter.convertEKBCommit(commit);
            try {
                performPersisting(converted, commit);
                runEKBPostCommitHooks(commit);
            } catch (EKBException e) {
                exception = e;
            }
            runEKBErrorHooks(commit, exception);
        } finally {
            releaseContext(contextId);
        }
    }

    /**
     * If the context locking mode is activated, this method locks the given context for writing operations. If this
     * context is already locked, an EKBConcurrentException is thrown.
     */
    private void lockContext(String contextId, UUID expectedParentRevision) {
        if (mode == ContextLockingMode.DEACTIVATED) {
            return;
        }
        synchronized (activeWritingContexts) {
            if (mode.isConcurrentWriteProtectionActivated() && activeWritingContexts.contains(contextId)) {
                throw new EKBConcurrentException("There is already a writing process active in the context.");
            }
            if (mode.isExpectedHeadRevisionCheckActivated() 
                    && !Objects.equal(edbService.getLastRevisionNumberOfContext(contextId), expectedParentRevision)) {
                throw new EKBConcurrentException("The current revision of the context does not match the "
                        + "expected one.");
            }
            activeWritingContexts.add(contextId);
        }
    }

    /**
     * If the context locking mode is activated, this method releases the lock for the given context for writing
     * operations.
     */
    private void releaseContext(String contextId) {
        if (mode == ContextLockingMode.DEACTIVATED) {
            return;
        }
        synchronized (activeWritingContexts) {
            activeWritingContexts.remove(contextId);
        }
    }

    /**
     * Runs all registered pre-commit hooks
     */
    private void runEKBPreCommitHooks(EKBCommit commit) throws EKBException {
        for (EKBPreCommitHook hook : preCommitHooks) {
            try {
                hook.onPreCommit(commit);
            } catch (EKBException e) {
                throw new EKBException("EDBException is thrown in a pre commit hook.", e);
            } catch (Exception e) {
                LOGGER.warn("An exception is thrown in a EKB pre commit hook.", e);
            }
        }
    }

    /**
     * Runs all registered post-commit hooks
     */
    private void runEKBPostCommitHooks(EKBCommit commit) throws EKBException {
        for (EKBPostCommitHook hook : postCommitHooks) {
            try {
                hook.onPostCommit(commit);
            } catch (Exception e) {
                LOGGER.warn("An exception is thrown in a EKB post commit hook.", e);
            }
        }
    }

    /**
     * Runs all registered error hooks
     */
    private void runEKBErrorHooks(EKBCommit commit, EKBException exception) {
        if (exception != null) {
            for (EKBErrorHook hook : errorHooks) {
                hook.onError(commit, exception);
            }
            throw exception;
        }
    }

    /**
     * Performs the sanity checks of the given models.
     */
    private SanityCheckReport performSanityChecks(EKBCommit commit) throws SanityCheckException {
        // TODO: [OPENENGSB-2717] implement sanity check logic
        return null;
    }

    /**
     * Performs the persisting of the models into the EDB.
     */
    private void performPersisting(ConvertedCommit commit, EKBCommit source) throws EKBException {
        try {
            EDBCommit ci = edbService.createEDBCommit(commit.getInserts(), commit.getUpdates(), commit.getDeletes());
            ci.setDomainId(source.getDomainId());
            ci.setConnectorId(source.getConnectorId());
            ci.setInstanceId(source.getInstanceId());
            ci.setComment(source.getComment());
            edbService.commit(ci);
            source.setRevisionNumber(ci.getRevisionNumber());
            source.setParentRevisionNumber(ci.getParentRevisionNumber());
        } catch (EDBCheckException e) {
            throw new ModelPersistException(convertEDBObjectList(e.getFailedInserts()),
                convertEDBObjectList(e.getFailedUpdates()), e.getFailedDeletes(), e);
        } catch (EDBException e) {
            throw new EKBException("Error while commiting EKBCommit", e);
        }
    }

    /**
     * Converts a list of EDBObject instances into a list of corresponding model oids.
     */
    private List<String> convertEDBObjectList(List<EDBObject> objects) {
        List<String> oids = new ArrayList<>();
        for (EDBObject object : objects) {
            oids.add(object.getOID());
        }
        return oids;
    }
}
