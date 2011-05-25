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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.api.edb.EDBEntry;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EDBObjectDiff;

public class ObjectDiff implements EDBObjectDiff {
    private JPACommit startCommit;
    private JPACommit endCommit;
    private EDBObject startState;
    private EDBObject endState;
    private Map<String, EDBEntry> diff;
    private Integer differences;

    /**
     * Constructor
     */
    public ObjectDiff(JPACommit startCommit, JPACommit endCommit,
            EDBObject startState, EDBObject endState) throws EDBException {

        if (startState == null || endState == null) {
            throw new EDBException("Incomplete Diff object, cannot compare null states!");
        }

        diff = new HashMap<String, EDBEntry>();
        differences = 0;
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
        updateDiff();
    }

    /**
     * checks for start state and end state which key/value pairs are in common and which
     * have been changed, added or deleted
     */
    private void updateDiff() throws EDBException {
        List<String> keyList = loadKeyList();

        for (String key : keyList) {
            if (key.equals("id") || key.equals("prevTimestamp") || key.equals("timestamp")) {
                continue;
            }
            Object first = startState.get(key);
            Object last = endState.get(key);

            // if the key value pair is exactly the same, skip it
            if (last != null && first != null && first.equals(last)) {
                continue;
            }
            diff.put(key, new Entry(first, last));
            differences++;
        }
    }

    /**
     * loads all keys from the start state and the end state
     */
    private List<String> loadKeyList() {
        Set<String> keySet = new HashSet<String>();
        for (Map.Entry<String, Object> e : startState.entrySet()) {
            keySet.add(e.getKey());
        }
        for (Map.Entry<String, Object> e : endState.entrySet()) {
            keySet.add(e.getKey());
        }
        return new ArrayList<String>(keySet);
    }

    @Override
    public Map<String, EDBEntry> getDiffMap() {
        return diff;
    }

    @Override
    public final int getDifferenceCount() {
        return differences;
    }

    @Override
    public EDBObject getStartState() {
        return startState;
    }

    @Override
    public EDBObject getEndState() {
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
}
