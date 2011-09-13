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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.edb.EDBDiff;
import org.openengsb.core.api.edb.EDBEntry;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EDBObjectDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diff implements EDBDiff {
    private static final Logger LOGGER = LoggerFactory.getLogger(Diff.class);
    private JPACommit startCommit;
    private JPACommit endCommit;
    private List<EDBObject> startState;
    private List<EDBObject> endState;
    private HashMap<String, EDBObjectDiff> diff;

    public Diff(JPACommit startCommit, JPACommit endCommit, List<EDBObject> startState,
            List<EDBObject> endState) throws EDBException {
        if (endCommit.getTimestamp() < startCommit.getTimestamp()) {
            this.startCommit = endCommit;
            this.endCommit = startCommit;
            this.startState = endState;
            this.endState = startState;
        } else {
            this.startCommit = startCommit;
            this.endCommit = endCommit;
            this.startState = startState;
            this.endState = endState;
        }

        createObjectDiffs();
        LOGGER.debug("Diff created. Difference count = {}", diff.size());
    }

    /**
     * Analyzes the start and end state and creates for every object that is different an objectdiff entry
     */
    private void createObjectDiffs() throws EDBException {
        diff = new HashMap<String, EDBObjectDiff>();
        List<EDBObject> tempList = new ArrayList<EDBObject>();
        for (EDBObject o : this.endState) {
            tempList.add(o);
        }

        addModifiedOrDeletedObjects(tempList);
        addNewObjects(tempList);
    }

    /**
     * add all modified or deleted objects to the diff collection. As base to indicate if something changed the start
     * state and the list of elements from the end state is taken.
     */
    private void addModifiedOrDeletedObjects(List<EDBObject> tempList) {
        for (EDBObject a : this.startState) {
            String oid = a.getOID();
            EDBObject b = removeIfExist(oid, tempList);
            ObjectDiff odiff = new ObjectDiff(this.startCommit, this.endCommit, a, b);
            if (odiff.getDifferenceCount() > 0) {
                diff.put(oid, odiff);
            }
        }
    }

    /**
     * add all new object to the diff collection. As base to indicate if an object is new, the list of elements from the
     * end state which are left is taken.
     */
    private void addNewObjects(List<EDBObject> tempList) {
        for (EDBObject b : tempList) {
            String oid = b.getOID();
            ObjectDiff odiff = new ObjectDiff(this.startCommit, this.endCommit, null, b);
            if (odiff.getDifferenceCount() > 0) {
                diff.put(oid, odiff);
            }
        }
    }

    /**
     * Removes the element with the given oid if it exists in the list and returns it. If it not exists, null will be
     * returned.
     */
    private EDBObject removeIfExist(String oid, List<EDBObject> tempList) {
        Iterator<EDBObject> iterator = tempList.iterator();
        while (iterator.hasNext()) {
            EDBObject obj = iterator.next();
            if (obj.getOID().equals(oid)) {
                iterator.remove();
                return obj;
            }
        }
        LOGGER.debug("{} wasn't found in the list of end state objects", oid);
        return null;
    }

    @Override
    public Map<String, EDBObjectDiff> getObjectDiffs() {
        return diff;
    }

    @Override
    public final int getDifferenceCount() {
        return diff.size();
    }

    @Override
    public ObjectDiff getDiff(String key) {
        return (ObjectDiff) diff.get(key);
    }

    @Override
    public List<EDBObject> getStartState() {
        return startState;
    }

    @Override
    public List<EDBObject> getEndState() {
        return endState;
    }

    @Override
    public JPACommit getStartCommit() {
        return startCommit;
    }

    @Override
    public JPACommit getEndCommit() {
        return endCommit;
    }

    @Override
    public String printDifferences() {
        StringBuilder builder = new StringBuilder();
        Map<String, EDBObjectDiff> diff = this.getObjectDiffs();
        for (Map.Entry<String, EDBObjectDiff> e : diff.entrySet()) {
            String oid = e.getKey();

            builder.append("Found a difference for object: " + oid);

            EDBObjectDiff odiff = e.getValue();
            Map<String, EDBEntry> diffMap = odiff.getDiffMap();
            for (Map.Entry<String, EDBEntry> de : diffMap.entrySet()) {
                String key = de.getKey();
                EDBEntry entry = de.getValue();
                builder.append("      Entry: '").append(key).append("' from: '").append(entry.getBefore())
                    .append("' to: '").append(entry.getAfter()).append("'");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
