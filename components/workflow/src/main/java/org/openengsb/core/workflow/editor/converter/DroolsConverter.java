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

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.openengsb.core.api.workflow.WorkflowConverter;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.core.workflow.editor.converter.Process.Connection;
import org.openengsb.core.workflow.editor.converter.Process.EndNode;
import org.openengsb.core.workflow.editor.converter.Process.Start;

public class DroolsConverter implements WorkflowConverter {

    private final JAXBContext context;

    private final Marshaller marshaller;

    public DroolsConverter() throws JAXBException {
        context =
            JAXBContext.newInstance(ActionNode.class, ActionEvent.class, Process.class, Connection.class, Start.class,
                org.openengsb.core.workflow.editor.converter.ActionNode.Action.class, EndNode.class);
        this.marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.schemaLocation",
            "http://drools.org/drools-5.0/process drools-processes-5.0.xsd");
    }

    @Override
    public String convert(WorkflowRepresentation workflow) {
        Process process = Process.build(workflow);
        StringWriter writer = new StringWriter();
        try {
            marshaller.marshal(process, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

}
