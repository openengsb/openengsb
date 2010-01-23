package org.openengsb.report.datastore;

import java.util.List;

import org.openengsb.core.model.Event;

public interface EventStore {

    void storeEvent(StorageKey key, Event event);

    List<Event> getEvents(StorageKey key);

    void clearEvents(StorageKey key);

}
