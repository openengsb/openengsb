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
import java.util.UUID;
import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.jpa.internal.util.EDBUtils;
import org.openengsb.core.edb.api.EDBStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JPACommit extends VersionedEntity implements EDBCommit {

    private static final Logger LOGGER = LoggerFactory.getLogger(JPACommit.class);

    @Column(name = "COMMITER", length = 50)
    private String committer;
    @Column(name = "TIME")
    private Long timestamp;
    @Column(name = "CONTEXT", length = 50)
    private String context;
    @Column(name = "COMMENT", length = 200)
    private String comment;
    @Column(name = "ISCOMMITED")
    private Boolean committed = false;
    @Column(name = "REVISION")
    private String revision;
    @Column(name = "PARENT")
    private String parent;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "STAGE", nullable = true)
    private JPAStage stage;
    @Column(name = "DOMAIN")
    private String domainId;
    @Column(name = "CONNECTOR")
    private String connectorId;
    @Column(name = "INSTANCE")
    private String instanceId;
    @Column(name = "INSERTS")
    @OneToMany(fetch = FetchType.EAGER)
    private List<JPAObject> inserts;
    @Column(name = "UPDATES")
    @OneToMany(fetch = FetchType.EAGER)
    private List<JPAObject> updates;
    @Column(name = "DELS")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> deletions;

    /**
     * the empty constructor is only for the jpa enhancer. Do not use it in real
     * code.
     */
    @Deprecated
    public JPACommit() {
        inserts = new ArrayList<JPAObject>();
        updates = new ArrayList<JPAObject>();
        deletions = new ArrayList<String>();
    }

    public JPACommit(String committer, String contextId) {
        this.committer = committer;
        this.context = contextId;
        this.stage = null;
        deletions = new ArrayList<String>();
        inserts = new ArrayList<JPAObject>();
        updates = new ArrayList<JPAObject>();
        this.revision = UUID.randomUUID().toString();
    }

    @Override
    public void setCommitted(Boolean committed) {
        this.committed = committed;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    public final List<EDBObject> getObjects() {
        List<JPAObject> objects = new ArrayList<JPAObject>();
        objects.addAll(inserts);
        objects.addAll(updates);
        return EDBUtils.convertJPAObjectsToEDBObjects(objects);
    }

    public List<JPAObject> getJPAObjects() {
        List<JPAObject> objects = new ArrayList<JPAObject>();
        objects.addAll(inserts);
        objects.addAll(updates);
        return objects;
    }

    @Override
    public final List<String> getDeletions() {
        return deletions != null ? deletions : new ArrayList<String>();
    }

    public void setDeletions(List<String> deletions) {
        this.deletions = deletions;
    }

    @Override
    public final String getCommitter() {
        return committer;
    }

    @Override
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public final Long getTimestamp() {
        return timestamp;
    }

    @Override
    public final String getContextId() {
        return context;
    }

    @Override
    public void delete(String oid) throws EDBException {
        if (deletions.contains(oid)) {
            LOGGER.debug("could not delete object {} because it was never added", oid);
            return;
        }
        deletions.add(oid);
        LOGGER.debug("deleted object {} from the commit", oid);
    }

    public void deleteAll(List<EDBObject> objects) throws EDBException {
        if (objects != null) {
            for (EDBObject object : objects) {
                delete(object.getOID());
            }
        }
    }

    @Override
    public void insert(EDBObject obj) throws EDBException {
        if (!inserts.contains(obj)) {
            inserts.add(EDBUtils.convertEDBObjectToJPAObject(obj));
            LOGGER.debug("Added object {} to the commit for inserting", obj.getOID());
        }
    }

    public void insertAll(List<EDBObject> objects) throws EDBException {
        if (objects != null) {
            for (EDBObject object : objects) {
                insert(object);
            }
        }
    }

    @Override
    public void update(EDBObject obj) throws EDBException {
        if (!updates.contains(obj)) {
            updates.add(EDBUtils.convertEDBObjectToJPAObject(obj));
            LOGGER.debug("Added object {} to the commit for updating", obj.getOID());
        }
    }

    public void updateAll(List<EDBObject> objects) throws EDBException {
        if (objects != null) {
            for (EDBObject object : objects) {
                update(object);
            }
        }
    }

    @Override
    public List<EDBObject> getInserts() {
        return inserts != null ? EDBUtils.convertJPAObjectsToEDBObjects(inserts)
                : new ArrayList<EDBObject>();
    }

    public List<JPAObject> getInsertedObjects() {
        return inserts != null ? inserts : new ArrayList<JPAObject>();
    }

    @Override
    public List<EDBObject> getUpdates() {
        return updates != null ? EDBUtils.convertJPAObjectsToEDBObjects(updates)
                : new ArrayList<EDBObject>();
    }

    public List<JPAObject> getUpdatedObjects() {
        return updates != null ? updates : new ArrayList<JPAObject>();
    }

    @Override
    public UUID getParentRevisionNumber() {
        return parent != null ? UUID.fromString(parent) : null;
    }

    @Override
    public UUID getRevisionNumber() {
        return revision != null ? UUID.fromString(revision) : null;
    }

    @Override
    public void setHeadRevisionNumber(UUID head) {
        this.parent = head != null ? head.toString() : null;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }

    @Override
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public EDBStage getEDBStage() {
        return this.stage;
    }

    @Override
    public void setEDBStage(EDBStage stage) {
        this.stage = (JPAStage) stage;
    }
}
