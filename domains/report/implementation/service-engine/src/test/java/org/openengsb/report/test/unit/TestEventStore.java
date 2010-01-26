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
package org.openengsb.report.test.unit;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.model.Event;
import org.openengsb.report.EventStorageRegistry;
import org.openengsb.report.datastore.EventStorageType;
import org.openengsb.report.datastore.EventStore;
import org.openengsb.report.datastore.InMemoryEventStore;
import org.openengsb.report.datastore.StorageKey;

public class TestEventStore {

    private EventStorageRegistry registry;

    private EventStore eventStore;

    @Before
    public void setUp() {
        registry = new EventStorageRegistry();
        eventStore = new InMemoryEventStore();
    }

    @Test
    public void testStoreEvent() {
        Event e = createEvent();
        StorageKey key = new StorageKey("rep1", EventStorageType.contextId, "42");
        eventStore.storeEvent(key, e);

        List<Event> events = eventStore.getEvents(key);
        Assert.assertEquals(events.size(), 1);
        Assert.assertEquals(events.get(0), e);
    }

    @Test
    public void testStoreEventAndClear() {
        Event e = createEvent();
        StorageKey key = new StorageKey("rep1", EventStorageType.contextId, "42");
        eventStore.storeEvent(key, e);

        List<Event> events = eventStore.getEvents(key);
        eventStore.clearEvents(key);

        Assert.assertEquals(1, events.size());
        Assert.assertEquals(e, events.get(0));
    }

    @Test
    public void testRegistry() {
        StorageKey key = new StorageKey("rep1", EventStorageType.contextId, "42");
        registry.storeEventsFor(key);
        Assert.assertEquals(1, registry.getStorageKeysFor(key.getType(), key.getId()).size());
        Assert.assertEquals(key, registry.getStorageKeysFor(key.getType(), key.getId()).iterator().next());
    }

    private Event createEvent() {
        Event e = new Event("domain", "name");
        e.setValue("value", UUID.randomUUID().toString());
        return e;
    }

}
