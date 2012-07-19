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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.validation.ValidationError;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.validation.FormValidator;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Creates a panel containing a service-editor, for usage in forms.
 * 
 */
@SuppressWarnings("deprecation")
public class ServiceEditorPanel extends Panel {

    private static final long serialVersionUID = 5593901084329552949L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEditorPanel.class);

    private final class EntryModel implements IModel<String> {

        private static final long serialVersionUID = -6996584309100143740L;

        private Entry<String, Object> entry;
        private int index;

        public EntryModel(Entry<String, Object> entry, int i) {
            this.entry = entry;
            index = i;
            adjustArraySize();
        }

        private void adjustArraySize() {
            if (isArray()) {
                if (getArray().length <= index) {
                    Object[] newArray = Arrays.copyOf(getArray(), index + 1);
                    entry.setValue(newArray);
                }
            } else if (index > 0) {
                entry.setValue(new Object[index + 1]);
            }
        }

        public void deleteSubElement(int index) {
            if (isArray()) {
                Object[] oldArray = getArray();
                Object[] newArray = new Object[oldArray.length - 1];
                int j = 0;
                for (int i = 0; i < oldArray.length; i++) {
                    if (i == index) {
                        continue;
                    }
                    newArray[j] = oldArray[i];
                    j++;
                }
                entry.setValue(newArray.length == 1 ? newArray[0] : newArray);
            }
        }

        @Override
        public void detach() {
            // do nothing
        }

        @Override
        public String getObject() {
            if (isArray()) {
                return (String) getArray()[index];
            } else if (index == 0) {
                Object entryValue = entry.getValue();
                return entryValue == null ? "null" : entryValue.toString();
            } else {
                throw new IllegalStateException("value is not an array");
            }
        }

        @Override
        public void setObject(String object) {
            if (!isArray() && index == 0) {
                entry.setValue(object);
            } else {
                Object[] array = getArray();
                array[index] = object;
            }
        }

        private Object[] getArray() {
            return (Object[]) entry.getValue();
        }

        private boolean isArray() {
            return entry.getValue().getClass().isArray();
        }

    }

    private final List<AttributeDefinition> attributes;
    private Model<Boolean> validatingModel;
    private WebMarkupContainer propertiesContainer;
    private ListView<MapEntry<String, Object>> propertiesList;
    private final Form<?> parentForm;
    private Map<String, Object> properties;
    private static final List<String> LOCKED_PROPERTIES = Arrays.asList(
        org.openengsb.core.api.Constants.CONNECTOR_KEY,
        org.openengsb.core.api.Constants.DOMAIN_KEY,
        Constants.SERVICE_ID, Constants.OBJECTCLASS, Constants.SERVICE_PID);

    @SuppressWarnings({ "serial" })
    public ServiceEditorPanel(String id, List<AttributeDefinition> attributes,
            Map<String, String> attributeMap, Map<String, Object> properties, Form<?> parentForm) {
        super(id);
        this.attributes = attributes;
        this.parentForm = parentForm;
        this.properties = properties;
        initPanel(attributes, attributeMap, properties);
        add(new AbstractBehavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                response.renderCSSReference(new PackageResourceReference(ServiceEditorPanel.class,
                    "ServiceEditorPanel.css"));
            }
        });
    }

    public void reloadList(Map<String, Object> properties) {
        List<MapEntry<String, Object>> entryList = transformToEntryList(properties);

        Collections.sort(entryList, new Comparator<MapEntry<String, Object>>() {
            @Override
            public int compare(MapEntry<String, Object> o1, MapEntry<String, Object> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        this.properties = properties;
        propertiesList.setList(entryList);
    }

    public List<MapEntry<String, Object>> transformToEntryList(Map<String, Object> properties) {
        Set<Entry<String, Object>> entrySet = properties.entrySet();
        Collection<Entry<String, Object>> filtered =
            Collections2.filter(entrySet, new Predicate<Entry<String, Object>>() {
                @Override
                public boolean apply(Entry<String, Object> input) {
                    return !LOCKED_PROPERTIES.contains(input.getKey());
                }
            });
        Collection<MapEntry<String, Object>> transformed =
            Collections2.transform(filtered, new EntryConverterFunction<String, Object>(properties));
        List<MapEntry<String, Object>> entryList = Lists.newArrayList(transformed);
        return entryList;
    }

    private class EntryConverterFunction<K, V> implements Function<Map.Entry<K, V>, MapEntry<K, V>> {

        private Map<K, V> originalMap;

        protected EntryConverterFunction(Map<K, V> originalMap) {
            this.originalMap = originalMap;
        }

        @Override
        public MapEntry<K, V> apply(Entry<K, V> input) {
            return new MapEntry<K, V>(originalMap, input);
        }
    }

    /**
     * Removes the property with the given key and reloads the property list afterwards
     */
    public void removeProperty(String key) {
        properties.remove(key);
        reloadList(properties);
    }

    @SuppressWarnings("serial")
    private void initPanel(List<AttributeDefinition> attributes, Map<String, String> attributeMap,
            Map<String, Object> properties) {
        RepeatingView fields =
            AttributeEditorUtil.createFieldList("fields", attributes, attributeMap);
        add(fields);
        validatingModel = new Model<Boolean>(true);
        CheckBox checkbox = new CheckBox("validate", validatingModel);
        add(checkbox);
        propertiesList = new ListView<MapEntry<String, Object>>("properties") {
            @Override
            protected void populateItem(final ListItem<MapEntry<String, Object>> item) {
                item.setOutputMarkupId(true);
                final MapEntry<String, Object> modelObject = item.getModelObject();
                IModel<String> keyModel = new PropertyModel<String>(modelObject, "key");
                item.add(new Label("key", keyModel));

                item.add(new WebMarkupContainer("buttonKey").add(new AjaxEventBehavior("onclick") {
                    protected void onEvent(AjaxRequestTarget target) {
                        ServiceEditorPanel.this.removeProperty(modelObject.getKey());
                        target.add(ServiceEditorPanel.this);
                    }
                }));

                final RepeatingView repeater = new RepeatingView("values");
                item.add(repeater);
                Object value = modelObject.getValue();
                if (value.getClass().isArray()) {
                    Object[] values = (Object[]) value;
                    for (int i = 0; i < values.length; i++) {
                        WebMarkupContainer container = new WebMarkupContainer(repeater.newChildId());
                        final EntryModel model = new EntryModel(modelObject, i);
                        final int index = i;
                        AjaxEditableLabel<String> l =
                            new AjaxEditableLabel<String>("value", model);
                        container.add(l);
                        container.add(new WebMarkupContainer("buttonValue").add(new AjaxEventBehavior("onclick") {
                            protected void onEvent(AjaxRequestTarget target) {
                                model.deleteSubElement(index);
                                target.add(ServiceEditorPanel.this);
                            }
                        }));
                        repeater.add(container);
                    }
                } else {
                    WebMarkupContainer container = new WebMarkupContainer(repeater.newChildId());
                    IModel<String> valueModel = new EntryModel(modelObject, 0);
                    AjaxEditableLabel<String> l = new AjaxEditableLabel<String>("value", valueModel);
                    container.add(l);
                    container.add(new WebMarkupContainer("buttonValue").setVisible(false));
                    repeater.add(container);
                }

                AjaxButton button = new AjaxButton("newArrayEntry", parentForm) {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        Object value = modelObject.getValue();
                        if (value.getClass().isArray()) {
                            Object[] array = (Object[]) value;
                            Object[] newArray = Arrays.copyOf(array, array.length + 1);
                            newArray[array.length] = "";
                            modelObject.setValue(newArray);
                        } else {
                            Object[] newArray = new Object[2];
                            newArray[0] = value;
                            newArray[1] = "";
                            modelObject.setValue(newArray);
                        }
                        target.add(item);
                        target.add(ServiceEditorPanel.this);
                    }

                    @Override
                    protected void onError(AjaxRequestTarget target, Form<?> form) {
                        LOGGER.warn("Error occured during submit!");
                    }
                };
                item.add(button);
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

            private static final long serialVersionUID = -4181095793820830517L;

            @Override
            public void validate(Form<?> form) {
                Map<String, FormComponent<?>> loadFormComponents = loadFormComponents(form);
                Map<String, String> toValidate = new HashMap<String, String>();
                for (Map.Entry<String, FormComponent<?>> entry : loadFormComponents.entrySet()) {
                    toValidate.put(entry.getKey(), entry.getValue().getValue());
                }
                try {
                    validator.validate(toValidate);
                } catch (ConnectorValidationFailedException e) {
                    Map<String, String> attributeErrorMessages = e.getErrorMessages();
                    for (Map.Entry<String, String> entry : attributeErrorMessages.entrySet()) {
                        FormComponent<?> fc = loadFormComponents.get(entry.getKey());
                        fc.error(new ValidationError().setMessage(entry.getValue()));
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
