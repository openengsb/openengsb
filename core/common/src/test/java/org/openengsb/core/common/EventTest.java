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

package org.openengsb.core.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.beans.IntrospectionException;

import org.junit.Test;

public class EventTest {

    @Test
    public void testToString() throws IntrospectionException {
        Event event = new TestEvent("test");
        assertThat(event.toString(), equalTo("Event Properties => class:" + event.getClass().toString()
                + "; int:5; name:test; type:TestEvent;"));
    }

    private static class TestEvent extends Event {
        public TestEvent(String name) {
            super(name);
        }

        public int getInt() {
            return 5;
        }
    }
}
