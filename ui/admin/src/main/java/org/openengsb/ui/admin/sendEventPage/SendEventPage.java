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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.ruleEditorPanel.RuleEditorPanel;
import org.openengsb.ui.admin.ruleEditorPanel.RuleManagerProvider;
import org.openengsb.ui.admin.util.ValueConverter;
import org.openengsb.ui.common.editor.AttributeEditorUtil;
import org.openengsb.ui.common.model.MapModel;
import org.openengsb.ui.common.util.MethodUtil;

@AuthorizeInstantiation("ROLE_USER")
public class SendEventPage extends BasePage implements RuleManagerProvider {

    private static Log log = LogFactory.getLog(SendEventPage.class);

    private static OsgiUtilsService serviceUtils = OpenEngSBCoreServices.getServiceUtilsService();

    @SpringBean
    private WorkflowService eventService;

    private DropDownChoice<Class<?>> dropDownChoice;
    @SpringBean
    private RuleManager ruleManager;

    @SpringBean
    private AuditingDomain auditing;

    private final Map<String, IModel<String>> values = new HashMap<String, IModel<String>>();

    private RepeatingView fieldList;

    private final ValueConverter valueConverter = new ValueConverter();

    private Map<String, String> realValues = new HashMap<String, String>();

    public SendEventPage() {
        initContent();
    }

    public SendEventPage(PageParameters parameters) {
        super(parameters);
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
                target.addComponent(container);
            }
        });
        form.add(dropDownChoice);
        form.add(container);
        container.add(createEditorPanelForClass(classes.get(0)));
        container.setOutputMarkupId(true);
        form.add(new FeedbackPanel("feedback"));

        final WebMarkupContainer auditsContainer = new WebMarkupContainer("auditsContainer");
        auditsContainer.setOutputMarkupId(true);

        AjaxButton submitButton = new IndicatingAjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Event event = buildEvent(dropDownChoice.getModelObject(), values);
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
                target.addComponent(form);
                target.addComponent(auditsContainer);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(form);
            }
        };
        submitButton.setOutputMarkupId(true);
        form.add(submitButton);
        List<String> audits = new ArrayList<String>();
        try {
            audits = auditing.getAudits();
        } catch (Exception e) {
            log.error("Audits cannot be loaded", e);
        }
        ListView<String> listView = new ListView<String>("audits", audits) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("audit", item.getModelObject()));
            }
        };
        auditsContainer.add(listView);
        add(auditsContainer);
        add(new RuleEditorPanel("ruleEditor", this));
    }

    private RepeatingView createEditorPanelForClass(Class<?> theClass) {
        values.clear();
        realValues.clear();
        List<AttributeDefinition> attributes = MethodUtil.buildAttributesList(theClass);
        moveNameToFront(attributes);
        for (AttributeDefinition def : attributes) {
            values.put(def.getId(), new MapModel<String, String>(realValues, def.getId()));
        }
        fieldList = AttributeEditorUtil.createFieldList("fields", attributes, values);
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

    private Event buildEvent(Class<?> eventClass, Map<String, IModel<String>> values) {
        try {
            Event obj = (Event) eventClass.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(eventClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getWriteMethod() == null
                        || !Modifier.isPublic(propertyDescriptor.getWriteMethod().getModifiers())) {
                    continue;
                }
                String string = values.get(propertyDescriptor.getName()).getObject();
                Object converted = valueConverter.convert(propertyDescriptor.getPropertyType(), string);
                propertyDescriptor.getWriteMethod().invoke(obj, converted);
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
