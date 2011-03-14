package org.openengsb.core.workflow.editor.converter;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.openengsb.core.workflow.editor.Workflow;
import org.openengsb.core.workflow.editor.WorkflowConverter;
import org.openengsb.core.workflow.editor.converter.Process.End;
import org.openengsb.core.workflow.editor.converter.Process.Start;

public class DroolsConverter implements WorkflowConverter {

    private final JAXBContext context;

    private final Marshaller marshaller;

    public DroolsConverter() throws JAXBException {
        context =
            JAXBContext.newInstance(ActionNode.class, EventNode.class, Process.class, Connection.class, Start.class,
                org.openengsb.core.workflow.editor.converter.ActionNode.Action.class, End.class);
        this.marshaller = context.createMarshaller();
    }

    @Override
    public String convert(Workflow workflow) {
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
