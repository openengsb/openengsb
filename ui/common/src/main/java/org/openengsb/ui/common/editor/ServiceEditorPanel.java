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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.openengsb.core.api.ServiceValidationFailedException;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.validation.FormValidator;
import org.openengsb.core.common.util.DictionaryAsMap;

/**
 * Creates a panel containing a service-editor, for usage in forms.
 *
 */
@SuppressWarnings("serial")
public class ServiceEditorPanel extends Panel {

    private final List<AttributeDefinition> attributes;
    private Model<Boolean> validatingModel;
    private WebMarkupContainer propertiesContainer;
    private ListView<Map.Entry<String, Object>> propertiesList;

    public ServiceEditorPanel(String id, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Dictionary<String, Object> properties) {
        super(id);
        this.attributes = attributes;
        initPanel(attributes, attributeMap, properties);
    }

    public void reloadList(Dictionary<String, Object> properties) {
        System.out.println("ServiceEditorPanel.reloadList()" + System.identityHashCode(properties));
        Map<String, Object> wrapped = DictionaryAsMap.wrap(properties);
        List<Entry<String, Object>> entryList = new LinkedList<Map.Entry<String, Object>>(wrapped.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Object>>() {
            @Override
            public int compare(Entry<String, Object> o1, Entry<String, Object> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        propertiesList.setList(entryList);
    }

    private void initPanel(List<AttributeDefinition> attributes, Map<String, String> attributeMap,
            Dictionary<String, Object> properties) {
        RepeatingView fields =
            AttributeEditorUtil.createFieldList("fields", attributes, attributeMap);
        add(fields);
        validatingModel = new Model<Boolean>(true);
        CheckBox checkbox = new CheckBox("validate", validatingModel);
        add(checkbox);

        propertiesList = new ListView<Map.Entry<String, Object>>("properties") {
            @Override
            protected void populateItem(ListItem<Map.Entry<String, Object>> item) {
                Entry<String, Object> modelObject = item.getModelObject();
                IModel<String> keyModel = new PropertyModel<String>(modelObject, "key");
                item.add(new Label("key", keyModel));
                IModel<String> valueModel;

                if (modelObject.getValue() instanceof String) {
                    valueModel = new PropertyModel<String>(item.getModelObject(), "value");
                    TextField<String> textField = new TextField<String>("value", valueModel);
                    item.add(textField);
                } else {
                    valueModel = new Model<String>("#array");
                    TextField<String> textField = new TextField<String>("value", valueModel);
                    textField.setEnabled(false);
                    item.add(textField);
                }
            }
        };
        propertiesList.setOutputMarkupId(true);
        reloadList(properties);

        propertiesContainer = new WebMarkupContainer("propertiesContainer");
        propertiesContainer.add(propertiesList);
        propertiesContainer.setOutputMarkupId(true);

        add(propertiesList);
    }

    /**
     * attach a ServiceValidator to the given form. This formValidator is meant to validate the fields in context to
     * each other. This validation is only done on submit.
     */
    public void attachFormValidator(final Form<?> form, final FormValidator validator) {
        form.add(new AbstractFormValidator() {

            @Override
            public void validate(Form<?> form) {
                Map<String, FormComponent<?>> loadFormComponents = loadFormComponents(form);
                Map<String, String> toValidate = new HashMap<String, String>();
                for (Map.Entry<String, FormComponent<?>> entry : loadFormComponents.entrySet()) {
                    toValidate.put(entry.getKey(), entry.getValue().getValue());
                }
                try {
                    validator.validate(toValidate);
                } catch (ServiceValidationFailedException e) {
                    Map<String, String> attributeErrorMessages = e.getErrorMessages();
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
                        Component component =
                            form.get("attributesPanel:fields:" + attribute + ":row:field");
                        if (component instanceof FormComponent<?>) {
                            formComponents.put(attribute, (FormComponent<?>) component);
                        }
                    }
                }
                return formComponents;
            }
        });
    }

    /**
     * enable ad-hoc validation on all fields in your form
     */
    public static void addAjaxValidationToForm(Form<?> form) {
        AjaxFormValidatingBehavior.addToAllFormComponents(form, "onBlur");
        AjaxFormValidatingBehavior.addToAllFormComponents(form, "onChange");
    }

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public boolean isValidating() {
        return validatingModel.getObject();
    }
}
