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

import java.util.List;

import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.SanityCheckException;
import org.openengsb.core.ekb.api.SanityCheckReport;
import org.openengsb.core.ekb.api.hooks.EKBPostCommitHook;
import org.openengsb.core.ekb.api.hooks.EKBPreCommitHook;
import org.openengsb.core.ekb.common.ConvertedCommit;
import org.openengsb.core.ekb.common.EDBConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public PersistInterfaceService(EngineeringDatabaseService edbService, EDBConverter edbConverter,
            List<EKBPreCommitHook> preCommitHooks, List<EKBPostCommitHook> postCommitHooks) {
        this.edbService = edbService;
        this.edbConverter = edbConverter;
        this.preCommitHooks = preCommitHooks;
        this.postCommitHooks = postCommitHooks;
    }

    @Override
    public void commit(EKBCommit commit) throws SanityCheckException, EKBException {
        LOGGER.debug("Commit of models was called");
        runPersistingLogic(commit, true, true);
        LOGGER.debug("Commit of models was successful");
    }

    @Override
    public void forceCommit(EKBCommit commit) throws EKBException {
        LOGGER.debug("Force commit of models was called");
        runPersistingLogic(commit, false, true);
        LOGGER.debug("Force commit of models was successful");
    }

    @Override
    public SanityCheckReport check(EKBCommit commit) throws SanityCheckException, EKBException {
        LOGGER.debug("Sanity checks of models was called");
        SanityCheckReport report = performSanityChecks(commit);
        LOGGER.debug("Sanity checks of models passed successful");
        return report;
    }

    /**
     * Runs the logic of the PersistInterface. Does the sanity checks if check is set to true and does the persisting of
     * models if persist is set to true.
     */
    private void runPersistingLogic(EKBCommit commit, boolean check, boolean persist)
        throws SanityCheckException, EKBException {
        for (EKBPreCommitHook hook : preCommitHooks) {
            try {
                hook.onPreCommit(commit);
            } catch (EKBException e) {
                throw new EKBException("EDBException is thrown in a pre commit hook.", e);
            } catch (Exception e) {
                LOGGER.warn("An exception is thrown in a EKB pre commit hook.", e);
            }
        }
        if (check) {
            performSanityChecks(commit);
        }
        if (persist) {
            ConvertedCommit converted = edbConverter.convertEKBCommit(commit);
            performPersisting(converted, commit);
        }
        for (EKBPostCommitHook hook : postCommitHooks) {
            try {
                hook.onPostCommit(commit);
            } catch (Exception e) {
                LOGGER.warn("An exception is thrown in a EKB post commit hook.", e);
            }
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
            edbService.commit(ci);
            source.setRevisionNumber(ci.getRevisionNumber());
            source.setParentRevisionNumber(ci.getParentRevisionNumber());
        } catch (EDBException e) {
            throw new EKBException("Error while commiting EKBCommit", e);
        }
    }
}
