package org.openengsb.report;

import java.util.HashSet;
import java.util.Set;

import org.openengsb.report.datastore.StorageKey;

public class EventStoragePolicy {
    private Set<StorageKey> toCollect = new HashSet<StorageKey>();

    public void storeEventsFor(StorageKey storageKey) {
        toCollect.add(storageKey);
    }

    public void stopStoringEventsFor(StorageKey storageKey) {
        toCollect.remove(storageKey);
    }

    public boolean areEventsStoredFor(StorageKey storageKey) {
        return toCollect.contains(storageKey);
    }
}
