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
import java.util.Map;

import javax.persistence.EntityManager;

import org.openengsb.core.edb.api.EDBCheckException;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.dao.DefaultJPADao;
import org.openengsb.core.edb.jpa.internal.dao.JPADao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard pre-commit hook for the EDB. It does the basic checking algorithms, fills and updates the
 * model version field and checks for conflicts. If any error occurs, the onPreCommit function throws an
 * EDBCheckException.
 */
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
        List<EDBObject> insertFails = null;
        List<EDBObject> updateFails = null;
        List<String> deleteFails = null;

        if (commit.getInserts() != null) {
            insertFails = checkInserts(commit.getInserts());
        }
        if (commit.getDeletions() != null) {
            deleteFails = checkDeletions(commit.getDeletions());
        }
        if (commit.getUpdates() != null) {
            updateFails = checkUpdates(commit.getUpdates());
        }
        testIfExceptionNeeded(insertFails, updateFails, deleteFails);
    }

    /**
     * Checks all lists with failed objects if there is the need to throw an EDBCheckException. If the need is given, it
     * creates this exception and throws it.
     */
    private void testIfExceptionNeeded(List<EDBObject> insertFails, List<EDBObject> updateFails,
            List<String> deleteFails) throws EDBCheckException {
        StringBuilder builder = new StringBuilder();

        for (EDBObject insert : insertFails) {
            builder.append("Object with the oid ").append(insert.getOID()).append(" exists already. ");
        }
        for (EDBObject update : updateFails) {
            builder.append("Found a conflict for the oid ").append(update.getOID()).append(". ");
        }
        for (String delete : deleteFails) {
            builder.append("Object with the oid ").append(delete).append(" doesn't exists/is deleted. ");
        }
        if (builder.length() != 0) {
            EDBCheckException checkException = new EDBCheckException(builder.toString());
            checkException.setFailedInserts(insertFails);
            checkException.setFailedUpdates(updateFails);
            checkException.setFailedDeletes(deleteFails);
            throw checkException;
        }
    }

    /**
     * Checks if all oid's of the given EDBObjects are not existing yet. Returns a list of objects where the EDBObject
     * already exists.
     */
    private List<EDBObject> checkInserts(List<EDBObject> inserts) {
        List<EDBObject> failedObjects = new ArrayList<EDBObject>();
        for (EDBObject insert : inserts) {
            String oid = insert.getOID();
            if (checkIfActiveOidExisting(oid)) {
                failedObjects.add(insert);
            } else {
                insert.put(EDBConstants.MODEL_VERSION, 1);
            }
        }
        return failedObjects;
    }

    /**
     * Checks if all oid's of the given EDBObjects are existing. Returns a list of objects where the EDBObject doesn't
     * exist.
     */
    private List<String> checkDeletions(List<String> deletes) {
        List<String> failedObjects = new ArrayList<String>();
        for (String delete : deletes) {
            if (!checkIfActiveOidExisting(delete)) {
                failedObjects.add(delete);
            }
        }
        return failedObjects;
    }

    /**
     * Checks every update for a potential conflict. Returns a list of objects where a conflict has been found.
     */
    private List<EDBObject> checkUpdates(List<EDBObject> updates) throws EDBException {
        List<EDBObject> failedObjects = new ArrayList<EDBObject>();
        for (EDBObject update : updates) {
            try {
                Integer modelVersion = investigateVersionAndCheckForConflict(update);
                modelVersion++;
                update.put(EDBConstants.MODEL_VERSION, modelVersion);
            } catch (EDBException e) {
                failedObjects.add(update);
            }
        }
        return failedObjects;
    }

    /**
     * Investigates the version of an EDBObject and checks if a conflict can be found.
     */
    private Integer investigateVersionAndCheckForConflict(EDBObject newObject) throws EDBException {
        Object version = newObject.get(EDBConstants.MODEL_VERSION);
        Integer modelVersion;
        if (version == null) {
            modelVersion = null;
        } else if (version.getClass().equals(Integer.class)) {
            modelVersion = (Integer) version;
        } else {
            modelVersion = Integer.valueOf((String) version);
        }
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

    /**
     * Loads the EDBObject for the given oid.
     */
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
