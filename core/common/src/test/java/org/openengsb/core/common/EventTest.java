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
