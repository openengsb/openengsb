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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.ui.web.editor.EditorPanel;

public class SendEventPageTest {

    private WicketTester tester;
    private EditorPanel editorPanel;
    private DropDownChoice<Class<?>> dropdown;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        tester = new WicketTester();
        List<Class<?>> classes = Arrays.<Class<?>> asList(Dummy.class, Dummy2.class);
        tester.startPage(new SendEventPage(classes));
        editorPanel = (EditorPanel) tester.getComponentFromLastRenderedPage("editor");
        dropdown = (DropDownChoice<Class<?>>) tester
                .getComponentFromLastRenderedPage("form:dropdown");
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

    @SuppressWarnings("unused")
    private class Dummy2 {

        private String firstProperty;
        private String secondProperty;

        public String getFirstProperty() {
            return firstProperty;
        }

        public void setFirstProperty(String firstProperty) {
            this.firstProperty = firstProperty;
        }

        public String getSecondProperty() {
            return secondProperty;
        }

        public void setSecondProperty(String secondProperty) {
            this.secondProperty = secondProperty;
        }
    }

    @Test
    public void testStandardPageComponents() throws Exception {
        tester.assertVisible("form:dropdown");
        tester.assertVisible("editor");
        assertThat(dropdown, notNullValue());
        assertThat(editorPanel, notNullValue());
    }

    @Test
    public void givenTwoClassesInCtor_shouldAddThemToTheDropDown() {
        assertEquals(2, dropdown.getChoices().size());
        assertEquals(Dummy.class, dropdown.getChoices().get(0));
        assertEquals("Dummy", dropdown.getValue());
        assertEquals(Dummy2.class, dropdown.getChoices().get(1));
    }

    @Test
    public void firstClassIsDefault_shouldCreateEditorFieldsBasedOnDefault() {
        final List<AttributeDefinition> attributes = editorPanel.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes.size(), 1);
        assertEquals(attributes.get(0).getName(), "testProperty");
    }

    @Test
    public void selectNewClassInDropDown_shouldRenderNewEditorPanelThroughAjax() throws Exception {
        FormTester formTester = tester.newFormTester("form");
        formTester.select("dropdown", 1);
        tester.executeAjaxEvent(dropdown, "onchange");
        List<AttributeDefinition> attributes = ((EditorPanel) tester.getComponentFromLastRenderedPage("editor")).getAttributes();
        assertThat(attributes.size(), is(2));
        assertThat(attributes.get(0).getName(), is("firstProperty"));
    }
}
