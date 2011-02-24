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

package org.openengsb.ui.admin.workflowEditor.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.workflow.editor.Action;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;

@AuthorizeInstantiation("ROLE_USER")
public class EditAction extends BasePage {

    private transient Method actionMethod;

    @SpringBean
    private DomainService domainService;

    public EditAction(final Action node) {

        IModel<List<Class<? extends Domain>>> domainModel = new AbstractReadOnlyModel<List<Class<? extends Domain>>>() {

            @Override
            public List<Class<? extends Domain>> getObject() {
                List<Class<? extends Domain>> domains = new ArrayList<Class<? extends Domain>>();
                for (DomainProvider provider : domainService.domains()) {
                    domains.add(provider.getDomainInterface());
                }
                return domains;
            }
        };
        IModel<List<Method>> methodModel = new AbstractReadOnlyModel<List<Method>>() {
            @Override
            public List<Method> getObject() {
                if (node.getDomain() != null) {
                    return Arrays.asList(node.getDomain().getMethods());
                } else {
                    return Collections.emptyList();
                }
            }
        };

        DropDownChoice domain = new DropDownChoice("domainSelect", new PropertyModel(node, "domain"), domainModel);
        domain.setOutputMarkupId(true);

        final DropDownChoice method =
            new DropDownChoice("methodSelect", new PropertyModel(this, "actionMethod"), methodModel);
        method.setOutputMarkupId(true);

        Form<Object> form = new Form<Object>("actionForm") {
            @Override
            protected void onSubmit() {
                if (node.getLocation() != "" && node.getLocation() != null && node.getDomain() != null
                        && getActionMethod() != null) {
                    node.setMethodName(getActionMethod().getName());
                    node.setMethodParameters(Arrays.asList(getActionMethod().getParameterTypes()));
                    setResponsePage(WorkflowEditor.class);
                }
            }
        };
        form.add(domain);
        form.add(method);
        form.add(new TextField<String>("location", new PropertyModel<String>(node, "location")));
        add(form);
    }

    public final Method getActionMethod() {
        return actionMethod;
    }

    public final void setActionMethod(Method actionMethod) {
        this.actionMethod = actionMethod;
    }

}
