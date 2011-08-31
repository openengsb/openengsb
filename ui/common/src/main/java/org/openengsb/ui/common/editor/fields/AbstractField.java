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

package org.openengsb.ui.common.editor.fields;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.ui.common.model.LocalizableStringModel;

/**
 * Field intended for editing a property in a bean (e.g. a service).
 */
@SuppressWarnings("serial")
public abstract class AbstractField<T> extends Panel {

    public AbstractField(String id, IModel<String> model, AttributeDefinition attribute, IValidator<T> validator,
            boolean editable) {
        super(id);
        FormComponent<T> component = createFormComponent(attribute, model);
        if (validator != null) {
            component.add(validator);
        }
        component.setLabel(new LocalizableStringModel(this, attribute.getName()));
        component.setOutputMarkupId(true);
        component.setMarkupId(attribute.getId());
        component.setRequired(attribute.isRequired());
        component.setEnabled(editable);
        add(new SimpleFormComponentLabel("name", component).add(new SimpleAttributeModifier("for", attribute.getId())));
        add(component);
        addTooltip(attribute);
    }

    public AbstractField(String id, IModel<String> model, AttributeDefinition attribute, IValidator<T> validator) {
        this(id, model, attribute, validator, true);
    }

    private void addTooltip(AttributeDefinition attribute) {
        Image tooltip = new Image("tooltip", new ResourceReference(AbstractField.class, "balloon.png"));
        if (attribute.hasDescription()) {
            tooltip.add(new AttributeModifier("title", true, new LocalizableStringModel(this, attribute
                .getDescription())));
        } else {
            tooltip.setVisible(false);
        }
        add(tooltip);
    }

    protected abstract FormComponent<T> createFormComponent(AttributeDefinition attribute, IModel<String> model);
}
