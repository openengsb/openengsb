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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.AttributeDefinition.Option;
import org.openengsb.ui.common.editor.ModelFascade;
import org.openengsb.ui.common.model.LocalizableStringModel;

@SuppressWarnings("serial")
public class DropdownField extends AbstractField<String> {

    public DropdownField(String id, IModel<String> model, AttributeDefinition attribute, IValidator<String> validator) {
        super(id, model, attribute, validator);
    }

    @Override
    protected ModelFascade<String> createFormComponent(AttributeDefinition attribute, IModel<String> model) {
        final Map<String, LocalizableStringModel> labels = new HashMap<String, LocalizableStringModel>();
        final List<String> values = new ArrayList<String>();
        for (Option o : attribute.getOptions()) {
            labels.put(o.getValue(), new LocalizableStringModel(this, o.getLabel()));
            values.add(o.getValue());
        }
        DropDownChoice<String> choice = new DropDownChoice<String>("field", model, values,
                new IChoiceRenderer<String>() {
                    @Override
                    public String getDisplayValue(String object) {
                        return labels.get(object).getObject();
                    }

                    @Override
                    public String getIdValue(String object, int index) {
                        return "" + index;
                    }

                });
        choice.setNullValid(!attribute.isRequired());
        ModelFascade<String> retVal = new ModelFascade<String>();
        retVal.setMainComponent(choice);
        return retVal;
    }
}
