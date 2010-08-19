/**

Copyright 2010 OpenEngSB Division, Vienna University of Technology

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package org.openengsb.ui.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.ui.web.editor.EditorPanel;

public class SendEventPageTest {

    private WicketTester tester;

    @Before
    public void setup() {
        tester = new WicketTester();
    }

    private class Dummy {

        private String testProperty;

        @SuppressWarnings("unused")
        public String getTestProperty() {
            return testProperty;
        }

        @SuppressWarnings("unused")
        public void setTestProperty(String testProperty) {
            this.testProperty = testProperty;
        }
    }

    private class Dummy2 {
    }

    @Test
    public void intialisationTest() {
        List<Class<?>> classes = Arrays.<Class<?>> asList(Dummy.class, Dummy2.class);
        tester.startPage(new SendEventPage(classes));
        @SuppressWarnings("unchecked")
        DropDownChoice<Class<?>> dropdown = (DropDownChoice<Class<?>>) tester
                .getComponentFromLastRenderedPage("dropdown");
        assertNotNull(dropdown);
        tester.assertComponent("dropdown", DropDownChoice.class);
        assertEquals(2, dropdown.getChoices().size());
        assertEquals(Dummy.class, dropdown.getChoices().get(0));
        assertEquals("Dummy", dropdown.getValue());
        assertEquals(Dummy2.class, dropdown.getChoices().get(1));
        tester.assertComponent("editor", EditorPanel.class);
        EditorPanel editorPanel = (EditorPanel) tester.getComponentFromLastRenderedPage("editor");
        final List<AttributeDefinition> attributes = editorPanel.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes.size(), 1);
        assertEquals(attributes.get(0).getName(), "testProperty");
    }
}
