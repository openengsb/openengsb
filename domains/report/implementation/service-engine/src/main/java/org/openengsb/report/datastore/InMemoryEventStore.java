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

    private Map<StorageKey, List<Event>> eventMap = new HashMap<StorageKey, List<Event>>();

    @Override
    public List<Event> getEvents(StorageKey key) {
        List<Event> list = eventMap.get(key);
        if (list == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Event>(list);
    }

    @Override
    public void storeEvent(StorageKey key, Event event) {
        List<Event> list = eventMap.get(key);
        if (list == null) {
            list = new ArrayList<Event>();
            eventMap.put(key, list);
        }
        list.add(event);
    }

    @Override
    public void clearEvents(StorageKey key) {
        eventMap.remove(key);
    }

}
