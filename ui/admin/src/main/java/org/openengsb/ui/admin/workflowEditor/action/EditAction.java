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
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.NodeRepresentation;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;
import org.ops4j.pax.wicket.api.PaxWicketBean;

@AuthorizeInstantiation("ROLE_USER")
public class EditAction extends BasePage {

    private transient Method actionMethod;

    @PaxWicketBean(name = "domainProviders")
    private List<DomainProvider> domainProviders;

    private final ActionRepresentation action;

    public EditAction(final NodeRepresentation parent, final ActionRepresentation action) {
        this.action = action;
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        IModel<List<Class<? extends Domain>>> domainModel = new AbstractReadOnlyModel<List<Class<? extends Domain>>>() {
            private static final long serialVersionUID = 2025153088116670822L;

            @Override
            public List<Class<? extends Domain>> getObject() {
                List<Class<? extends Domain>> domains = new ArrayList<Class<? extends Domain>>();
                List<DomainProvider> domainProvidersCopy = new ArrayList<DomainProvider>(domainProviders);
                Collections.sort(domainProvidersCopy, Comparators.forDomainProvider());
                for (DomainProvider provider : domainProvidersCopy) {
                    domains.add(provider.getDomainInterface());
                }
                return domains;
            }
        };
        IModel<List<Method>> methodModel = new AbstractReadOnlyModel<List<Method>>() {
            private static final long serialVersionUID = -8022955494416312574L;

            @Override
            public List<Method> getObject() {
                if (action.getDomain() != null) {
                    return Arrays.asList(action.getDomain().getDeclaredMethods());
                } else {
                    return Collections.emptyList();
                }
            }
        };

        DropDownChoice<Class<? extends Domain>> domain =
            new DropDownChoice<Class<? extends Domain>>("domainSelect", new PropertyModel<Class<? extends Domain>>(
                action, "domain"), domainModel);
        domain.setOutputMarkupId(true);

        IChoiceRenderer<Method> methodRenderer = new IChoiceRenderer<Method>() {
            private static final long serialVersionUID = -6081780028419630690L;

            @Override
            public Object getDisplayValue(Method object) {
                StringBuilder builder = new StringBuilder();
                builder.append(object.getReturnType().getSimpleName());
                builder.append(" ");
                builder.append(object.getName());
                builder.append("(");
                Iterator<Class<?>> iterator = Arrays.asList(object.getParameterTypes()).iterator();
                while (iterator.hasNext()) {
                    builder.append(iterator.next().getSimpleName());
                    if (iterator.hasNext()) {
                        builder.append(",");
                    }
                }
                builder.append(")");
                return builder.toString();
            }

            @Override
            public String getIdValue(Method object, int index) {
                return Integer.toString(index);
            }

        };
        final DropDownChoice<Method> method =
            new DropDownChoice<Method>("methodSelect", new PropertyModel<Method>(this, "actionMethod"), methodModel,
                methodRenderer);
        method.setOutputMarkupId(true);

        Form<Object> form = new Form<Object>("actionForm") {
            private static final long serialVersionUID = -2637470792433348801L;
        };
        Button createTemplateCode = new Button("create-template-code") {
            private static final long serialVersionUID = 8658058523401324841L;

            @Override
            public void onSubmit() {
                if (action.getDomain() != null && action.getMethodName() != null && action.getLocation() != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(action.getLocation() + "." + action.getMethodName() + "(");
                    Iterator<Class<?>> iterator = action.getMethodParameters().iterator();

                    while (iterator.hasNext()) {
                        Class<?> next = iterator.next();
                        builder.append(next.getName());
                        if (iterator.hasNext()) {
                            builder.append(", ");
                        }
                    }
                    builder.append(");");
                    action.setCode(builder.toString());
                } else {
                    if (action.getDomain() == null) {
                        error(getString("error.domain"));
                    }
                    if (action.getMethodName() == null) {
                        error(getString("error.method"));
                    }
                    if (action.getLocation() == null) {
                        error(getString("error.location"));
                    }
                }
            }
        };

        Button submitButton = new Button("submit-button") {
            private static final long serialVersionUID = 6331939408156197715L;

            @Override
            public void onSubmit() {
                if (action.getLocation() != "" && action.getLocation() != null && action.getDomain() != null
                        && action.getMethodName() != null && action.getCode() != null) {
                    if (parent != null) {
                        parent.addAction(action);
                    }
                    setResponsePage(WorkflowEditor.class);
                }
            }
        };

        Button cancelButton = new Button("cancel-button") {
            private static final long serialVersionUID = 3838055892315690316L;

            @Override
            public void onSubmit() {
                setResponsePage(WorkflowEditor.class);
            }
        };

        form.add(domain);
        form.add(method);
        form.add(createTemplateCode);
        form.add(new TextField<String>("location", new PropertyModel<String>(action, "location")));
        form.add(new TextArea<String>("code", new PropertyModel<String>(action, "code")));
        form.add(submitButton);
        form.add(cancelButton);
        add(form);
    }

    public final Method getActionMethod() {
        return actionMethod;
    }

    public final void setActionMethod(Method actionMethod) {
        this.actionMethod = actionMethod;
        action.setMethodName(getActionMethod().getName());
        action.setMethodParameters(Arrays.asList(getActionMethod().getParameterTypes()));
    }

}
