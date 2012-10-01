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

package org.openengsb.ui.admin.sendEventPage;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.WorkflowException;
import org.openengsb.core.workflow.api.WorkflowService;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.ruleEditorPanel.RuleEditorPanel;
import org.openengsb.ui.admin.ruleEditorPanel.RuleManagerProvider;
import org.openengsb.ui.admin.util.ValueConverter;
import org.openengsb.ui.common.editor.AttributeEditorUtil;
import org.openengsb.ui.common.util.MethodUtil;
import org.openengsb.ui.common.workflow.WorkflowStartPanel;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "WORKFLOW_USER")
@PaxWicketMountPoint(mountPoint = "events")
public class SendEventPage extends BasePage implements RuleManagerProvider {

    private static final long serialVersionUID = -6450762722099473732L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SendEventPage.class);

    public static final String PAGE_NAME_KEY = "sendEventPage.title";
    public static final String PAGE_DESCRIPTION_KEY = "sendEventPage.description";

    @PaxWicketBean(name = "osgiUtilsService")
    private OsgiUtilsService serviceUtils;

    @PaxWicketBean(name = "eventService")
    private WorkflowService eventService;

    private DropDownChoice<Class<?>> dropDownChoice;
    @PaxWicketBean(name = "ruleManager")
    private RuleManager ruleManager;

    @PaxWicketBean(name = "auditing")
    private AuditingDomain auditing;

    private RepeatingView fieldList;

    private final ValueConverter valueConverter = new ValueConverter();

    private Map<String, String> realValues = new HashMap<String, String>();

    public SendEventPage() {
        initContent();
    }

    public SendEventPage(PageParameters parameters) {
        super(parameters, PAGE_NAME_KEY);
        initContent();
    }

    private void initContent() {
        List<Class<? extends Event>> classes = new ArrayList<Class<? extends Event>>();
        classes.add(Event.class);
        for (DomainProvider domain : serviceUtils.listServices(DomainProvider.class)) {
            classes.addAll(domain.getEvents());
        }
        init(classes);
    }

    //TODO: OPENENGSB-3272: Extract this into an own component
    private Component createProjectChoice() {
        DropDownChoice<String> dropDownChoice = new DropDownChoice<String>("projectChoice", new IModel<String>() {
            
            private static final long serialVersionUID = -5776062054709043273L;

            @Override
            public String getObject() {
                return getSessionContextId();
            }

            @Override
            public void setObject(String object) {
                ContextHolder.get().setCurrentContextId(object);
            }

            @Override
            public void detach() {
            }
        }, getAvailableContexts()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }

            @Override
            protected void onModelChanged() {
                setResponsePage(SendEventPage.this.getClass());
            }

        };
        return dropDownChoice;
    }
    
    public SendEventPage(List<Class<? extends Event>> classes) {
        init(classes);
    }

    @SuppressWarnings("serial")
    private void init(List<? extends Class<?>> classes) {
        Form<Object> form = new Form<Object>("form");
        add(form);
        ChoiceRenderer<Class<?>> choiceRenderer = new ChoiceRenderer<Class<?>>("canonicalName", "simpleName");
        final WebMarkupContainer container = new WebMarkupContainer("fieldContainer");
        dropDownChoice = new DropDownChoice<Class<?>>("dropdown", classes, choiceRenderer);
        dropDownChoice.setModel(new Model<Class<?>>(classes.get(0)));

        dropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Class<?> theClass = dropDownChoice.getModelObject();
                fieldList.removeAll();
                container.replace(createEditorPanelForClass(theClass));
                target.add(container);
            }
        });
        form.add(dropDownChoice);
        form.add(container);
        container.add(createEditorPanelForClass(classes.get(0)));
        container.setOutputMarkupId(true);
        form.add(new FeedbackPanel("feedback"));

        Form<?> pc = new Form<Object>("projectChoiceForm");
        pc.add(createProjectChoice());
        add(pc);
        
        final WebMarkupContainer auditsContainer = new WebMarkupContainer("auditsContainer");
        auditsContainer.setOutputMarkupId(true);

        AjaxButton submitButton = new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Event event = buildEvent(dropDownChoice.getModelObject(), realValues);
                if (event != null) {
                    try {
                        eventService.processEvent(event);
                        info(new StringResourceModel("send.event.success", SendEventPage.this, null).getString());
                    } catch (WorkflowException e) {
                        StringResourceModel resourceModel =
                            new StringResourceModel("send.event.error.process", SendEventPage.this, null);
                        error(resourceModel.getString());
                    }
                } else {
                    error(new StringResourceModel("send.event.error.build", SendEventPage.this, null).getString());
                }
                target.add(form);
                target.add(auditsContainer);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };
        submitButton.setOutputMarkupId(true);
        form.add(submitButton);
        List<Event> audits = new ArrayList<Event>();
        try {
            audits = auditing.getAllAudits();
        } catch (Exception e) {
            LOGGER.error("Audits cannot be loaded", e);
        }
        ListView<Event> listView = new ListView<Event>("audits", audits) {
            @Override
            protected void populateItem(ListItem<Event> item) {
                item.add(new Label("audit", item.getModelObject().getName()));
            }
        };
        auditsContainer.add(listView);
        add(auditsContainer);
        add(new WorkflowStartPanel("workflowStartPanel"));
        add(new RuleEditorPanel("ruleEditor", this));
    }

    private RepeatingView createEditorPanelForClass(Class<?> theClass) {
        realValues.clear();
        List<AttributeDefinition> attributes = MethodUtil.buildAttributesList(theClass);
        moveNameToFront(attributes);

        fieldList = AttributeEditorUtil.createFieldList("fields", attributes, realValues);
        return fieldList;
    }

    private List<AttributeDefinition> moveNameToFront(List<AttributeDefinition> attributes) {
        int i = 0;
        for (AttributeDefinition a : attributes) {
            if ("name".equals(a.getId())) {
                break;
            }
            i++;
        }
        AttributeDefinition tmp = attributes.get(0);
        attributes.set(0, attributes.get(i));
        attributes.set(i, tmp);
        return attributes;
    }

    private Event buildEvent(Class<?> eventClass, Map<String, String> values) {
        try {
            Event obj = (Event) eventClass.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(eventClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getWriteMethod() == null
                        || !Modifier.isPublic(propertyDescriptor.getWriteMethod().getModifiers())) {
                    continue;
                }
                String string = values.get(propertyDescriptor.getName());
                Object converted = valueConverter.convert(propertyDescriptor.getPropertyType(), string);
                propertyDescriptor.getWriteMethod().invoke(obj, converted);
            }
            return obj;
        } catch (Exception e) {
            LOGGER.error("building event instance failed", e);
            return null;
        }
    }

    @Override
    public RuleManager getRuleManager() {
        return ruleManager;
    }
}
