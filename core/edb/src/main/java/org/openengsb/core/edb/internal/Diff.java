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

public class Diff implements EDBDiff {
    private JPACommit startCommit;
    private JPACommit endCommit;
    private List<EDBObject> startState;
    private List<EDBObject> endState;
    private HashMap<String, EDBObjectDiff> diff;

    /**
     * Constructor
     */
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

        for (EDBObject a : this.startState) {
            String uid = a.getUID();
            EDBObject b = removeIfExist(uid, tempList);
            ObjectDiff odiff = new ObjectDiff(this.startCommit, this.endCommit, a, b);
            if (odiff.getDifferenceCount() > 0) {
                diff.put(uid, odiff);
            }
        }
    }

    /**
     * Removes the element with the given uid if it exists in the list and returns it. If it not exists, null will be
     * returned.
     */
    private EDBObject removeIfExist(String uid, List<EDBObject> tempList) {
        Iterator<EDBObject> iterator = tempList.iterator();
        while (iterator.hasNext()) {
            EDBObject obj = iterator.next();
            if (obj.getUID().equals(uid)) {
                iterator.remove();
                return obj;
            }
        }
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
            String uid = e.getKey();

            builder.append("    Found a difference for object: " + uid);

            EDBObjectDiff odiff = e.getValue();
            Map<String, EDBEntry> diffMap = odiff.getDiffMap();
            for (Map.Entry<String, EDBEntry> de : diffMap.entrySet()) {
                String key = de.getKey();
                EDBEntry entry = de.getValue();
                builder.append("      Entry: '" + key + "' from: '" + entry.getBefore() + "' to: '"
                        + entry.getAfter() + "'");
            }
        }
        return builder.toString();
    }
}
