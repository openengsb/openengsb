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

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.SingleAttributeValidationResult;
import org.openengsb.ui.web.MethodUtil;
import org.openengsb.ui.web.editor.fields.AbstractField;
import org.openengsb.ui.web.editor.fields.CheckboxField;
import org.openengsb.ui.web.editor.fields.DropdownField;
import org.openengsb.ui.web.editor.fields.InputField;
import org.openengsb.ui.web.editor.fields.PasswordField;
import org.openengsb.ui.web.model.MapModel;

public final class EditorFieldFactory {
    private EditorFieldFactory() {
    }

    /**
     * creates a RepeatingView providing a suitable editor field for every property.
     *
     * @param values map used for saving the data @see org.openengsb.ui.web.model.MapModel
     */
    public static RepeatingView createFieldList(String id, Class<?> bean, Map<String, String> values) {
        List<AttributeDefinition> attributes = MethodUtil.buildAttributesList(bean);
        return createFieldList(id, attributes, values);
    }

    /**
     * creates a RepeatingView providing a suitable editor field for every attribute in the list.
     *
     * @param values map used for saving the data @see org.openengsb.ui.web.model.MapModel
     */
    public static RepeatingView createFieldList(String id, List<AttributeDefinition> attributes,
            Map<String, String> values) {
        RepeatingView fields = new RepeatingView(id);
        for (AttributeDefinition a : attributes) {
            addRowToView(values, fields, a);
        }
        return fields;
    }

    /**
     * creates a RepeatingView providing a suitable editor field for every attribute in the list.
     *
     * @param values map used for saving the data @see org.openengsb.ui.web.model.MapModel
     * @param attributeViewIds this Map is populated with ids of the generated elements
     * @return
     */
    public static RepeatingView createFieldList(String id, List<AttributeDefinition> attributes,
            Map<String, String> values, Map<String, String> attributeViewIds) {
        RepeatingView fields = new RepeatingView(id);
        for (AttributeDefinition a : attributes) {
            String attributeViewId = addRowToView(values, fields, a);
            attributeViewIds.put(a.getId(), attributeViewId);
        }
        return fields;
    }

    private static String addRowToView(Map<String, String> values, RepeatingView fields, AttributeDefinition a) {
        String attributeViewId = fields.newChildId();
        WebMarkupContainer row = new WebMarkupContainer(attributeViewId);
        fields.add(row);
        row.add(createEditorField("row", new MapModel<String, String>(values, a.getId()), a));
        return attributeViewId;
    }

    /**
     * creates a single EditorField for the given attribute
     */
    public static AbstractField<?> createEditorField(String id, IModel<String> model,
            final AttributeDefinition attribute) {
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

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
    private static final class BooleanFieldValidator extends EditorFieldValidator<Boolean> {
        private BooleanFieldValidator(AttributeDefinition attribute) {
            super(attribute);
        }
    }

    @SuppressWarnings("serial")
    private static final class StringFieldValidator extends EditorFieldValidator<String> {
        private StringFieldValidator(AttributeDefinition attribute) {
            super(attribute);
        }
    }
}
