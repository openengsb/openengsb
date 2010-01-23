package org.openengsb.report.datastore;

public class StorageKey {
    private final EventStorageType type;
    private final String id;

    public StorageKey(String type, String id) {
        this(EventStorageType.fromString(type), id);
    }

    public StorageKey(EventStorageType type, String id) {
        this.type = type;
        this.id = id;
    }

    public EventStorageType getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
