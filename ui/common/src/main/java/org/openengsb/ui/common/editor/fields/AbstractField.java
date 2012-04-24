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

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.validation.IValidator;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.ui.common.editor.ModelFacade;
import org.openengsb.ui.common.model.LocalizableStringModel;

/**
 * Field intended for editing a property in a bean (e.g. a service).
 */
@SuppressWarnings("serial")
public abstract class AbstractField<T> extends Panel {
    private IModel<String> model;
    private AttributeDefinition attribute;
    private IValidator<T> validator;
    private boolean editable;

    public AbstractField(String id, IModel<String> model, AttributeDefinition attribute, IValidator<T> validator,
            boolean editable) {
        super(id);
        this.model = model;
        this.attribute = attribute;
        this.validator = validator;
        this.editable = editable;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        ModelFacade<T> component = createFormComponent(attribute, model);

        List<Component> helpComponents = component.getHelpComponents();
        if (helpComponents != null) {
            for (Component child : helpComponents) {
                add(child);
            }
        }
        FormComponent<T> mainComponent = component.getMainComponent();
        mainComponent.setOutputMarkupId(true);
        mainComponent.setMarkupId(attribute.getId());
        // editable is always set to true
        mainComponent.setEnabled(editable);
        if (validator != null) {
            mainComponent.add(validator);
        }
        mainComponent.setRequired(attribute.isRequired());
        mainComponent.setLabel(new LocalizableStringModel(this, attribute.getName()));
        SimpleFormComponentLabel label = new SimpleFormComponentLabel("name", mainComponent);
        label.add(AttributeModifier.replace("for", attribute.getId()));
        if (attribute.isRequired()) {
            label.add(AttributeModifier.replace("class", "required"));
        }
        add(label);
        add(mainComponent);
        addTooltip(attribute);
    }

    public AbstractField(String id, IModel<String> model, AttributeDefinition attribute, IValidator<T> validator) {
        this(id, model, attribute, validator, true);
    }

    private void addTooltip(AttributeDefinition attribute) {
        Image tooltip = new Image("tooltip", new PackageResourceReference(AbstractField.class, "balloon.png"));
        if (attribute.hasDescription()) {
            tooltip.add(new AttributeModifier("title", new LocalizableStringModel(this, attribute
                .getDescription())));
        } else {
            tooltip.setVisible(false);
        }
        add(tooltip);
    }

    protected abstract ModelFacade<T> createFormComponent(AttributeDefinition attribute, IModel<String> model);
}
