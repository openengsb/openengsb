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

package org.openengsb.core.events;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.DomainEvents;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.test.NullEvent;

public class DomainProxyFactoryTest {

    public interface TestInterface extends DomainEvents {
        void raiseEvent(NullEvent e);
    }

    public interface TestInterfaceFailName extends DomainEvents {
        void methodx();
    }

    public interface TestInterfaceFailParam extends DomainEvents {
        void raiseEvent();
    }

    public interface TestInterfaceFailParamType extends DomainEvents {
        void raiseEvent(String string);
    }

    private WorkflowService workflowMock;

    @Before
    public void setUp() throws Exception {
        workflowMock = mock(WorkflowService.class);
    }

    @Test
    public void testProxy_shouldWork() throws Exception {
        DomainEventsProxyFactoryBean factory = new DomainEventsProxyFactoryBean();
        factory.setDomainEventInterface(TestInterface.class);
        factory.setWorkflowService(workflowMock);

        TestInterface obj = (TestInterface) factory.getObject();

        NullEvent event = new NullEvent();
        obj.raiseEvent(event);

        Mockito.verify(workflowMock, Mockito.atLeastOnce()).processEvent(event);
    }

    @Test(expected = EventProxyException.class)
    public void testProxyWithFalseName_shouldFail() throws Exception {
        DomainEventsProxyFactoryBean factory = new DomainEventsProxyFactoryBean();
        factory.setDomainEventInterface(TestInterfaceFailName.class);
        factory.setWorkflowService(workflowMock);

        TestInterfaceFailName obj = (TestInterfaceFailName) factory.getObject();
        obj.methodx();
    }

    @Test(expected = EventProxyException.class)
    public void testProxyWithMissingParam_shouldFail() throws Exception {
        DomainEventsProxyFactoryBean factory = new DomainEventsProxyFactoryBean();
        factory.setDomainEventInterface(TestInterfaceFailParam.class);
        factory.setWorkflowService(workflowMock);

        TestInterfaceFailParam obj = (TestInterfaceFailParam) factory.getObject();
        obj.raiseEvent();
    }

    @Test(expected = EventProxyException.class)
    public void testProxyWithWrongParamType_shouldFail() throws Exception {
        DomainEventsProxyFactoryBean factory = new DomainEventsProxyFactoryBean();
        factory.setDomainEventInterface(TestInterfaceFailParamType.class);
        factory.setWorkflowService(workflowMock);

        TestInterfaceFailParamType obj = (TestInterfaceFailParamType) factory.getObject();
        obj.raiseEvent("blub");
    }

}
