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
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.l10n.PassThroughStringLocalizer;
import org.openengsb.ui.common.editor.fields.AbstractField;

public class ServiceEditorPanelTest {

    private WicketTester tester;
    private ServiceEditorPanel editor;
    private Map<String, String> defaultValues;
    private AttributeDefinition attribOption;
    private AttributeDefinition attribBoolean;
    private final AttributeDefinition attrib = newAttribute("attrib", "name", "desc").build();
    private final AttributeDefinition attribNoDesc = newAttribute("attribNoDesc", "name", "").build();
    private Map<String, String> editorValues;

    @Before
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        tester = new WicketTester();
        attribOption = newAttribute("attribOption", "option", "").option("label_a", "1").option("label_b", "2").build();
        attribBoolean = newAttribute("attribBool", "bool", "").asBoolean().build();
    }

    @Test
    public void testEditingStringAttribute_shouldRenderTextFieldWithPresetValues() throws Exception {
        startEditorPanel(attrib);
        tester.debugComponentTrees();
        TextField<?> tf = getEditorFieldFormComponent(attrib.getId(), TextField.class);
        assertThat(tf.getValue(), is(defaultValues.get(attrib.getId())));
    }

    @Test
    public void testAttributeWithDescription_shouldRenderTooltipImageWithTitle() throws Exception {
        startEditorPanel(attrib);
        assertThat(((Image) getEditorField(attrib.getId()).get("tooltip")).isVisible(), is(true));
    }

    @Test
    public void testAttributeWithoutDescription_shouldShowNoTooltipImage() throws Exception {
        startEditorPanel(attribNoDesc);
        assertThat(getEditorField(attribNoDesc.getId()).get("tooltip").isVisible(), is(false));
    }

    @Test
    public void testOptionAttribute_shouldBeDisplayedAsDropDown() throws Exception {
        startEditorPanel(attribOption);
        DropDownChoice<?> choice = getEditorFieldFormComponent(attribOption.getId(), DropDownChoice.class);
        assertThat(choice.getChoices().size(), is(attribOption.getOptions().size()));
    }

    @Test
    public void testChoicesInDropDownChoice_shouldBeInSameOrderAsOptionAttribute() throws Exception {
        startEditorPanel(attribOption);
        @SuppressWarnings("unchecked")
        List<String> choice = getEditorFieldFormComponent(attribOption.getId(), DropDownChoice.class).getChoices();
        for (int i = 0; i < attribOption.getOptions().size(); ++i) {
            assertThat(choice.get(i), is(attribOption.getOptions().get(i).getValue()));
        }
    }

    @Test
    public void testBoolAttribute_shouldBeDisplayedAsCheckBox() throws Exception {
        startEditorPanel(attribBoolean);
        CheckBox cb = getEditorFieldFormComponent(attribBoolean.getId(), CheckBox.class);
        assertThat(cb, notNullValue());
    }

    @Test
    public void testContainsInitialPropertiesFields_shouldContainProperties() throws Exception {
        Map<String, Object> props = new Hashtable<String, Object>();
        props.put("testpropx", "42");
        props.put("foo", "bar");
        startEditorPanel(props, attribOption);

        Label label1 = (Label) tester.getComponentFromLastRenderedPage("panel:properties:0:key");
        assertThat(label1.getDefaultModelObjectAsString(), is("foo"));
        Component comp = tester.getComponentFromLastRenderedPage("panel:properties:0:values:1:value");
        @SuppressWarnings("unchecked")
        AjaxEditableLabel<String> value1 = (AjaxEditableLabel<String>) comp;
        assertThat((String) value1.getDefaultModelObject(), is("bar"));

        Label label2 = (Label) tester.getComponentFromLastRenderedPage("panel:properties:1:key");
        assertThat(label2.getDefaultModelObjectAsString(), is("testpropx"));
        @SuppressWarnings("unchecked")
        AjaxEditableLabel<String> value2 =
            (AjaxEditableLabel<String>) tester.getComponentFromLastRenderedPage("panel:properties:1:values:1:value");
        assertThat((String) value2.getDefaultModelObject(), is("42"));
    }

    @Test
    public void testContainsInitialPropertiesFieldsWithArray_shouldContainProperties() throws Exception {
        Map<String, Object> props = new Hashtable<String, Object>();
        props.put("testpropx", new String[]{ "42", "foo" });
        startEditorPanel(props, attribOption);

        Label label1 = (Label) tester.getComponentFromLastRenderedPage("panel:properties:0:key");
        assertThat(label1.getDefaultModelObjectAsString(), is("testpropx"));
        tester.debugComponentTrees();
        @SuppressWarnings("unchecked")
        AjaxEditableLabel<String> value1 =
            (AjaxEditableLabel<String>) tester.getComponentFromLastRenderedPage("panel:properties:0:values:1:value");
        assertThat((String) value1.getDefaultModelObject(), is("42"));

        @SuppressWarnings("unchecked")
        AjaxEditableLabel<String> value2 =
            (AjaxEditableLabel<String>) tester.getComponentFromLastRenderedPage("panel:properties:0:values:2:value");
        assertThat((String) value2.getDefaultModelObject(), is("foo"));
    }
    
    @Test
    public void testDeleteProperty_shouldWork() throws Exception {
        Map<String, Object> props = new Hashtable<String, Object>();
        props.put("testpropx", new String[]{ "42", "foo" });
        props.put("testpropy", new String[]{ "ping", "pong"});
        startEditorPanel(props, attribOption);
        String path = "panel:properties:0:key";
        Label label1 = (Label) tester.getComponentFromLastRenderedPage(path);
        String before = label1.getDefaultModelObjectAsString();
        tester.executeAjaxEvent("panel:properties:0:buttonKey", "onclick");
        label1 = (Label) tester.getComponentFromLastRenderedPage(path);
        String after = label1.getDefaultModelObjectAsString();
        
        assertThat(before, is("testpropx"));
        assertThat(after, is("testpropy"));
    }

    @Test
    public void testDeletePropertyValue_shouldWork() throws Exception {
        Map<String, Object> props = new Hashtable<String, Object>();
        props.put("testpropx", new String[]{ "42", "foo" });
        startEditorPanel(props, attribOption);
        
        String path = "panel:properties:0:values:1:value";
        @SuppressWarnings("unchecked")
        AjaxEditableLabel<String> value1 =
            (AjaxEditableLabel<String>) tester.getComponentFromLastRenderedPage(path);
        String before = (String) value1.getDefaultModelObject();
        tester.executeAjaxEvent("panel:properties:0:values:1:buttonValue", "onclick");
        
        @SuppressWarnings("unchecked")
        AjaxEditableLabel<String> value2 =
            (AjaxEditableLabel<String>) tester.getComponentFromLastRenderedPage(path);
        String after = (String) value2.getDefaultModelObject();

        assertThat(before, is("42"));
        assertThat(after, is("foo"));
    }

    private AttributeDefinition.Builder newAttribute(String id, String name, String desc) {
        return AttributeDefinition.builder(new PassThroughStringLocalizer()).id(id).name(name).description(desc);
    }

    private void startEditorPanel(final Map<String, Object> properties,
            final AttributeDefinition... attributes) {
        editorValues = new HashMap<String, String>();
        defaultValues = new HashMap<String, String>();
        for (AttributeDefinition a : attributes) {
            editorValues.put(a.getId(), a.getDefaultValue().getString(Locale.ENGLISH));
            defaultValues.put(a.getId(), a.getDefaultValue().getString(Locale.ENGLISH));
        }
        editor =
            tester.startComponentInPage(new ServiceEditorPanel("panel", Arrays.asList(attributes),
                editorValues, properties,
                mock(Form.class)));
    }

    private void startEditorPanel(final AttributeDefinition... attributes) {
        startEditorPanel(new Hashtable<String, Object>(), attributes);
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
        return "fields:" + attributeId + ":row:field";
    }

    private AbstractField<?> getEditorField(String attributeId) {
        return (AbstractField<?>) getEditorFieldFormComponent(attributeId, FormComponent.class).getParent();
    }
}
