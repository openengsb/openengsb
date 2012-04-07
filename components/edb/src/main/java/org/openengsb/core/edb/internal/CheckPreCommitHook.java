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

package org.openengsb.core.edb.internal;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBConstants;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.internal.dao.DefaultJPADao;
import org.openengsb.core.edb.internal.dao.JPADao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckPreCommitHook implements EDBPreCommitHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckPreCommitHook.class);
    private JPADao dao;
    
    public CheckPreCommitHook() {
        
    }
    
    public CheckPreCommitHook(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    @Override
    public void onPreCommit(EDBCommit commit) throws EDBException {
        if (commit.getInserts() != null) {
            checkInserts(commit.getInserts());
        }
        if (commit.getDeletions() != null) {
            checkDeletions(commit.getDeletions());
        }
        if (commit.getUpdates() != null) {
            checkUpdates(commit.getUpdates());
        }
    }
    
    /**
     * Checks if all oid's of the given EDBObjects are not existing yet. If they do, an EDBException is thrown.
     */
    private void checkInserts(List<EDBObject> inserts) throws EDBException {
        for (EDBObject insert : inserts) {
            String oid = insert.getOID();
            if (checkIfActiveOidExisting(oid)) {
                throw new EDBException("The object under the oid " + oid + " is already existing");
            } else {
                insert.put(EDBConstants.MODEL_VERSION, 1);
            }
        }
    }

    /**
     * Checks if all oid's of the given EDBObjects are existing. If they don't exist, an EDBException is thrown.
     */
    private void checkDeletions(List<String> deletes) throws EDBException {
        for (String delete : deletes) {
            if (!checkIfActiveOidExisting(delete)) {
                throw new EDBException("The object under the oid " + delete + " is not existing or is already deleted");
            }
        }
    }

    /**
     * Checks every update for a potential conflict. If a conflict is found, an EDBException is thrown.
     */
    private void checkUpdates(List<EDBObject> updates) throws EDBException {
        for (EDBObject update : updates) {
            Integer modelVersion = investigateVersionAndCheckForConflict(update);
            modelVersion++;
            update.put(EDBConstants.MODEL_VERSION, modelVersion);
        }
    }

    /**
     * Investigates the version of an EDBObject and checks if a conflict can be found.
     */
    private Integer investigateVersionAndCheckForConflict(EDBObject newObject) throws EDBException {
        Integer modelVersion = (Integer) newObject.get(EDBConstants.MODEL_VERSION);
        String oid = newObject.getOID();

        if (modelVersion != null) {
            Integer currentVersion = getVersionOfOid(oid);
            if (!modelVersion.equals(currentVersion)) {
                try {
                    checkForConflict(newObject);
                } catch (EDBException e) {
                    LOGGER.info("conflict detected, user get informed");
                    throw new EDBException("conflict was detected. There is a newer version of the model with the oid "
                            + oid + " saved.");
                }
                modelVersion = currentVersion;
            }
        } else {
            modelVersion = getVersionOfOid(oid);
        }

        return modelVersion;
    }

    /**
     * Simple check mechanism if there is a conflict between a model which should be saved and the existing model, based
     * on the values which are in the EDB.
     */
    private void checkForConflict(EDBObject newObject) throws EDBException {
        String oid = newObject.getOID();
        EDBObject object = getObject(oid);
        for (Map.Entry<String, Object> entry : newObject.entrySet()) {
            if (entry.getKey().equals(EDBConstants.MODEL_VERSION)) {
                continue;
            }
            Object value = object.get(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                LOGGER.debug("Conflict detected at key %s when comparing %s with %s", new Object[]{ entry.getKey(),
                    entry.getValue(), value == null ? "null" : value.toString() });
                throw new EDBException("Conflict detected. Failure when comparing the values of the key "
                        + entry.getKey());
            }
        }
    }
    
    /**
     * Returns true if the given oid is active right now (means is existing and not deleted) and return false otherwise.
     */
    private boolean checkIfActiveOidExisting(String oid) {
        try {
            EDBObject obj = getObject(oid);
            if (!obj.isDeleted()) {
                return true;
            }
        } catch (EDBException e) {
            // nothing to do here
        }
        return false;
    }
    
    private EDBObject getObject(String oid) throws EDBException {
        JPAObject temp = dao.getJPAObject(oid);
        return temp.getObject();
    }
    
    /**
     * Loads the actual version of a model with the given oid.
     */
    private Integer getVersionOfOid(String oid) throws EDBException {
        return dao.getVersionOfOid(oid);
    }
    
    public void setEntityManager(EntityManager entityManager) {
        dao = new DefaultJPADao(entityManager);
    }

}
