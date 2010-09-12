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
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.ui.web.editor.fields.AbstractField;
import org.openengsb.ui.web.validation.NumberValidator;

@SuppressWarnings("serial")
public class EditorPanelTest {


    private WicketTester tester;
    private EditorPanel editor;
    private Map<String, String> defaultValues;
    private AttributeDefinition attribOption;
    private AttributeDefinition attribBoolean;
    private final AttributeDefinition attrib = newAttribute("attrib", "name", "desc");
    private final AttributeDefinition attribNoDesc = newAttribute("attribNoDesc", "name", "");

    @Before
    public void setup() {
        attribOption = newAttribute("attribOption", "option", "");
        attribOption.addOption("label_a", "1");
        attribOption.addOption("label_b", "2");
        attribBoolean = newAttribute("attribBool", "bool", "");
        attribBoolean.setBoolean(true);
    }

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
    @Ignore("empty string in model gets replaced with null, why is this happening")
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
        formTester.setValue(buildFormComponentId(attrib.getId()), "new_value_a");
        formTester.submit();
        assertThat(editor.getValues().get(attrib.getId()), is("new_value_a"));
    }

    @Test
    public void optionAttribute_shouldBeDisplayedAsDropDown() {
        startEditorPanel(attribOption);
        DropDownChoice<?> choice = getEditorFieldFormComponent(attribOption.getId(), DropDownChoice.class);
        assertThat(choice.getChoices().size(), is(attribOption.getOptions().size()));
    }

    @Test
    public void choicesInDropDownChoice_shouldBeInSameOrderAsOptionAttribute() {
        startEditorPanel(attribOption);
        @SuppressWarnings("unchecked")
        List<String> choice = getEditorFieldFormComponent(attribOption.getId(), DropDownChoice.class)
                .getChoices();
        for (int i=0; i < attribOption.getOptions().size(); ++i) {
            assertThat(choice.get(i), is(attribOption.getOptions().get(i).getValue()));
        }
    }

    @Test
    public void selectLabelInDropDownChoice_shouldSetRightValueInModel() {
        startEditorPanel(attribOption);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        formTester.select(buildFormComponentId(attribOption.getId()), 1);
        formTester.submit();
        assertThat(editor.getValues().get(attribOption.getId()), is(attribOption.getOptions().get(1).getValue()));
    }

    @Test
    public void boolAttribute_shouldBeDisplayedAsCheckBox() {
        startEditorPanel(attribBoolean);
        CheckBox cb = getEditorFieldFormComponent(attribBoolean.getId(), CheckBox.class);
        assertThat(cb, notNullValue());
    }

    @Test
    public void checkCheckbox_shouldReturnTrueInModel() {
        startEditorPanel(attribBoolean);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        formTester.setValue(buildFormComponentId(attribBoolean.getId()), true);
        formTester.submit();
        assertThat(editor.getValues().get(attribBoolean.getId()), is("true"));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void putLetterIntoNumberField_shouldResultInError() throws Exception {
        FieldValidator validator = new NumberValidator();
        attrib.setValidator(validator);
        startEditorPanel(attrib);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        String buildFormComponentId = buildFormComponentId(attrib.getId());
        formTester.setValue(buildFormComponentId, "A");
        tester.executeAjaxEvent(editor.getId() + ":form:" + buildFormComponentId, "onBlur");
        tester.assertErrorMessages(new String[] {"Number formating Error"});
    }

    private AttributeDefinition newAttribute(String id, String name, String desc) {
        AttributeDefinition a = new AttributeDefinition();
        a.setId(id);
        a.setName(name);
        a.setDescription(desc);
        return a;
    }

    private void startEditorPanel(final AttributeDefinition... attributes) {
        final HashMap<String, String> values = new HashMap<String, String>();
        for (AttributeDefinition a : attributes) {
            values.put(a.getId(), a.getDefaultValue());
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
        String id = editor.getId() + ":form:" + buildFormComponentId(attributeId);
        Component c = tester.getComponentFromLastRenderedPage(id);
        assertThat(c, notNullValue());
        assertThat(c, is(componentType));
        return (T) c;
    }

    public static String buildFormComponentId(String attributeId) {
        return "fields:" + attributeId + ":row:field";
    }

    private AbstractField getEditorField(String attributeId) {
        return (AbstractField) getEditorFieldFormComponent(attributeId, FormComponent.class).getParent();
    }

    private void setFormValue(String attributeId, String value) {
        tester.getServletRequest().setParameter(
                getEditorFieldFormComponent(attributeId, FormComponent.class).getInputName(), value);
    }
}
