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

package org.openengsb.ui.admin.workflowEditor.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.NodeRepresentation;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;

@AuthorizeInstantiation("ROLE_USER")
public class EditAction extends BasePage {

    private transient Method actionMethod;

    private OsgiUtilsService serviceUtils = OpenEngSBCoreServices.getServiceUtilsService();

    public EditAction(final NodeRepresentation parent, final ActionRepresentation action) {

        IModel<List<Class<? extends Domain>>> domainModel = new AbstractReadOnlyModel<List<Class<? extends Domain>>>() {

            @Override
            public List<Class<? extends Domain>> getObject() {
                List<Class<? extends Domain>> domains = new ArrayList<Class<? extends Domain>>();
                List<DomainProvider> serviceList = serviceUtils.listServices(DomainProvider.class);
                Collections.sort(serviceList, Comparators.forDomainProvider());
                for (DomainProvider provider : serviceList) {
                    domains.add(provider.getDomainInterface());
                }
                return domains;
            }
        };
        IModel<List<Method>> methodModel = new AbstractReadOnlyModel<List<Method>>() {
            @Override
            public List<Method> getObject() {
                if (action.getDomain() != null) {
                    return Arrays.asList(action.getDomain().getDeclaredMethods());
                } else {
                    return Collections.emptyList();
                }
            }
        };

        DropDownChoice domain = new DropDownChoice("domainSelect", new PropertyModel(action, "domain"), domainModel);
        domain.setOutputMarkupId(true);

        final DropDownChoice method =
            new DropDownChoice("methodSelect", new PropertyModel(this, "actionMethod"), methodModel);
        method.setOutputMarkupId(true);

        Form<Object> form = new Form<Object>("actionForm") {
            @Override
            protected void onSubmit() {
                if (action.getLocation() != "" && action.getLocation() != null && action.getDomain() != null
                        && getActionMethod() != null) {
                    action.setMethodName(getActionMethod().getName());
                    action.setMethodParameters(Arrays.asList(getActionMethod().getParameterTypes()));
                    if (parent != null) {
                        parent.addAction(action);
                    }
                    setResponsePage(WorkflowEditor.class);
                }
            }
        };

        Button cancelButton = new Button("cancel-button") {
            @Override
            public void onSubmit() {
                setResponsePage(WorkflowEditor.class);
            }
        };

        form.add(domain);
        form.add(method);
        form.add(new TextField<String>("location", new PropertyModel<String>(action, "location")));
        form.add(cancelButton);
        add(form);
    }

    public final Method getActionMethod() {
        return actionMethod;
    }

    public final void setActionMethod(Method actionMethod) {
        this.actionMethod = actionMethod;
    }

}
