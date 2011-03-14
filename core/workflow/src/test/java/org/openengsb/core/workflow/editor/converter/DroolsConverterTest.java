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
    public void callWorkflow_ShouldConvertCorrectly() throws SAXException, IOException,
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
