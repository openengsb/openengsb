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
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.jbi.types.ServiceEndpointTargetType;
import org.openengsb.config.model.Models;

public class ShowServiceAssemblyPage extends BasePage {
    public EndpointType selected;

    @SpringBean
    private ServiceAssemblyDao dao;
    @SpringBean
    private PersistedObjectDao poDao;

    @SuppressWarnings("serial")
    public ShowServiceAssemblyPage(final ServiceAssembly sa) {
        setDefaultModel(Models.compoundDomain(dao, sa));
        add(new Label("name"));

        ListView<PersistedObject> endpointList = new ListView<PersistedObject>("endpoints") {
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
                        ServiceAssembly sa = (ServiceAssembly) ShowServiceAssemblyPage.this
                                .getDefaultModelObject();
                        dao.refresh(sa);
                        RequestCycle.get().setResponsePage(new ShowServiceAssemblyPage(sa));
                    }
                });
            }
        };
        add(endpointList);
        add(new Label("endpointLabel", getLocalizer().getString("endpointLabel", this)) {
            @Override
            public boolean isVisible() {
                return sa.getEndpoints().isEmpty();
            }
        });

        Form<?> newComponentForm = new Form<Object>("newComponentForm") {
            @Override
            protected void onSubmit() {
                PersistedObject po = new PersistedObject();
                ServiceAssembly sa = (ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject();
                po.setServiceAssembly(sa);
                po.setDeclaredType(selected.getName());
                po.setComponentType(selected.getParent().getName());
                for (AbstractType t : selected.getAttributes()) {
                    if (t.getClass().equals(ServiceEndpointTargetType.class)) {
                        po.getAttributes().put(t.getName(), new ReferenceAttribute(po, t.getName(), null));
                    } else {
                        po.getAttributes().put(
                                t.getName(),
                                new ValueAttribute(po, t.getName(), t.getDefaultValue() != null ? t.getDefaultValue()
                                        : ""));
                    }
                }
                RequestCycle.get().setResponsePage(new BeanEditorPage(po));
            }
        };
        add(newComponentForm);

        DropDownChoice<EndpointType> choice = new DropDownChoice<EndpointType>("componentSelect",
                new PropertyModel<EndpointType>(this, "selected"), componentService.getEndpoints(),
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
        newComponentForm.add(choice);

        Form<?> actionForm = new Form<Object>("actionForm");
        add(actionForm);
        actionForm.add(new Button("deployButton") {
            @Override
            public void onSubmit() {
                try {
                    assemblyService.deploy((ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject());
                } catch (IOException e) {
                    error(new StringResourceModel("deploy.failed", this, null).getString());
                }
            }
        });
        actionForm.add(new Button("deleteButton") {
            @Override
            public void onSubmit() {
                dao.delete((ServiceAssembly) ShowServiceAssemblyPage.this.getDefaultModelObject());
                getRequestCycle().setResponsePage(OverviewPage.class);
            }
        });

        add(new FeedbackPanel("feedback"));
    }
}
