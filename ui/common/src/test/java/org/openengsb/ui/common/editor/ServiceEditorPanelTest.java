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

package org.openengsb.ui.common.editor;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.l10n.PassThroughStringLocalizer;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.ui.common.editor.fields.AbstractField;
import org.openengsb.ui.common.validation.DefaultPassingFormValidator;

@SuppressWarnings("serial")
public class ServiceEditorPanelTest {

    private WicketTester tester;
    private ServiceEditorPanel editor;
    private Map<String, String> defaultValues;
    private AttributeDefinition attribOption;
    private AttributeDefinition attribBoolean;
    private final AttributeDefinition attrib = newAttribute("attrib", "name", "desc").build();
    private final AttributeDefinition attribNoDesc = newAttribute("attribNoDesc", "name", "").build();

    @Before
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        attribOption = newAttribute("attribOption", "option", "").option("label_a", "1").option("label_b", "2").build();
        attribBoolean = newAttribute("attribBool", "bool", "").asBoolean().build();
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
    public void optionAttribute_shouldBeDisplayedAsDropDown() {
        startEditorPanel(attribOption);
        DropDownChoice<?> choice = getEditorFieldFormComponent(attribOption.getId(), DropDownChoice.class);
        assertThat(choice.getChoices().size(), is(attribOption.getOptions().size()));
    }

    @Test
    public void choicesInDropDownChoice_shouldBeInSameOrderAsOptionAttribute() {
        startEditorPanel(attribOption);
        @SuppressWarnings("unchecked")
        List<String> choice = getEditorFieldFormComponent(attribOption.getId(), DropDownChoice.class).getChoices();
        for (int i = 0; i < attribOption.getOptions().size(); ++i) {
            assertThat(choice.get(i), is(attribOption.getOptions().get(i).getValue()));
        }
    }

    @Test
    public void boolAttribute_shouldBeDisplayedAsCheckBox() {
        startEditorPanel(attribBoolean);
        CheckBox cb = getEditorFieldFormComponent(attribBoolean.getId(), CheckBox.class);
        assertThat(cb, notNullValue());
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
        editor = (ServiceEditorPanel) tester.startPanel(new TestPanelSource() {
            @Override
            public Panel getTestPanel(String panelId) {
                return new ServiceEditorPanel(panelId, Arrays.asList(attributes), values);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T getEditorFieldFormComponent(String attributeId, Class<T> componentType) {
        String id = editor.getId() + ":" + buildFormComponentId(attributeId);
        Component c = tester.getComponentFromLastRenderedPage(id);
        assertThat(c, notNullValue());
        assertThat(c, is(componentType));
        return (T) c;
    }

    public String buildFormComponentId(String attributeId) {
        return "fields:" + editor.getAttributeViewId(attributeId) + ":row:field";
    }

    private AbstractField<?> getEditorField(String attributeId) {
        return (AbstractField<?>) getEditorFieldFormComponent(attributeId, FormComponent.class).getParent();
    }
}
