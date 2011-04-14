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

import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.validation.FieldValidator;
import org.openengsb.core.api.validation.SingleAttributeValidationResult;
import org.openengsb.ui.common.editor.fields.AbstractField;
import org.openengsb.ui.common.editor.fields.CheckboxField;
import org.openengsb.ui.common.editor.fields.DropdownField;
import org.openengsb.ui.common.editor.fields.InputField;
import org.openengsb.ui.common.editor.fields.PasswordField;
import org.openengsb.ui.common.model.MapModel;
import org.openengsb.ui.common.util.MethodUtil;

/**
 * common utility-functions for creating Forms to edit properties of beans.
 */
public final class AttributeEditorUtil {
    private AttributeEditorUtil() {
    }

    /**
     * creates a RepeatingView providing a suitable editor field for every property.
     *
     * @param values map used for saving the data @see org.openengsb.ui.common.wicket.model.MapModel
     */
    public static RepeatingView createFieldList(String id, Class<?> bean, Map<String, String> values) {
        List<AttributeDefinition> attributes = MethodUtil.buildAttributesList(bean);
        return createFieldList(id, attributes, values);
    }

    /**
     * creates a RepeatingView providing a suitable editor field for every attribute in the list.
     *
     * @param values map used for saving the data @see org.openengsb.ui.common.wicket.model.MapModel
     */
    public static RepeatingView createFieldList(String id, List<AttributeDefinition> attributes,
            Map<String, String> values) {
        RepeatingView fields = new RepeatingView(id);
        for (AttributeDefinition a : attributes) {
            WebMarkupContainer row = new WebMarkupContainer(a.getId());
            MapModel<String, String> model = new MapModel<String, String>(values, a.getId());
            row.add(createEditorField("row", model, a));
            fields.add(row);
        }
        return fields;
    }

    /**
     * creates a single EditorField for the given attribute
     */
    public static AbstractField<?> createEditorField(String id, IModel<String> model,
            final AttributeDefinition attribute) {
        return createEditorField(id, model, attribute, true);
    }

    /**
     * creates a single EditorField for the given attribute
     */
    public static AbstractField<?> createEditorField(String id, IModel<String> model,
            final AttributeDefinition attribute, boolean editable) {
        if (attribute.isBoolean()) {
            return new CheckboxField(id, model, attribute, new BooleanFieldValidator(attribute));
        }
        StringFieldValidator validator = new StringFieldValidator(attribute);
        if (!attribute.getOptions().isEmpty()) {
            return new DropdownField(id, model, attribute, validator);
        } else if (attribute.isPassword()) {
            return new PasswordField(id, model, attribute, validator);
        } else {
            InputField inputField = new InputField(id, model, attribute, validator, editable);
            return inputField;
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
