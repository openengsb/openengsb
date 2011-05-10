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

package org.openengsb.core.api.edb;

import java.util.Map;

/**
 * An ObjectDiff compares two EDBObjects of the same UID and stores their differences. Note that "objects" may be null
 * in case they didn't exist, whereas commits must never be null!
 */
public interface EDBObjectDiff {
    /**
     * Get a map of all the fields which are different in the two provided states.
     * 
     * @return A Map from the field name to ObjectDiff.Entry
     */
    Map<String, EDBEntry> getDiffMap();

    /**
     * Get the number of fields which are not the same in both states.
     * 
     * @return The number of changes.
     */
    int getDifferenceCount();

    /**
     * Get the EDBObject at its initial state of the comparison.
     * 
     * @return The full object at its initial state.
     */
    EDBObject getStartState();

    /**
     * Get the EDBObject at its final state of the comparison.
     * 
     * @return The full object at its final state.
     */
    EDBObject getEndState();

    /**
     * Get the full commit object for the initial state.
     * 
     * @return The initial commit.
     */
    EDBCommit getStartCommit();

    /**
     * Get the full commit object for the final state.
     * 
     * @return The final commit.
     */
    EDBCommit getEndCommit();
}
