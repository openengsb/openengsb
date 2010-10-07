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

package org.openengsb.domains.report.plaintext.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openengsb.domains.report.IdType;
import org.openengsb.domains.report.NoSuchReportException;

public class ReportStorageRegistry {

    private Map<String, StorageKey> toCollect = new HashMap<String, StorageKey>();

    public void storeDataFor(StorageKey storageKey) {
        toCollect.put(storageKey.getReportId(), storageKey);
    }

    public void stopStoringDataFor(StorageKey storageKey) {
        toCollect.remove(storageKey.getReportId());
    }

    public Set<StorageKey> getStorageKeysFor(IdType type, String id) {
        Set<StorageKey> result = new HashSet<StorageKey>();
        for (StorageKey key : toCollect.values()) {
            if (key.getType().equals(type) && key.getId().equals(id)) {
                result.add(key);
            }
        }
        return result;
    }

    public StorageKey getKeyFor(String reportId) throws NoSuchReportException {
        if (!toCollect.containsKey(reportId)) {
            throw new NoSuchReportException(
                "There is currently no data collection process active for a report with id: " + reportId);
        }
        return toCollect.get(reportId);
    }

}
