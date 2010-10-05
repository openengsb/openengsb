/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.report;

import java.util.HashSet;
import java.util.Set;

import org.openengsb.domains.report.datastore.EventStorageType;
import org.openengsb.domains.report.datastore.StorageKey;

public class EventStorageRegistry {
    private Set<StorageKey> toCollect = new HashSet<StorageKey>();

    public void storeEventsFor(StorageKey storageKey) {
        toCollect.add(storageKey);
    }

    public void stopStoringEventsFor(StorageKey storageKey) {
        toCollect.remove(storageKey);
    }

    public Set<StorageKey> getStorageKeysFor(EventStorageType type, String id) {
        Set<StorageKey> result = new HashSet<StorageKey>();
        for (StorageKey key : toCollect) {
            if (key.getType().equals(type) && key.getId().equals(id)) {
                result.add(key);
            }
        }
        return result;
    }

}
