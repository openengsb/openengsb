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

package org.openengsb.core.workflow.editor.converter;

import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.workflow.WorkflowConverter;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EndRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.xml.sax.SAXException;

/**
 * Test Method use different naming scheme here, to be clearer which potential structures in the Workflow are tested by
 * each method.
 */
public class DroolsConverterTest {

    private WorkflowRepresentation workflow;

    @Before
    public void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        workflow = new WorkflowRepresentation();
        workflow.setName("workflow");
        setActionValues(workflow.getRoot());
    }

    @Test
    public void testCallsWorkflow_shouldConvertCorrectly() throws SAXException, IOException,
        JAXBException {
        ActionRepresentation root = workflow.getRoot();
        EndRepresentation end = new EndRepresentation();
        root.addAction(createAction(end));
        root.addAction(createAction(end));
        EventRepresentation event = createEvent();
        event.addAction(createAction(end));
        event.addAction(createAction(end));
        root.addEvent(event);
        String convert = convertWorkflowToString();
        assertConversionResult(convert, "test-workflow");
    }

    @Test
    public void actionFollowingAction() throws SAXException, IOException, JAXBException {
        workflow.getRoot().addAction(createAction());
        assertConversionResult(convertWorkflowToString(), "actionFollowingAction");
    }

    @Test
    public void actionsFollowingAction() throws SAXException, IOException, JAXBException {
        workflow.getRoot().addAction(createAction());
        workflow.getRoot().addAction(createAction());
        assertConversionResult(convertWorkflowToString(), "actionsFollowingAction");
    }

    @Test
    public void actionsFollowingActionWithSharedEnd() throws SAXException, IOException, JAXBException {
        ActionRepresentation createAction = createAction();
        EndRepresentation end = new EndRepresentation();
        createAction.setEnd(end);
        workflow.getRoot().addAction(createAction);
        workflow.getRoot().addAction(createAction);
        assertConversionResult(convertWorkflowToString(), "actionsFollowingActionWithSharedEnd");
    }

    @Test
    public void actionFollowingEvent_eventFollowingAction() throws SAXException, IOException, JAXBException {
        ActionRepresentation createAction = createAction();
        EventRepresentation event = createEvent();
        event.addAction(createAction);
        workflow.getRoot().addEvent(event);
        assertConversionResult(convertWorkflowToString(), "actionFollowingEvent_eventFollowingAction");
    }

    @Test
    public void actionsFollowingEvent() throws SAXException, IOException, JAXBException {
        ActionRepresentation createAction = createAction();
        EventRepresentation event = createEvent();
        event.addAction(createAction);
        event.addAction(createAction);
        workflow.getRoot().addEvent(event);
        assertConversionResult(convertWorkflowToString(), "actionsFollowingEvent");
    }

    @Test
    public void eventsFollowingAction() throws SAXException, IOException, JAXBException {
        ActionRepresentation createAction = createAction();
        EventRepresentation event = createEvent();
        event.addAction(createAction);
        workflow.getRoot().addEvent(event);
        workflow.getRoot().addEvent(event);
        assertConversionResult(convertWorkflowToString(), "eventsFollowingAction");
    }

    @Test
    public void actionAndEventFollowingAction() throws SAXException, IOException, JAXBException {
        EndRepresentation end = new EndRepresentation();
        ActionRepresentation createAction = createAction(end);
        EventRepresentation event = createEvent();
        ActionRepresentation createAction2 = createAction(end);
        event.addAction(createAction2);
        workflow.getRoot().addAction(createAction);
        workflow.getRoot().addEvent(event);
        assertConversionResult(convertWorkflowToString(), "actionAndEventFollowingAction");
    }

    @Test
    public void eventFollowingEvent() throws SAXException, IOException, JAXBException {
        EventRepresentation event = createEvent();
        ActionRepresentation createAction2 = createAction();
        event.addAction(createAction2);
        EventRepresentation parentEvent = createEvent();
        parentEvent.addEvent(event);
        workflow.getRoot().addEvent(parentEvent);
        assertConversionResult(convertWorkflowToString(), "eventFollowingEvent");
    }

    @Test
    public void eventsFollowingEvent() throws SAXException, IOException, JAXBException {
        EndRepresentation end = new EndRepresentation();
        EventRepresentation event = createEvent();
        ActionRepresentation createAction2 = createAction(end);
        event.addAction(createAction2);
        EventRepresentation parentEvent = createEvent();
        parentEvent.addEvent(event);
        parentEvent.addEvent(event);
        workflow.getRoot().addEvent(parentEvent);
        assertConversionResult(convertWorkflowToString(), "eventsFollowingEvent");
    }

    private String convertWorkflowToString() throws JAXBException {
        WorkflowConverter converter = new DroolsConverter();
        String convert = converter.convert(workflow);
        System.out.println(convert);
        return convert;
    }

    private void assertConversionResult(String convert, String name) throws SAXException, IOException {
        XMLAssert.assertXMLEqual(new FileReader(this.getClass().getResource("/converted/" + name + ".xml").getPath()),
            new StringReader(convert));
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("convertertest.drl", getClass()), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newReaderResource(new StringReader(convert)), ResourceType.DRF);
        if (kbuilder.hasErrors()) {
            fail(kbuilder.getErrors().toString());
        }
    }

    private EventRepresentation createEvent() {
        EventRepresentation event = new EventRepresentation();
        event.setEvent(NullEvent.class);
        return event;
    }

    private ActionRepresentation createAction(EndRepresentation end) {
        ActionRepresentation createAction = createAction();
        createAction.setEnd(end);
        return createAction;
    }

    private ActionRepresentation createAction() {
        return setActionValues(new ActionRepresentation());
    }

    private ActionRepresentation setActionValues(ActionRepresentation root) {
        root.setDomain(NullDomain.class);
        root.setLocation("location");
        Method method = NullDomain.class.getMethods()[0];
        root.setMethodName(method.getName());
        root.setMethodParameters(Arrays.asList(method.getParameterTypes()));
        root.setCode("location.nullMethod();");
        return root;
    }
}
