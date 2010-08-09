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

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.openengsb.core.config.descriptor.AttributeDefinition;

@SuppressWarnings("serial")
public class EditorField extends Panel {

    public EditorField(String id, IModel<String> model, AttributeDefinition attribute) {
        super(id);
        TextField<String> tf = new TextField<String>("field", model);
        tf.setLabel(new Model<String>(attribute.getName()));
        tf.setOutputMarkupId(true);
        tf.setMarkupId(attribute.getId());
        add(new SimpleFormComponentLabel("name", tf).add(new SimpleAttributeModifier("for", attribute.getId())));
        add(tf);
        Image tooltip = new Image("tooltip");
        if (attribute.hasDescription()) {
            tooltip.add(new SimpleAttributeModifier("title", attribute.getDescription()));
        } else {
            tooltip.setVisible(false);
        }
        add(tooltip);
    }

}
