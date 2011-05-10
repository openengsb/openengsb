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
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.edb.EDBEntry;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EDBObjectDiff;

public class ObjectDiff implements EDBObjectDiff {
    private JPACommit commitA;
    private JPACommit commitB;
    private EDBObject stateA;
    private EDBObject stateB;
    private Map<String, EDBEntry> diff;
    private Integer differences;

    public ObjectDiff(JPACommit ca, JPACommit cb, EDBObject oa, EDBObject ob) throws EDBException {
        diff = new HashMap<String, EDBEntry>();
        differences = 0;
        if (cb.getTimestamp() < ca.getTimestamp()) {
            this.commitA = cb;
            this.commitB = ca;
            this.stateA = ob;
            this.stateB = oa;
        } else {
            this.commitA = ca;
            this.commitB = cb;
            this.stateA = oa;
            this.stateB = ob;
        }
        updateDiff();
    }

    private void updateDiff() throws EDBException {
        if (stateA == null || stateB == null) {
            throw new EDBException("Incomplete Diff object, cannot compare null states!");
        }

        diff.clear();
        // Now create a mapping
        List<String> keyList = new ArrayList<String>();
        if (stateA != null) {
            for (Map.Entry<String, Object> e : stateA.entrySet()) {
                String key = e.getKey();
                if (key.equals("_id") || key.equals("@prevTimestamp") || key.equals("@timestamp")) {
                    continue;
                }
                keyList.add(key);

                Object first = e.getValue();
                Object last = (stateB != null) ? stateB.get(key) : null;

                if (last != null && first.equals(last)) {
                    continue;
                }
                diff.put(key, new Entry(first, last));
                ++differences;
            }
        }
        keyList.add("_id");
        keyList.add("@prevTimestamp");
        keyList.add("@timestamp");

        // Now we have all keys from stateA, now add the missing keys from stateB
        if (stateB != null) {
            for (Map.Entry<String, Object> e : stateB.entrySet()) {
                String key = e.getKey();
                if (keyList.contains(key)) {
                    continue;
                }
                diff.put(key, new Entry(null, e.getValue()));
                ++differences;
            }
        }
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
        return stateA;
    }

    @Override
    public EDBObject getEndState() {
        return stateB;
    }

    @Override
    public JPACommit getStartCommit() {
        return commitA;
    }

    @Override
    public JPACommit getEndCommit() {
        return commitB;
    }
}
