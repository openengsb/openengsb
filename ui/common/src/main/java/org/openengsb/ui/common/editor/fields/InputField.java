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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.ui.common.editor.ModelFascade;

@SuppressWarnings("serial")
public class InputField extends AbstractField<String> {

    public InputField(String id, IModel<String> model, AttributeDefinition attribute, IValidator<String> validator,
            boolean editable) {
        super(id, model, attribute, validator, editable);
    }

    public InputField(String id, IModel<String> model, AttributeDefinition attribute, IValidator<String> validator) {
        super(id, model, attribute, validator);
    }

    @Override
    protected ModelFascade<String> createFormComponent(AttributeDefinition attribute, IModel<String> model) {
        TextField<String> text = new TextField<String>("field", model);
        ModelFascade<String> retVal = new ModelFascade<String>();
        retVal.setMainComponent(text);
        return retVal;
    }
}
