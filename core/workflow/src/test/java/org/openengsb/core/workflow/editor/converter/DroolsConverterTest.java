package org.openengsb.core.workflow.editor.converter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.openengsb.core.workflow.editor.Action;
import org.openengsb.core.workflow.editor.Event;
import org.openengsb.core.workflow.editor.Workflow;
import org.openengsb.core.workflow.editor.WorkflowConverter;
import org.xml.sax.SAXException;

public class DroolsConverterTest {

    @Test
    public void callWorkflow_ShouldConvertCorrectly() throws FileNotFoundException, SAXException, IOException,
        JAXBException {
        Workflow workflow = new Workflow();
        workflow.setName("workflow");
        Action root = workflow.getRoot();
        setActionValues(root);
        Event event = new Event();
        event.setEvent(NullEvent.class);
        event.addAction(setActionValues(new Action()));
        root.addEvent(event);
        root.addAction(setActionValues(new Action()));
        WorkflowConverter converter = new DroolsConverter();
        String convert = converter.convert(workflow);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(new FileReader(this.getClass().getResource("/test-workflow.xml").getPath()),
            new StringReader(convert));
    }

    private Action setActionValues(Action root) {
        root.setDomain(NullDomain.class);
        root.setLocation("location");
        Method method = NullDomain.class.getMethods()[0];
        root.setMethodName(method.getName());
        root.setMethodParameters(Arrays.asList(method.getParameterTypes()));
        return root;
    }
}
