/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EndRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.openengsb.core.workflow.internal.persistence.WorkflowRepresentationConverter;

public class WorkflowRepresentationTest {

    @Test
    public void testWorkflowRepresentationMarshalling_shouldMarshallWorkflowCorrectly() throws JAXBException {
        WorkflowRepresentation representation = createWorkflow();
        WorkflowRepresentationConverter workflowRepresentationMarshaller = new WorkflowRepresentationConverter();
        String converted = workflowRepresentationMarshaller.marshallWorkflow(representation);
        WorkflowRepresentation rep = workflowRepresentationMarshaller.unmarshallWorkflow(converted);
        compareActionRepresentation(representation.getRoot(), rep.getRoot());
    }

    private void compareEventRepresentation(EventRepresentation first, EventRepresentation second) {
        assertThat(first.getEvent().getName(), equalTo(second.getEvent().getName()));
        checkActions(first.getActions(), second.getActions());
        checkEvents(first.getEvents(), second.getEvents());
    }

    private void compareActionRepresentation(ActionRepresentation first, ActionRepresentation second) {
        assertThat(first.getCode(), equalTo(second.getCode()));
        assertThat(first.getDomain().getName(), equalTo(second.getDomain().getName()));
        assertThat(first.getLocation(), equalTo(second.getLocation()));
        assertThat(first.getMethodName(), equalTo(second.getMethodName()));
        assertThat(first.getMethodParameters(), equalTo(second.getMethodParameters()));
        checkActions(first.getActions(), second.getActions());
        checkEvents(first.getEvents(), second.getEvents());
    }

    private void checkEvents(List<EventRepresentation> first, List<EventRepresentation> second) {
        assertThat(first.size(), equalTo(second.size()));
        Iterator<EventRepresentation> firstEventsIterator = first.iterator();
        Iterator<EventRepresentation> secondEventsIterator = second.iterator();
        while (firstEventsIterator.hasNext()) {
            compareEventRepresentation(firstEventsIterator.next(), secondEventsIterator.next());
        }
    }

    private void checkActions(List<ActionRepresentation> first, List<ActionRepresentation> second) {
        assertThat(first.size(), equalTo(second.size()));
        Iterator<ActionRepresentation> firstActionsIterator = first.iterator();
        Iterator<ActionRepresentation> secondActionsIterator = second.iterator();
        while (firstActionsIterator.hasNext()) {
            compareActionRepresentation(firstActionsIterator.next(), secondActionsIterator.next());
        }
    }

    private WorkflowRepresentation createWorkflow() {
        WorkflowRepresentation representation = new WorkflowRepresentation();
        representation.setName("Workflow");
        ActionRepresentation root = createAction();
        representation.setRoot(root);
        ActionRepresentation action = createAction();
        action.addAction(createAction());
        action.addEvent(createEvent());
        root.addAction(action);

        EventRepresentation event = createEvent();
        event.addAction(createAction());
        event.addEvent(createEvent());
        root.addEvent(event);
        return representation;
    }

    private EventRepresentation createEvent() {
        EventRepresentation event = new EventRepresentation();
        event.setEvent(NullEvent.class);
        return event;
    }

    private ActionRepresentation createAction() {
        ActionRepresentation action = new ActionRepresentation();
        action.setCode("Code");
        action.setDomain(NullDomain.class);
        action.setMethodName(NullDomain.class.getMethods()[0].getName());
        action.setLocation("Location");
        action.setMethodParameters(Arrays.asList(NullDomain.class.getMethods()[0].getParameterTypes()));
        action.setEnd(new EndRepresentation("End"));
        return action;
    }
}
