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

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.ui.web.model.MapModel;

@SuppressWarnings("serial")
public class EditorPanel extends Panel {

    private final Map<String, String> values;
    private final List<AttributeDefinition> attributes;

    public EditorPanel(String id, List<AttributeDefinition> attributes, Map<String, String> values) {
        super(id);
        this.attributes = attributes;
        this.values = values;
        createForm(attributes, values);
    }

    private void createForm(List<AttributeDefinition> attributes, Map<String, String> values) {
        @SuppressWarnings("rawtypes")
        Form<?> form = new Form("form") {
            @Override
            protected void onSubmit() {
                EditorPanel.this.onSubmit();
            }
        };
        add(form);

        form.add(new FeedbackPanel("feedback"));
        RepeatingView fields = new RepeatingView("fields");
        form.add(fields);

        for (AttributeDefinition a : attributes) {
            WebMarkupContainer row = new WebMarkupContainer(a.getId());
            fields.add(row);
            row.add(new EditorField("row", new MapModel<String, String>(values, a.getId()), a));
        }
    }

    public void onSubmit() {
    }

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public Map<String, String> getValues() {
        return values;
    }
}
