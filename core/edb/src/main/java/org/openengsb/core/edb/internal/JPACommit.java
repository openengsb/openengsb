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

import javax.persistence.Entity;

import org.openengsb.core.edb.Commit;
import org.openengsb.core.edb.Database;
import org.openengsb.core.edb.EDBObject;
import org.openengsb.core.edb.exceptions.EDBException;

@Entity
public class JPACommit extends Commit {
    private List<JPAObject> jpaObjects;

    public JPACommit() {
        super();
    }

    public JPACommit(String committer, String role, long timestamp, Database db) {
        super(committer, role, timestamp, db);
    }

    @Override
    public void finalize() throws EDBException {
        if (getCommitted()) {
            throw new EDBException("Commit already finalized, probably already committed.");
        }
        fillUIDs();
        jpaObjects = new ArrayList<JPAObject>();
        for (EDBObject o : getObjects()) {
            jpaObjects.add(new JPAObject(o));
        }
    }

    @Override
    public void loadCommit() throws EDBException {
        for (JPAObject o : jpaObjects) {
            getObjects().add(o.getObject());
        }
        jpaObjects.clear();
    }
}
