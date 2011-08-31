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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JPACommit implements EDBCommit {
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
        objects = new ArrayList<EDBObject>();
        deletions = new ArrayList<String>();
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

    @Override
    public final List<EDBObject> getObjects() {
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
    public void add(EDBObject obj) throws EDBException {
        if (!objects.contains(obj)) {
            objects.add(obj);
            LOGGER.debug("Added object " + obj.getOID() + " to the commit");
        }
    }

    @Override
    public void delete(String oid) throws EDBException {
        if (deletions.contains(oid)) {
            LOGGER.debug("could not delete object " + oid + " because it was never added");
            return;
        }
        deletions.add(oid);
        LOGGER.debug("deleted object " + oid + " from the commit");
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
}
