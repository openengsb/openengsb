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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.WorkflowException;
import org.openengsb.core.workflow.WorkflowService;
import org.openengsb.ui.web.editor.EditorPanel;
import org.openengsb.ui.web.ruleeditor.RuleEditorPanel;
import org.openengsb.ui.web.ruleeditor.RuleManagerProvider;

@SuppressWarnings("serial")
public class SendEventPage extends BasePage implements RuleManagerProvider {

    private transient Log log = LogFactory.getLog(SendEventPage.class);

    @SpringBean
    private WorkflowService eventService;

    @SpringBean
    private DomainService domainService;

    private DropDownChoice<Class<?>> dropDownChoice;
    @SpringBean
    private RuleManager ruleManager;

    public SendEventPage() {
        List<Class<? extends Event>> classes = new ArrayList<Class<? extends Event>>();
        classes.add(Event.class);
        for (DomainProvider domain : domainService.domains()) {
            classes.addAll(domain.getEvents());
        }
        init(classes);
    }

    public SendEventPage(List<Class<? extends Event>> classes) {
        init(classes);
    }

    private void init(List<? extends Class<?>> classes) {
        Form<Object> form = new Form<Object>("form");
        add(form);
        ChoiceRenderer<Class<?>> choiceRenderer = new ChoiceRenderer<Class<?>>("canonicalName", "simpleName");
        dropDownChoice = new DropDownChoice<Class<?>>("dropdown", classes, choiceRenderer);
        dropDownChoice.setModel(new Model<Class<?>>(classes.get(0)));
        dropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Class<?> theClass = dropDownChoice.getModelObject();
                EditorPanel editor = createEditorPanelForClass(theClass);
                SendEventPage.this.replace(editor);
                target.addComponent(editor);
            }
        });
        form.add(dropDownChoice);
        add(createEditorPanelForClass(classes.get(0)));
        add(new RuleEditorPanel("ruleEditor", this));
    }

    private EditorPanel createEditorPanelForClass(Class<?> theClass) {
        Map<String, String> defaults = new HashMap<String, String>();
        List<AttributeDefinition> attributes = MethodUtil.buildAttributesList(theClass);
        EditorPanel editor = new EditorPanel("editor", attributes, defaults) {
            @Override
            public void onSubmit() {
                Event event = buildEvent(dropDownChoice.getModelObject(), getValues());
                if (event != null) {
                    try {
                        eventService.processEvent(event);
                        info(new StringResourceModel("send.event.success", SendEventPage.this, null).getString());
                    } catch (WorkflowException e) {
                        error(
                            new StringResourceModel("send.event.error.process", SendEventPage.this, null).getString());
                    }
                } else {
                    error(new StringResourceModel("send.event.error.build", SendEventPage.this, null).getString());
                }
            }
        };
        editor.setOutputMarkupId(true);
        return editor;
    }

    private Event buildEvent(Class<?> eventClass, Map<String, String> values) {
        try {
            Event obj = (Event) eventClass.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(eventClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getWriteMethod() == null || !Modifier
                    .isPublic(propertyDescriptor.getWriteMethod().getModifiers())) {
                    continue;
                }
                propertyDescriptor.getWriteMethod().invoke(obj, values.get(propertyDescriptor.getName()));
            }
            return obj;
        } catch (Exception e) {
            log.error("building event istance failed", e);
            return null;
        }
    }

    @Override
    public RuleManager getRuleManager() {
        return ruleManager;
    }
}
