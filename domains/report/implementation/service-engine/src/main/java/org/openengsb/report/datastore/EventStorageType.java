package org.openengsb.report.datastore;

public enum EventStorageType {

    contextId, correlationId, flowId, customId;

    public static EventStorageType fromString(String string) {
        for (EventStorageType type : EventStorageType.values()) {
            if (type.toString().equals(string)) {
                return type;
            }
        }
        return customId;
    }

}
