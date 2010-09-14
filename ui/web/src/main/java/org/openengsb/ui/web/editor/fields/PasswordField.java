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

package org.openengsb.ui.web.editor.fields;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.openengsb.core.common.descriptor.AttributeDefinition;

@SuppressWarnings("serial")
public class PasswordField extends AbstractField {

    public PasswordField(String id, IModel<String> model, AttributeDefinition attribute, IValidator fieldValidationValidator) {
        super(id, model, attribute, fieldValidationValidator);
    }

    @Override
    protected FormComponent<String> createFormComponent(AttributeDefinition attribute, IModel<String> model) {
        PasswordTextField field = new PasswordTextField("field", model);
        return field;
    }
}
