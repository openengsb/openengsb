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

package org.openengsb.ui.web.editor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.ui.web.validation.DefaultPassingFormValidator;

@SuppressWarnings("serial")
public abstract class ServiceEditorPanel extends Panel {

    private final Map<String, String> values;
    private final List<AttributeDefinition> attributes;
    private final FormValidator validator;
    private final Map<String, String> attributeViewIds = new HashMap<String, String>();

    public ServiceEditorPanel(String id, List<AttributeDefinition> attributes, Map<String, String> values) {
        this(id, attributes, values, new DefaultPassingFormValidator());
    }

    public ServiceEditorPanel(String id, List<AttributeDefinition> attributes, Map<String, String> values,
            FormValidator validator) {
        super(id);
        this.attributes = attributes;
        this.values = values;
        this.validator = validator;
        createForm(attributes, values);
    }

    private void createForm(List<AttributeDefinition> attributes, Map<String, String> values) {
        @SuppressWarnings("rawtypes")
        final Form<?> form = new Form("form");
        add(form);

        form.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
        attributeViewIds.clear();
        RepeatingView fields = EditorFieldFactory.createFieldList("fields", attributes, values, attributeViewIds);
        form.add(fields);
        CheckBox checkbox = new CheckBox("validate", new Model<Boolean>(true));
        form.add(checkbox);
        if (validator != null) {
            form.add(new AbstractFormValidator() {

                @Override
                public void validate(Form<?> form) {
                    Map<String, FormComponent<?>> loadFormComponents = loadFormComponents(form);
                    Map<String, String> toValidate = new HashMap<String, String>();
                    for (Map.Entry<String, FormComponent<?>> entry : loadFormComponents.entrySet()) {
                        toValidate.put(entry.getKey(), entry.getValue().getValue());
                    }
                    MultipleAttributeValidationResult validate = validator.validate(toValidate);
                    if (!validate.isValid()) {
                        Map<String, String> attributeErrorMessages = validate.getAttributeErrorMessages();
                        for (Map.Entry<String, String> entry : attributeErrorMessages.entrySet()) {
                            FormComponent<?> fc = loadFormComponents.get(entry.getKey());
                            fc.error((IValidationError) new ValidationError().setMessage(entry.getValue()));
                        }
                    }
                }

                @Override
                public FormComponent<?>[] getDependentFormComponents() {
                    Collection<FormComponent<?>> formComponents = loadFormComponents(form).values();
                    return formComponents.toArray(new FormComponent<?>[formComponents.size()]);
                }

                private Map<String, FormComponent<?>> loadFormComponents(final Form<?> form) {
                    Map<String, FormComponent<?>> formComponents = new HashMap<String, FormComponent<?>>();
                    if (validator != null) {
                        for (String attribute : validator.fieldsToValidate()) {
                            Component component = form.get("fields:" + getAttributeViewId(attribute) + ":row:field");
                            if (component instanceof FormComponent<?>) {
                                formComponents.put(attribute, (FormComponent<?>) component);
                            }
                        }
                    }
                    return formComponents;
                }
            });
        }
        // form.add(new Button("submitButton"));
        AjaxButton submitButton = new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ServiceEditorPanel.this.onSubmit();
                if (hasErrorMessage()) {
                    addAjaxValidationToForm(form);
                    target.addComponent(form);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                addAjaxValidationToForm(form);
                target.addComponent(form);
            }
        };
        form.setOutputMarkupId(true);
        form.add(submitButton);
    }

    private void addAjaxValidationToForm(Form<?> form) {
        AjaxFormValidatingBehavior.addToAllFormComponents(form, "onBlur");
        AjaxFormValidatingBehavior.addToAllFormComponents(form, "onChange");
    }

    public abstract void onSubmit();

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public String getAttributeViewId(String attribute) {
        return attributeViewIds.get(attribute);
    }
}
