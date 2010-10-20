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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.SingleAttributeValidationResult;
import org.openengsb.ui.web.editor.fields.AbstractField;
import org.openengsb.ui.web.editor.fields.CheckboxField;
import org.openengsb.ui.web.editor.fields.DropdownField;
import org.openengsb.ui.web.editor.fields.InputField;
import org.openengsb.ui.web.editor.fields.PasswordField;
import org.openengsb.ui.web.model.MapModel;
import org.openengsb.ui.web.validation.DefaultPassingFormValidator;

@SuppressWarnings("serial")
public abstract class EditorPanel extends Panel {

    private final Map<String, String> values;
    private final List<AttributeDefinition> attributes;
    private final FormValidator validator;

    public EditorPanel(String id, List<AttributeDefinition> attributes, Map<String, String> values) {
        this(id, attributes, values, new DefaultPassingFormValidator());
    }

    public EditorPanel(String id, List<AttributeDefinition> attributes, Map<String, String> values,
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
        RepeatingView fields = new RepeatingView("fields");
        form.add(fields);

        for (AttributeDefinition a : attributes) {
            WebMarkupContainer row = new WebMarkupContainer(a.getId());
            fields.add(row);
            row.add(createEditor("row", new MapModel<String, String>(values, a.getId()), a));
        }
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
                            Component component = form.get("fields:" + attribute + ":row:field");
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
                EditorPanel.this.onSubmit();
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

    private AbstractField<?> createEditor(String id, IModel<String> model, final AttributeDefinition attribute) {
        if (attribute.isBoolean()) {
            return new CheckboxField(id, model, attribute, new BooleanFieldValidator(attribute));
        }
        StringFieldValidator validator = new StringFieldValidator(attribute);
        if (!attribute.getOptions().isEmpty()) {
            return new DropdownField(id, model, attribute, validator);
        } else if (attribute.isPassword()) {
            return new PasswordField(id, model, attribute, validator);
        } else {
            return new InputField(id, model, attribute, validator);
        }
    }

    public abstract void onSubmit();

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public Map<String, String> getValues() {
        return values;
    }

    private abstract static class EditorFieldValidator<T> extends AbstractValidator<T> {
        private final AttributeDefinition attribute;

        protected EditorFieldValidator(AttributeDefinition attribute) {
            this.attribute = attribute;
        }

        @Override
        protected void onValidate(IValidatable<T> validatable) {
            FieldValidator validator = this.attribute.getValidator();
            SingleAttributeValidationResult validationResult = validator.validate(validatable.getValue().toString());
            if (!validationResult.isValid()) {
                error(validatable, validationResult.getErrorMessageId());
            }
        }
    }

    private static final class BooleanFieldValidator extends EditorFieldValidator<Boolean> {
        private BooleanFieldValidator(AttributeDefinition attribute) {
            super(attribute);
        }
    }

    private static final class StringFieldValidator extends EditorFieldValidator<String> {
        private StringFieldValidator(AttributeDefinition attribute) {
            super(attribute);
        }
    }
}
