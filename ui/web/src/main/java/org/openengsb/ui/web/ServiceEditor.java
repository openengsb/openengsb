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

package org.openengsb.ui.web;

import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.ui.common.wicket.editor.ServiceEditorPanel;
import org.openengsb.ui.common.wicket.validation.DefaultPassingFormValidator;

@SuppressWarnings("serial")
public abstract class ServiceEditor extends Panel {

    private final Map<String, String> values;
    private final List<AttributeDefinition> attributes;
    private final FormValidator validator;
    private ServiceEditorPanel serviceEditorPanel;

    public ServiceEditor(String id, List<AttributeDefinition> attributes, Map<String, String> values) {
        this(id, attributes, values, new DefaultPassingFormValidator());
    }

    public ServiceEditor(String id, List<AttributeDefinition> attributes, Map<String, String> values,
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
        serviceEditorPanel = new ServiceEditorPanel("attributesPanel", attributes, values);
        form.add(serviceEditorPanel);
        if (validator != null) {
            serviceEditorPanel.attachFormValidator(form, validator);
        }

        form.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
        AjaxButton submitButton = new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ServiceEditor.this.onSubmit();
                if (hasErrorMessage()) {
                    serviceEditorPanel.addAjaxValidationToForm(form);
                    target.addComponent(form);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                serviceEditorPanel.addAjaxValidationToForm(form);
                target.addComponent(form);
            }
        };
        form.setOutputMarkupId(true);
        form.add(submitButton);
    }

    public abstract void onSubmit();

    public ServiceEditorPanel getServiceEditorPanel() {
        return this.serviceEditorPanel;
    }

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public Map<String, String> getValues() {
        return values;
    }

    protected boolean isValidating(){
        return serviceEditorPanel.isValidating();
    }
}
