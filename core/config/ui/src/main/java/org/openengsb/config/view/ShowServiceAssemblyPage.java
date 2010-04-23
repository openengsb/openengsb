/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config.view;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.config.dao.PersistedObjectDao;
import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.domain.ReferenceAttribute;
import org.openengsb.config.domain.ServiceAssembly;
import org.openengsb.config.domain.ValueAttribute;
import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.BeanType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.jbi.types.RefType;
import org.openengsb.config.jbi.types.ServiceEndpointTargetType;
import org.openengsb.config.model.Models;

public class ShowServiceAssemblyPage extends BasePage {
    public EndpointType selectedEndpointType;
    public BeanType selectedBeanType;

    @SpringBean
    private ServiceAssemblyDao dao;
    @SpringBean
    private PersistedObjectDao poDao;

    @SuppressWarnings("serial")
    public ShowServiceAssemblyPage(ServiceAssembly sa) {
        setDefaultModel(Models.compoundDomain(dao, sa));
        add(new Label("name"));

        addPersistedObjectList("endpoint");
        addPersistedObjectList("bean");

        Form<?> newEndpointForm = new Form<Object>("newEndpointForm") {
            @Override
            protected void onSubmit() {
                createNewEndpointClicked();
            }
        };
        add(newEndpointForm);

        DropDownChoice<EndpointType> choice = new DropDownChoice<EndpointType>("endpointSelect",
                new PropertyModel<EndpointType>(this, "selectedEndpointType"), componentService.getEndpoints(),
                new IChoiceRenderer<EndpointType>() {
                    @Override
                    public Object getDisplayValue(EndpointType e) {
                        return new StringResourceModel(e.getParent().getName() + "." + e.getName() + "._name",
                                ShowServiceAssemblyPage.this, null).getString();
                    }

                    @Override
                    public String getIdValue(EndpointType e, int index) {
                        return Integer.toString(index);
                    }
                });
        choice.setRequired(true);
        newEndpointForm.add(choice);

        Form<?> newBeanForm = new Form<Object>("newBeanForm") {
            @Override
            protected void onSubmit() {
                createNewBeanClicked();
            }
        };
        add(newBeanForm);

        DropDownChoice<BeanType> beanChoice = new DropDownChoice<BeanType>("beanSelect", new PropertyModel<BeanType>(
                this, "selectedBeanType"), componentService.getBeans(), new IChoiceRenderer<BeanType>() {
            @Override
            public Object getDisplayValue(BeanType e) {
                return new StringResourceModel(e.getParent().getName() + "." + e.getClazz() + "._name",
                        ShowServiceAssemblyPage.this, null).getString();
            }

            @Override
            public String getIdValue(BeanType e, int index) {
                return Integer.toString(index);
            }
        });
        choice.setRequired(true);
        newBeanForm.add(beanChoice);

        Form<?> actionForm = new Form<Object>("actionForm");
        add(actionForm);
        Button deployButton = new Button("deployButton") {
            @Override
            public void onSubmit() {
                try {
                    ServiceAssembly sa = (ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject();
                    assemblyService.deploy(sa);
                    setResponsePage(new ShowServiceAssemblyPage(sa));
                } catch (IOException e) {
                    error(new StringResourceModel("deploy.failed", this, null).getString());
                }
            }
        };
        actionForm.add(deployButton);
        Button undeployButton = new Button("undeployButton") {
            @Override
            public void onSubmit() {
                try {
                    ServiceAssembly sa = (ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject();
                    assemblyService.undeploy(sa);
                    setResponsePage(new ShowServiceAssemblyPage(sa));
                } catch (IOException e) {
                    error(new StringResourceModel("undeploy.failed", this, null).getString());
                }
            }
        };
        actionForm.add(undeployButton);
        Button deleteButton = new Button("deleteButton") {
            @Override
            public void onSubmit() {
                dao.delete((ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject());
                setResponsePage(OverviewPage.class);
            }
        };
        actionForm.add(deleteButton);

        if (assemblyService.isDeployed(sa)) {
            deleteButton.setEnabled(false);
        } else {
            undeployButton.setEnabled(false);
        }

        add(new Label("deployState", new StringResourceModel(assemblyService.isDeployed(sa) ? "deployed"
                : "notDeployed", this, null).getString()));

        add(new FeedbackPanel("feedback"));
    }

    @SuppressWarnings("serial")
    private void addPersistedObjectList(String startName) {
        final ListView<PersistedObject> list = new ListView<PersistedObject>(startName + "s") {
            @Override
            protected void populateItem(ListItem<PersistedObject> item) {
                item.add(new Label("name", item.getModelObject().getName()));
                item.add(new Link<PersistedObject>("editLink", item.getModel()) {
                    @Override
                    public void onClick() {
                        RequestCycle.get().setResponsePage(new BeanEditorPage(getModelObject()));
                    }
                });
                item.add(new Link<PersistedObject>("deleteLink", item.getModel()) {
                    @Override
                    public void onClick() {
                        poDao.delete(getModelObject());
                        ServiceAssembly sa = (ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject();
                        dao.refresh(sa);
                        RequestCycle.get().setResponsePage(new ShowServiceAssemblyPage(sa));
                    }
                });
            }
        };
        add(list);
        @SuppressWarnings("unchecked")
        final boolean empty = ((List<PersistedObject>) list.getDefaultModel().getObject()).isEmpty();
        add(new Label(startName + "Label", getLocalizer().getString(startName + "Label", this)) {
            @Override
            public boolean isVisible() {
                return empty;
            }
        });
    }

    private void createNewEndpointClicked() {
        createNewPO(PersistedObject.Type.Endpoint, selectedEndpointType.getParent().getName(), selectedEndpointType
                .getName(), selectedEndpointType.getAttributes());
    }

    private void createNewBeanClicked() {
        createNewPO(PersistedObject.Type.Bean, selectedBeanType.getParent().getName(), selectedBeanType.getClazz(),
                selectedBeanType.getProperties());
    }

    private void createNewPO(PersistedObject.Type persistedType, String componentType, String declaredType,
            List<AbstractType> fields) {
        PersistedObject po = new PersistedObject();
        ServiceAssembly sa = (ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject();
        po.setServiceAssembly(sa);
        po.setPersistedType(persistedType);
        po.setDeclaredType(declaredType);
        po.setComponentType(componentType);
        for (AbstractType t : fields) {
            if (t.getClass().equals(ServiceEndpointTargetType.class) || t.getClass().equals(RefType.class)) {
                po.getAttributes().put(t.getName(), new ReferenceAttribute(po, t.getName(), null));
            } else {
                po.getAttributes().put(t.getName(),
                        new ValueAttribute(po, t.getName(), t.getDefaultValue() != null ? t.getDefaultValue() : ""));
            }
        }
        RequestCycle.get().setResponsePage(new BeanEditorPage(po));
    }
}
