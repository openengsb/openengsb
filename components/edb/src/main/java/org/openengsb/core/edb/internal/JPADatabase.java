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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.edb.EDBBatchEvent;
import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBDeleteEvent;
import org.openengsb.core.api.edb.EDBEvent;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBInsertEvent;
import org.openengsb.core.api.edb.EDBLogEntry;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EDBUpdateEvent;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.edb.internal.dao.DefaultJPADao;
import org.openengsb.core.edb.internal.dao.JPADao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

public class JPADatabase implements org.openengsb.core.api.edb.EngineeringDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JPADatabase.class);
    private EntityTransaction utx;
    @PersistenceContext(name = "openengsb-edb")
    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private JPADao dao;

    /**
     * this is just for testing the JPADatabase. Should only be called in the corresponding test class.
     */
    public void open() throws EDBException {
        LOGGER.debug("starting to open EDB for testing via JPA");
        Properties props = new Properties();
        emf = Persistence.createEntityManagerFactory("edb-test", props);
        setEntityManager(emf.createEntityManager());
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
        emf = null;
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

        long timestamp = System.currentTimeMillis();
        commit.setTimestamp(timestamp);
        for (EDBObject update : commit.getObjects()) {
            update.updateTimestamp(timestamp);
            entityManager.persist(new JPAObject(update));
        }

        try {
            performUtxAction(UTXACTION.BEGIN);
            commit.setCommitted(true);
            LOGGER.debug("persisting JPACommit");
            entityManager.persist(commit);

            LOGGER.debug("setting the deleted elements as deleted");
            for (String id : commit.getDeletions()) {
                EDBObject o = new EDBObject(id);
                o.updateTimestamp(timestamp);
                o.put("isDeleted", new Boolean(true));
                JPAObject j = new JPAObject(o);
                entityManager.persist(j);
            }

            performUtxAction(UTXACTION.COMMIT);
        } catch (Exception ex) {
            try {
                performUtxAction(UTXACTION.ROLLBACK);
            } catch (Exception e) {
                throw new EDBException("Failed to rollback transaction to DB", e);
            }
            throw new EDBException("Failed to commit transaction to DB", ex);
        }

        return timestamp;
    }

    /**
     * helper function that performs a UTXACTION if the utx is not null
     */
    private void performUtxAction(UTXACTION action) {
        if (utx == null) {
            return;
        }
        switch (action) {
            case BEGIN:
                utx.begin();
                break;
            case COMMIT:
                utx.commit();
                break;
            case ROLLBACK:
                utx.rollback();
                break;
            default:
                LOGGER.warn("unknown Transaction action: {}", action.toString());
                break;
        }
    }

    /**
     * enumeration for categorizing the transaction actions.
     */
    private enum UTXACTION {
            BEGIN, COMMIT, ROLLBACK
    };

    @Override
    public EDBObject getObject(String oid) throws EDBException {
        LOGGER.debug("loading newest JPAObject with the oid {}", oid);
        JPAObject temp = dao.getJPAObject(oid);
        return temp.getObject();
    }

    @Override
    public List<EDBObject> getObjects(List<String> oids) throws EDBException {
        List<JPAObject> objects = dao.getJPAObjects(oids);
        List<EDBObject> result = new ArrayList<EDBObject>();
        for (JPAObject object : objects) {
            result.add(object.getObject());
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
        for (JPAObject j : jpaObjects) {
            result.add(j.getObject());
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

    @Override
    public void processEDBInsertEvent(EDBInsertEvent event) throws EDBException {
        LOGGER.debug("received insert event");

        makeEDBActions(Arrays.asList(event.getModel()), null, null, event);

        LOGGER.debug("successfully inserted model");
    }

    @Override
    public void processEDBDeleteEvent(EDBDeleteEvent event) throws EDBException {
        LOGGER.debug("received delete event");

        makeEDBActions(null, null, Arrays.asList(event.getModel()), event);

        LOGGER.debug("successfully deleted model");
    }

    @Override
    public void processEDBUpdateEvent(EDBUpdateEvent event) throws EDBException {
        LOGGER.debug("received update event");

        makeEDBActions(null, Arrays.asList(event.getModel()), null, event);

        LOGGER.debug("successfully updated model");
    }

    @Override
    public void processEDBBatchEvent(EDBBatchEvent event) throws EDBException {
        LOGGER.debug("received batch event");

        makeEDBActions(event.getInserts(), event.getUpdates(), event.getDeletions(), event);

        LOGGER.debug("successfully run through the edb batch event");
    }

    private void makeEDBActions(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates,
            List<OpenEngSBModel> deletes, EDBEvent event) throws EDBException {
        JPACommit commit = createCommit(getAuthenticatedUser(), getActualContextId());

        if (inserts != null) {
            for (EDBObject object : checkInserts(inserts, event)) {
                commit.add(object);
            }
        }
        if (deletes != null) {
            for (String oid : checkDeletions(deletes, event)) {
                commit.delete(oid);
            }
        }
        if (updates != null) {
            for (EDBObject object : checkUpdates(updates, event)) {
                commit.add(object);
            }
        }

        this.commit(commit);
    }

    private List<EDBObject> checkInserts(List<OpenEngSBModel> inserts, EDBEvent event) throws EDBException {
        List<EDBObject> objects = new ArrayList<EDBObject>();
        for (OpenEngSBModel model : inserts) {
            String oid = ModelConverterUtils.createOID(model, event);
            if (checkIfActiveOidExisting(oid)) {
                throw new EDBException("object under the given oid is already existing");
            } else {
                objects.addAll(convertModelToEDBObject(model, oid, event, 1));
            }
        }
        return objects;
    }

    private List<String> checkDeletions(List<OpenEngSBModel> deletions, EDBEvent event) throws EDBException {
        List<String> oids = new ArrayList<String>();
        for (OpenEngSBModel model : deletions) {
            String oid = ModelConverterUtils.createOID(model, event);
            if (!checkIfActiveOidExisting(oid)) {
                throw new EDBException("the object under given oid is not existing or already deleted");
            } else {
                oids.add(oid);
            }
        }
        return oids;
    }

    private List<EDBObject> checkUpdates(List<OpenEngSBModel> updates, EDBEvent event) throws EDBException {
        List<EDBObject> objects = new ArrayList<EDBObject>();
        for (OpenEngSBModel model : updates) {
            String oid = ModelConverterUtils.createOID(model, event);
            Integer modelVersion = investigateVersionAndCheckForConflict(model, oid);
            modelVersion++;
            model.addOpenEngSBModelEntry(new OpenEngSBModelEntry(ModelConverterUtils.MODELVERSION, modelVersion,
                Integer.class));
            objects.addAll(convertModelToEDBObject(model, oid, event, modelVersion));
        }
        return objects;
    }

    private Integer investigateVersionAndCheckForConflict(OpenEngSBModel model, String oid) throws EDBException {
        Integer modelVersion = ModelConverterUtils.getModelVersion(model);

        if (modelVersion != null) {
            Integer currentVersion = getVersionOfOid(oid);
            if (!modelVersion.equals(currentVersion)) {
                try {
                    checkForConflict(model, oid);
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

    private List<EDBObject> convertModelToEDBObject(OpenEngSBModel model, String oid, EDBEvent event, Integer version) {
        List<EDBObject> objects = new ArrayList<EDBObject>();
        convertSubModel(model, event, objects, oid, version);
        return objects;
    }

    private String convertSubModel(OpenEngSBModel model, EDBEvent event, List<EDBObject> objects) {
        return convertSubModel(model, event, objects, null, null);
    }

    private String convertSubModel(OpenEngSBModel model, EDBEvent event, List<EDBObject> objects, String oid,
            Integer version) {
        Integer modelVersion;
        if (oid == null) {
            oid = ModelConverterUtils.createOID(model, event);
        }
        if (version == null) {
            if (checkIfActiveOidExisting(oid)) {
                modelVersion = investigateVersionAndCheckForConflict(model, oid);
            } else {
                modelVersion = 1;
            }
        } else {
            modelVersion = version;
        }

        EDBObject object = new EDBObject(oid);

        for (OpenEngSBModelEntry entry : model.getOpenEngSBModelEntries()) {
            if (OpenEngSBModel.class.isAssignableFrom(entry.getType())) {
                if (entry.getValue() == null) {
                    continue;
                }
                String subOid = convertSubModel((OpenEngSBModel) entry.getValue(), event, objects);
                object.put(entry.getKey(), subOid);
            } else if (List.class.isAssignableFrom(entry.getType())) {
                @SuppressWarnings("unchecked")
                List<OpenEngSBModel> subList = (List<OpenEngSBModel>) entry.getValue();
                if (subList == null) {
                    continue;
                }
                for (int i = 0; i < subList.size(); i++) {
                    String subOid = convertSubModel((OpenEngSBModel) subList.get(i), event, objects);
                    object.put(entry.getKey() + i, subOid);
                }
            } else {
                object.put(entry.getKey(), entry.getValue());
            }
        }
        object.put("domainId", event.getDomainId());
        object.put("connectorId", event.getConnectorId());
        object.put("instanceId", event.getInstanceId());
        object.put(ModelConverterUtils.MODELVERSION, modelVersion);

        objects.add(object);
        return oid;
    }

    private String getAuthenticatedUser() {
        // if JPADatabase is called via integration tests
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "testuser";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getActualContextId() {
        // if JPADatabase is called via integration tests
        if (ContextHolder.get() == null) {
            return "testcontext";
        }
        return ContextHolder.get().getCurrentContextId();
    }

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
     * simple check mechanism if there is a conflict between a model which should be saved and the existing model under
     * the given oid, based on the values which are in the edb.
     */
    private void checkForConflict(OpenEngSBModel model, String oid) throws EDBException {
        EDBObject object = getObject(oid);
        for (OpenEngSBModelEntry entry : model.getOpenEngSBModelEntries()) {
            Object value = object.get(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                throw new EDBException();
            }
        }
    }

    private Integer getVersionOfOid(String oid) throws EDBException {
        return dao.getVersionOfOid(oid);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        dao = new DefaultJPADao(entityManager);
    }
}
