package org.openengsb.report.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.model.Event;

public class InMemoryEventStore implements EventStore {

    private Map<EventStorageType, Map<String, List<Event>>> maps;

    public InMemoryEventStore() {
        for (EventStorageType type : EventStorageType.values()) {
            maps.put(type, new HashMap<String, List<Event>>());
        }
    }

    @Override
    public List<Event> getEvents(StorageKey key) {
        List<Event> list = maps.get(key.getType()).get(key.getId());
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public void storeEvent(StorageKey key, Event event) {
        Map<String, List<Event>> map = maps.get(key.getType());
        enterValue(map, key.getId(), event);
    }

    @Override
    public void clearEvents(StorageKey key) {
        Map<String, List<Event>> map = maps.get(key.getType());
        map.remove(key.getId());
    }

    private void enterValue(Map<String, List<Event>> map, String key, Event data) {
        List<Event> list = map.get(key);
        if (list == null) {
            list = new ArrayList<Event>();
            map.put(key, list);
        }
        list.add(data);
    }

}
