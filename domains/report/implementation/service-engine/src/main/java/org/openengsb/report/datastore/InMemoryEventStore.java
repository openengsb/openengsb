/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
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
        maps = new HashMap<EventStorageType, Map<String, List<Event>>>();
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
