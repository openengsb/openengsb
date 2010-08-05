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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.ui.web.model.MapModel;

@SuppressWarnings("serial")
public class EditorPanel extends Panel {

    public EditorPanel(String id, List<AttributeDefinition> attributes, Map<String, String> values) {
        super(id);
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
            row.add(new Label("name", a.getName()));
            TextField<String> tf = new TextField<String>("field", new MapModel<String, String>(values, a.getId()));
            row.add(tf);
        }
    }

    public void onSubmit() {
    }
}
