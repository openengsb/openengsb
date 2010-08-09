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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.config.descriptor.AttributeDefinition;

@SuppressWarnings("serial")
public class EditorTest {

    private WicketTester tester;
    private EditorPanel editor;
    private AttributeDefinition stringAttrib;
    private AttributeDefinition attribWithNoDescription;
    private Map<String, String> defaultValues;

    @Before
    public void setup() {
        stringAttrib = AttributeDefinition.builder().id("id_a").name("name_a").description("desc_a").build();
        attribWithNoDescription = AttributeDefinition.builder().id("id_b").name("name_b").build();
        final Map<String, String> values = new HashMap<String, String>();
        values.put(stringAttrib.getId(), stringAttrib.getId() + "_default");
        values.put(attribWithNoDescription.getId(), attribWithNoDescription.getId() + "_default");
        defaultValues = Collections.unmodifiableMap(values);
        tester = new WicketTester();
        editor = (EditorPanel) tester.startPanel(new TestPanelSource() {
            @Override
            public Panel getTestPanel(String panelId) {
                return new EditorPanel(panelId, Arrays.asList(stringAttrib, attribWithNoDescription), values);
            }
        });
    }

    @Test
    public void editingStringAttribute_shouldRenderTextFieldWithPresetValues() throws Exception {
        TextField<?> tf = getEditorFieldFormComponent(editor, stringAttrib.getId(), TextField.class);
        assertThat(tf.getValue(), is(defaultValues.get(stringAttrib.getId())));
    }

    @Test
    public void attributeWithDescription_shouldRenderTooltipImageWithTitle() throws Exception {
        assertThat(((Image) getEditorField(editor, stringAttrib.getId()).get("tooltip")).isVisible(), is(true));
    }

    @Test
    public void attributeWithoutDescription_shouldShowNoTooltipImage() throws Exception {
        assertThat(getEditorField(editor, attribWithNoDescription.getId()).get("tooltip").isVisible(), is(false));
    }

    // because of the dynamics of the editor, we have to look up the fields
    // another way
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> T getEditorFieldFormComponent(EditorPanel editor, String attributeId, Class<T> componentType) {
        Form<?> form = (Form<?>) editor.get("form");
        MarkupContainer fields = (MarkupContainer) form.get(1);
        for (int i = 0; i < fields.size(); ++i) {
            MarkupContainer row = (MarkupContainer) fields.get(i);
            FormComponent c = (FormComponent) ((Panel) row.get(0)).get(1);
            if (c.getInputName().equals("fields:" + attributeId + ":row:field")) {
                assertThat(c, is(componentType));
                return (T) c;
            }
        }
        fail("no form component '" + attributeId + "' found");
        throw new UnsupportedOperationException();
    }

    private static EditorField getEditorField(EditorPanel editor, String attributeId) {
        return (EditorField) getEditorFieldFormComponent(editor, attributeId, FormComponent.class).getParent();
    }
}
