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

package org.openengsb.core.workflow.internal.persistence;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EndRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;

/**
 * Converts a WorkflowRepresentation to xml using JAXB.
 */
public class WorkflowRepresentationConverter {

    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
    private JAXBContext context;

    public WorkflowRepresentationConverter() throws JAXBException {
        context = JAXBContext
            .newInstance(WorkflowRepresentation.class, ActionRepresentation.class,
                EventRepresentation.class,
                EndRepresentation.class);
        marshaller = context.createMarshaller();
        unmarshaller = context.createUnmarshaller();
    }

    public String marshallWorkflow(WorkflowRepresentation representation) {
        try {
            marshaller.setProperty("jaxb.formatted.output", true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(representation, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public WorkflowRepresentation unmarshallWorkflow(String workflow) {
        try {
            return (WorkflowRepresentation) unmarshaller.unmarshal(new StringReader(workflow));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
