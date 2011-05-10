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

import java.util.List;
import java.util.Map;


/**
 * A Diff object stores hierarchically the differences between two states. It provides access to the commit objects
 * representing the first and last commits which are compared, and contains a Map of UIDs to ObjectDiff objects for each
 * object which undergoes any change in the range of the two commits.
 */
public interface EDBDiff {

    /**
     * Get the Mapping from UIDs to ObjectDiff objects. The map only contains objects which actually change between the
     * two commits, whic are compared by this Diff object.
     */
    Map<String, EDBObjectDiff> getObjectDiffs();

    /**
     * Get the amount of objects which are not the same in the beginning and the end. This essencially just returns
     * getObjectDiffs().size()
     */
    int getDifferenceCount();

    /**
     * Get the ObjectDiff for a specific UID. Note that this function returns null in two cases: Either when no object
     * with this UID exists, or when it does, but is the very same at the time of both commits.
     */
    EDBObjectDiff getDiff(String key);

    /**
     * Get all objects at the state of the first commit.
     */
    List<EDBObject> getStartState();

    /**
     * Get all objects at the state of the last commit.
     */
    List<EDBObject> getEndState();

    /**
     * Get the commit from which we start comparing.
     */
    EDBCommit getStartCommit();

    /**
     * Get the commit at which the comparison ends.
     */
    EDBCommit getEndCommit();
}
