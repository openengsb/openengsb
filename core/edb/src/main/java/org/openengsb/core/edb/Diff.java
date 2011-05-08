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

package org.openengsb.core.edb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openengsb.core.edb.exceptions.EDBException;

/**
 * A Diff object stores hierarchically the differences between two states. It provides access to the commit objects
 * representing the first and last commits which are compared, and contains a Map of UIDs to ObjectDiff objects for each
 * object which undergoes any change in the range of the two commits.
 */
public class Diff {
    private Commit commitA;
    private Commit commitB;
    private List<EDBObject> stateA;
    private List<EDBObject> stateB;
    private HashMap<String, ObjectDiff> diff;

    public Diff(Commit ca, Commit cb, List<EDBObject> oa, List<EDBObject> ob) throws EDBException {
        diff = new HashMap<String, ObjectDiff>();
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

        // First create a shallow copy - .clone() is not in the interface itself :(
        List<EDBObject> tempList = new ArrayList<EDBObject>();
        for (EDBObject o : stateB) {
            tempList.add(o);
        }

        for (EDBObject a : stateA) {
            String uid = a.getString("@uid");
            EDBObject b = removeIfExist(uid, tempList);
            ObjectDiff odiff = new ObjectDiff(commitA, commitB, a, b);
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
            if (obj.getString("@uid").equals(uid)) {
                iterator.remove();
                return obj;
            }
        }
        return null;
    }

    /**
     * Get the Mapping from UIDs to ObjectDiff objects. The map only contains objects which actually change between the
     * two commits, whic are compared by this Diff object.
     * 
     * @return a Map<String, ObjectDiff> mapping.
     */
    public Map<String, ObjectDiff> getObjectDiffs() {
        return diff;
    }

    /**
     * Get the amount of objects which are not the same in the beginning and the end. This essencially just returns
     * getObjectDiffs().size()
     * 
     * @return The number of changed objects.
     */
    public final int getDifferenceCount() {
        return diff.size();
    }

    /**
     * Get the ObjectDiff for a specific UID. Note that this function returns null in two cases: Either when no object
     * with this UID exists, or when it does, but is the very same at the time of both commits.
     * 
     * @param key The desired object's UID.
     * @return ObjectDiff for object with the provided UID, null if it does not exist or is not changed!
     */
    public ObjectDiff getDiff(String key) {
        return (ObjectDiff) diff.get(key);
    }

    /**
     * Get all objects at the state of the first commit.
     * 
     * @return A list of EDBObjects representing the full state at the time of the first commit.
     */
    public List<EDBObject> getStartState() {
        return stateA;
    }

    /**
     * Get all objects at the state of the last commit.
     * 
     * @return A list of EDBObjects representing the full state at the time of the last commit.
     */
    public List<EDBObject> getEndState() {
        return stateB;
    }

    /**
     * Get the commit from which we start comparing.
     * 
     * @return The start-commit object.
     */
    public Commit getStartCommit() {
        return commitA;
    }

    /**
     * Get the commit at which the comparison ends.
     * 
     * @return The end-commit object.
     */
    public Commit getEndCommit() {
        return commitB;
    }
}
