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
package org.openengsb.core.workflow.flow;

import org.junit.Test;
import org.openengsb.core.common.Event;
import org.openengsb.core.workflow.EventHelper;

import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

public class EventHelperTest {

    private class EventListenerThread extends Thread {
        private EventHelper eventHelper;
        private Event lastEvent;

        public EventListenerThread(EventHelper eventHelper) {
            this.eventHelper = eventHelper;
        }

        @Override
        public void run() {
            try {
                lastEvent = eventHelper.waitForEvent();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public Event getLastEvent() {
            return this.lastEvent;
        }
    }

    @Test
    public void testEventHelperWaitForEvent() throws Exception {
        EventHelper eventHelper = new EventHelper();
        EventListenerThread thread = new EventListenerThread(eventHelper);
        thread.start();
        Thread.sleep(200);
        Event e = new Event();
        eventHelper.insertEvent(e);
        thread.join();

        assertThat(thread.getLastEvent(), sameInstance(e));
    }

}
