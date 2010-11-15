/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.l10n.PassThroughStringLocalizer;
import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.core.common.validation.SingleAttributeValidationResult;
import org.openengsb.core.common.validation.ValidationResultImpl;
import org.openengsb.ui.common.wicket.editor.NumberValidator;
import org.openengsb.ui.common.wicket.validation.DefaultPassingFormValidator;

@SuppressWarnings("serial")
public class EditorPanelTest {

    private WicketTester tester;
    private ServiceEditor editor;
    private Map<String, String> defaultValues;
    private AttributeDefinition attribOption;
    private AttributeDefinition attribBoolean;
    private final AttributeDefinition attrib = newAttribute("attrib", "name", "desc").build();
    private final AttributeDefinition numberAttrib = newAttribute("attrib", "name", "desc").validator(
        new NumberValidator()).build();
    private final AttributeDefinition attribNoDesc = newAttribute("attribNoDesc", "name", "").build();

    @Before
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        attribOption = newAttribute("attribOption", "option", "").option("label_a", "1").option("label_b", "2").build();
        attribBoolean = newAttribute("attribBool", "bool", "").asBoolean().build();
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
        tester.debugComponentTrees();
        formTester.setValue(buildFormComponentId(attrib.getId()), "new_value_a");
        formTester.submit();
        assertThat(editor.getValues().get(attrib.getId()), is("new_value_a"));
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
    public void checkCheckbox_shouldReturnTrueInModel() {
        startEditorPanel(attribBoolean);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        formTester.setValue(buildFormComponentId(attribBoolean.getId()), true);
        formTester.submit();
        assertThat(editor.getValues().get(attribBoolean.getId()), is("true"));
    }

    @Test
    public void putLetterIntoNumberField_shouldResultInError() throws Exception {

        startEditorPanel(numberAttrib);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        String buildFormComponentId = buildFormComponentId(numberAttrib.getId());
        formTester.setValue(buildFormComponentId, "A");
        tester.executeAjaxEvent(editor.getId() + ":form:submitButton", "onclick");
        tester.assertErrorMessages(new String[]{ "Number formating Error" });
    }

    @Test
    public void testValidateOnlyAfterSubmit() throws Exception {
        startEditorPanel(numberAttrib);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        String buildFormComponentId = buildFormComponentId(numberAttrib.getId());
        formTester.setValue(buildFormComponentId, "A");
        tester.executeAjaxEvent(editor.getId() + ":form:submitButton", "onclick");
        // tester.

    }

    @Test
    public void addValidatorToDropDownField_shouldReturnError() {
        testWithValidator(newAttribute("a", "name", "").option("a", "b").option("c", "d"));
    }

    @Test
    public void addValidatorToCheckboxField_shouldReturnError() {
        testWithValidator(newAttribute("a", "name", "").asBoolean());
    }

    @Test
    public void addValidatorToPasswordField_shouldReturnError() {
        testWithValidator(newAttribute("a", "name", "").asPassword());
    }

    private void testWithValidator(AttributeDefinition.Builder attributeDefinition) {
        AttributeDefinition a = attributeDefinition.validator(new FailValidator()).build();
        startEditorPanel(a);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        String buildFormComponentId = buildFormComponentId(a.getId());
        formTester.setValue(buildFormComponentId, "1");
        tester.executeAjaxEvent(editor.getId() + ":form:submitButton", "onclick");
        tester.assertErrorMessages(new String[]{ "Validation Error" });
    }

    @Test
    public void addFormValidator_ShouldExtractCorrectFormValues() {
        AttributeDefinition attrib1 = newAttribute("attrib1", "name1", "desc1").build();
        AttributeDefinition attrib2 = newAttribute("attrib2", "name2", "desc2").build();
        FormValidator validator = new FormValidator() {
            @Override
            public MultipleAttributeValidationResult validate(Map<String, String> attributes) {
                ArrayList<String> arrayList = new ArrayList<String>(attributes.keySet());
                Collections.sort(arrayList);
                Assert.assertEquals("attrib1", arrayList.get(0));
                Assert.assertEquals("attrib2", arrayList.get(1));
                Assert.assertEquals("a", attributes.get(arrayList.get(0)));
                Assert.assertEquals("b", attributes.get(arrayList.get(1)));

                Map<String, String> errorMessages = new HashMap<String, String>();
                for (String key : arrayList) {
                    errorMessages.put(key, "Validation Error");
                }
                return new MultipleAttributeValidationResultImpl(false, errorMessages);
            }

            @Override
            public List<String> fieldsToValidate() {
                return Arrays.asList(new String[]{ "attrib1", "attrib2" });
            }
        };
        startEditorPanel(validator, attrib1, attrib2);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        String component1Id = buildFormComponentId(attrib1.getId());
        String component2Id = buildFormComponentId(attrib2.getId());
        formTester.setValue(component1Id, "a");
        formTester.setValue(component2Id, "b");
        tester.executeAjaxEvent(editor.getId() + ":form:submitButton", "onclick");
        tester.assertErrorMessages(new String[]{ "Validation Error", "Validation Error" });
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void addFailFieldValidator_ShouldNotCallFormValidator() {
        AttributeDefinition attrib1 = newAttribute("attrib1", "name1", "desc1").validator(new FailValidator()).build();
        FormValidator mock = Mockito.mock(FormValidator.class);
        Mockito.when(mock.fieldsToValidate()).thenReturn(Arrays.asList(new String[]{ "attrib1" }));
        startEditorPanel(mock, attrib1);
        FormTester formTester = tester.newFormTester(editor.getId() + ":form");
        String component1Id = buildFormComponentId(attrib1.getId());
        formTester.setValue(component1Id, "a");
        tester.executeAjaxEvent(editor.getId() + ":form:submitButton", "onclick");
        Mockito.verify(mock, Mockito.never()).validate(Mockito.anyMap());
    }

    @Test
    public void startEditorPanel_ShouldHaveCheckedValidateCheckbox() {
        startEditorPanel(attrib);
        tester.assertModelValue(editor.getId() + ":form:attributesPanel:validate", true);
    }

    private AttributeDefinition.Builder newAttribute(String id, String name, String desc) {
        return AttributeDefinition.builder(new PassThroughStringLocalizer()).id(id).name(name).description(desc);
    }

    private void startEditorPanel(final AttributeDefinition... attributes) {
        this.startEditorPanel(new DefaultPassingFormValidator(), attributes);
    }

    private void startEditorPanel(final FormValidator validator, final AttributeDefinition... attributes) {
        final HashMap<String, String> values = new HashMap<String, String>();
        for (AttributeDefinition a : attributes) {
            values.put(a.getId(), a.getDefaultValue().getString(null));
        }
        defaultValues = new HashMap<String, String>(values);
        tester = new WicketTester();
        editor = (ServiceEditor) tester.startPanel(new TestPanelSource() {
            @Override
            public Panel getTestPanel(String panelId) {
                return new ServiceEditor(panelId, Arrays.asList(attributes), values, validator) {
                    @Override
                    public void onSubmit() {
                    }
                };
            }
        });
    }

    public String buildFormComponentId(String attributeId) {
        String string = "attributesPanel:fields:" + editor.getAttributeViewId(attributeId) + ":row:field";
        return string;
    }

    private static final class FailValidator implements FieldValidator {

        @Override
        public SingleAttributeValidationResult validate(String validate) {
            return new ValidationResultImpl(false, "validation.not");
        }

    }
}
