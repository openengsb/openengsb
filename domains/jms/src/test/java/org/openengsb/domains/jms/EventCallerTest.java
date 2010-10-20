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

package org.openengsb.domains.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.DomainEvents;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.Event;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class EventCallerTest {

    @Test
    public void raiseEvent_shouldCallFromBundleContext() {
        DomainProvider provider = mock(DomainProvider.class);
        when(provider.getDomainEventInterface()).thenAnswer(new Answer<Class<TestInterface>>() {
            @Override
            public Class<TestInterface> answer(InvocationOnMock invocation) {
                return TestInterface.class;
            }
        });
        BundleContext context = mock(BundleContext.class);
        ServiceReference mock2 = mock(ServiceReference.class);
        when(context.getServiceReference(TestInterface.class.getName())).thenReturn(mock2);
        TestInterface testInterface = mock(TestInterface.class);
        when(context.getService(mock2)).thenReturn(testInterface);
        EventCaller caller = new EventCaller(context, provider);
        TestEvent event = new TestEvent();
        caller.raiseEvent(event);
        verify(testInterface).raiseEvent(event);
        TestEvent2 event2 = new TestEvent2();
        caller.raiseEvent(event2);
        verify(testInterface).raiseEvent(event2);
    }

    private interface TestInterface extends DomainEvents {
        void raiseEvent(TestEvent event);

        void raiseEvent(TestEvent2 event);
    }

    private static class TestEvent extends Event {

    }

    private static class TestEvent2 extends Event {

    }
}
