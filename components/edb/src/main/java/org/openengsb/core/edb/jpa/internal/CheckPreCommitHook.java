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

import org.openengsb.core.edb.api.EDBCheckException;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBStage;
import org.openengsb.core.edb.api.hooks.EDBPreCommitHook;
import org.openengsb.core.edb.jpa.internal.dao.JPADao;
import org.openengsb.core.edb.jpa.internal.util.EDBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard pre-commit hook for the EDB. It does the basic
 * checking algorithms, fills and updates the model version field and checks for
 * conflicts. If any error occurs, the onPreCommit function throws an
 * EDBCheckException.
 */
public class CheckPreCommitHook implements EDBPreCommitHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckPreCommitHook.class);
    private JPADao dao;

    public CheckPreCommitHook(JPADao dao) {
        this.dao = dao;
    }

    @Override
    public void onPreCommit(EDBCommit commit) throws EDBException {
        if (!(commit instanceof JPACommit)) {
            throw new EDBException("Unsupported EDBCommit type");
        }
        JPACommit orig = (JPACommit) commit;
        List<JPAObject> insertFails = null;
        List<JPAObject> updateFails = null;
        List<String> deleteFails = null;

        if (orig.getInsertedObjects() != null) {
            insertFails = checkInserts(orig.getInsertedObjects(), orig.getEDBStage());
        }
        if (orig.getDeletions() != null) {
            deleteFails = checkDeletions(orig.getDeletions(), orig.getEDBStage());
        }
        if (orig.getUpdatedObjects() != null) {
            updateFails = checkUpdates(orig.getUpdatedObjects());
        }
        testIfExceptionNeeded(insertFails, updateFails, deleteFails);
    }

    /**
     * Checks all lists with failed objects if there is the need to throw an
     * EDBCheckException. If the need is given, it creates this exception and
     * throws it.
     */
    private void testIfExceptionNeeded(List<JPAObject> insertFails, List<JPAObject> updateFails,
            List<String> deleteFails) throws EDBCheckException {
        StringBuilder builder = new StringBuilder();

        for (JPAObject insert : insertFails) {
            builder.append("Object with the oid ").append(insert.getOID()).append(" exists already. ");
        }
        for (JPAObject update : updateFails) {
            builder.append("Found a conflict for the oid ").append(update.getOID()).append(". ");
        }
        for (String delete : deleteFails) {
            builder.append("Object with the oid ").append(delete).append(" doesn't exists/is deleted. ");
        }
        if (builder.length() != 0) {
            EDBCheckException checkException = new EDBCheckException(builder.toString());
            checkException.setFailedInserts(EDBUtils.convertJPAObjectsToEDBObjects(insertFails));
            checkException.setFailedUpdates(EDBUtils.convertJPAObjectsToEDBObjects(updateFails));
            checkException.setFailedDeletes(deleteFails);
            throw checkException;
        }
    }

    /**
     * Checks if all oid's of the given JPAObjects are not existing yet. Returns
     * a list of objects where the JPAObject already exists.
     */
    private List<JPAObject> checkInserts(List<JPAObject> inserts, EDBStage stage) {
        List<JPAObject> failedObjects = new ArrayList<JPAObject>();
        for (JPAObject insert : inserts) {
            String oid = insert.getOID();
            if (checkIfActiveOidExisting(oid, checkStage(stage))) {
                failedObjects.add(insert);
            } else {
                insert.addEntry(new JPAEntry(EDBConstants.MODEL_VERSION, "1", Integer.class.getName(), insert));
            }
        }
        return failedObjects;
    }

    private String checkStage(EDBStage stage) {
        if (stage != null) {
            return stage.getStageId();
        }
        return null;
    }

    /**
     * Checks if all oid's of the given JPAObjects are existing. Returns a list
     * of objects where the JPAObject doesn't exist.
     */
    private List<String> checkDeletions(List<String> deletes, EDBStage stage) {
        List<String> failedObjects = new ArrayList<String>();
        for (String delete : deletes) {
            if (!checkIfActiveOidExisting(delete, checkStage(stage))) {
                failedObjects.add(delete);
            }
        }
        return failedObjects;
    }

    /**
     * Checks every update for a potential conflict. Returns a list of objects
     * where a conflict has been found.
     */
    private List<JPAObject> checkUpdates(List<JPAObject> updates) throws EDBException {
        List<JPAObject> failedObjects = new ArrayList<JPAObject>();
        for (JPAObject update : updates) {
            try {
                Integer modelVersion = investigateVersionAndCheckForConflict(update);
                modelVersion++;
                update.removeEntry(EDBConstants.MODEL_VERSION);
                update.addEntry(new JPAEntry(EDBConstants.MODEL_VERSION, modelVersion + "",
                        Integer.class.getName(), update));
            } catch (EDBException e) {
                failedObjects.add(update);
            }
        }
        return failedObjects;
    }

    /**
     * Investigates the version of an JPAObject and checks if a conflict can be
     * found.
     */
    private Integer investigateVersionAndCheckForConflict(JPAObject newObject) throws EDBException {
        JPAEntry entry = newObject.getEntry(EDBConstants.MODEL_VERSION);
        String oid = newObject.getOID();
        Integer modelVersion = 0;

        if (entry != null) {
            modelVersion = Integer.parseInt(entry.getValue());
            Integer currentVersion = dao.getVersionOfOid(oid, checkStage(newObject.getJPAStage()));
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
            modelVersion = dao.getVersionOfOid(oid, checkStage(newObject.getJPAStage()));
        }

        return modelVersion;
    }

    /**
     * Simple check mechanism if there is a conflict between a model which
     * should be saved and the existing model, based on the values which are in
     * the EDB.
     */
    private void checkForConflict(JPAObject newObject) throws EDBException {
        String oid = newObject.getOID();
        JPAObject object = dao.getJPAObject(oid, checkStage(newObject.getJPAStage()));
        for (JPAEntry entry : newObject.getEntries()) {
            if (entry.getKey().equals(EDBConstants.MODEL_VERSION)) {
                continue;
            }
            JPAEntry rival = object.getEntry(entry.getKey());
            String value = rival != null ? rival.getValue() : null;
            if (value == null || !value.equals(entry.getValue())) {
                LOGGER.debug("Conflict detected at key {} when comparing {} with {}", new Object[]{entry.getKey(),
                    entry.getValue(), value == null ? "null" : value});
                throw new EDBException("Conflict detected. Failure when comparing the values of the key "
                        + entry.getKey());
            }
        }
    }

    /**
     * Returns true if the given oid is active right now (means is existing and
     * not deleted) and return false otherwise.
     */
    private boolean checkIfActiveOidExisting(String oid, String sid) {
        try {
            JPAObject obj = dao.getJPAObject(oid, sid);
            if (!obj.isDeleted()) {
                return true;
            }
        } catch (EDBException e) {
            // nothing to do here
        }
        return false;
    }
}
