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
package org.openengsb.ui.web.editor;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.core.config.descriptor.AttributeDefinition;

@SuppressWarnings("serial")
public class EditorTest {

    private final AttributeDefinition attrib = AttributeDefinition.builder().id("attrib").name("name")
            .description("desc").build();
    private final AttributeDefinition attribNoDesc = AttributeDefinition.builder().id("attribNoDesc").name("name")
            .build();
    private WicketTester tester;
    private EditorPanel editor;
    private Map<String, String> defaultValues;

    @Test
    public void editingStringAttribute_shouldRenderTextFieldWithPresetValues() throws Exception {
        startEditorPanel(attrib);
        TextField<?> tf = getEditorFieldFormComponent(attrib.getId(), TextField.class);
        assertThat(tf.getValue(), is(defaultValues.get(attrib.getId())));
    }

    @Test
    public void attributeWithDescription_shouldRenderTooltipImageWithTitle() throws Exception {
        startEditorPanel(attrib);
        assertThat(((Image) getEditorField(attrib.getId()).get("tooltip")).isVisible(), is(true));
    }

    @Test
    public void attributeWithoutDescription_shouldShowNoTooltipImage() throws Exception {
        startEditorPanel(attribNoDesc);
        assertThat(getEditorField(attribNoDesc.getId()).get("tooltip").isVisible(), is(false));
    }

    @Test
    public void submittingFormWithoutChange_shouldReturnInitialValues() throws Exception {
        startEditorPanel(attrib, attribNoDesc);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        formTester.submit();
        assertThat(editor.getValues(), is(defaultValues));
    }

    @Test
    public void submittingFormWithChanges_shouldReflectChangesInValues() throws Exception {
        startEditorPanel(attrib);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        setFormValue(attrib.getId(), "new_value_a");
        formTester.submit();
        assertThat(editor.getValues().get(attrib.getId()), is("new_value_a"));
    }

    @Test
    @Ignore
    public void optionAttribute_shouldBeDisplayedAsOptionChoice() {

    }

    private void startEditorPanel(final AttributeDefinition... attributes) {
        final HashMap<String, String> values = new HashMap<String, String>();
        for (AttributeDefinition a : attributes) {
            values.put(a.getId(), a.getId() + "_default");
        }
        defaultValues = new HashMap<String, String>(values);
        tester = new WicketTester();
        editor = (EditorPanel) tester.startPanel(new TestPanelSource() {
            @Override
            public Panel getTestPanel(String panelId) {
                return new EditorPanel(panelId, Arrays.asList(attributes), values);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T getEditorFieldFormComponent(String attributeId, Class<T> componentType) {
        String id = editor.getId() + ':' + buildFormComponentId(attributeId);
        Component c = tester.getComponentFromLastRenderedPage(id);
        assertThat(c, notNullValue());
        assertThat(c, is(componentType));
        return (T) c;
    }

    public static String buildFormComponentId(String attributeId) {
        return "form:fields:" + attributeId + ":row:field";
    }

    private EditorField getEditorField(String attributeId) {
        return (EditorField) getEditorFieldFormComponent(attributeId, FormComponent.class).getParent();
    }

    private void setFormValue(String attributeId, String value) {
        tester.getServletRequest().setParameter(
                getEditorFieldFormComponent(attributeId, FormComponent.class).getInputName(), value);
    }
}
