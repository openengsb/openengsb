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

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.openengsb.core.common.AbstractDataRow;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JPACommit extends AbstractDataRow implements EDBCommit {
    private static final Logger LOGGER = LoggerFactory.getLogger(JPACommit.class);

    @Column(name = "COMMITER", length = 50)
    private String committer;
    @Column(name = "TIME")
    private Long timestamp;
    @Column(name = "CONTEXT", length = 50)
    private String context;
    @Column(name = "DELS")
    @ElementCollection
    private List<String> deletions;
    @Column(name = "OIDS")
    @ElementCollection
    private List<String> oids;
    @Column(name = "ISCOMMITED")
    private Boolean committed = false;

    private List<EDBObject> objects;

    @Transient
    private List<EDBObject> inserts;
    @Transient
    private List<EDBObject> updates;
    /**
     * the empty constructor is only for the jpa enhancer. Do not use it in real code.
     */
    @Deprecated
    public JPACommit() {
    }

    public JPACommit(String committer, String contextId) {
        this.committer = committer;
        this.context = contextId;

        oids = new ArrayList<String>();
        deletions = new ArrayList<String>();
        inserts = new ArrayList<EDBObject>();
        updates = new ArrayList<EDBObject>();
    }

    @Override
    public void setCommitted(Boolean c) {
        committed = c;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public List<String> getOIDs() {
        fillOIDs();
        return oids;
    }

    public final List<EDBObject> getObjects() {
        List<EDBObject> objects = new ArrayList<EDBObject>();
        objects.addAll(inserts);
        objects.addAll(updates);
        return objects;
    }

    @Override
    public final List<String> getDeletions() {
        return deletions;
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

    private void fillOIDs() {
        if (oids == null) {
            oids = new ArrayList<String>();
        } else {
            oids.clear();
        }
        for (EDBObject o : objects) {
            oids.add(o.getOID());
        }
    }

    @Override
    public void insert(EDBObject obj) throws EDBException {
        if (!inserts.contains(obj)) {
            inserts.add(obj);
            LOGGER.debug("Added object {} to the commit for inserting", obj.getOID());
        }
    }

    @Override
    public void update(EDBObject obj) throws EDBException {
        if (!updates.contains(obj)) {
            updates.add(obj);
            LOGGER.debug("Added object {} to the commit for updating", obj.getOID());
        }
    }

    @Override
    public List<EDBObject> getInserts() {
        return inserts;
    }

    @Override
    public List<EDBObject> getUpdates() {
        return updates;
    }
}
