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

package org.openengsb.ui.admin.workflowEditor.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.Raises;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;

@AuthorizeInstantiation("ROLE_USER")
public class EditEvent extends BasePage {

    public EditEvent(final EventRepresentation node, final ActionRepresentation action) {

        IModel<List<Class<? extends Event>>> eventsModel =
            new AbstractReadOnlyModel<List<Class<? extends Event>>>() {
                private static final long serialVersionUID = -1821249760471529173L;

                @Override
                public List<Class<? extends Event>> getObject() {
                    List<Class<?>> methodParameters = action.getMethodParameters();
                    Method method;
                    try {
                        method =
                            action.getDomain().getMethod(action.getMethodName(),
                                methodParameters.toArray(new Class[methodParameters.size()]));
                        Raises annotation = method.getAnnotation(Raises.class);
                        if (annotation != null) {
                            return Arrays.asList(annotation.value());
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return Collections.emptyList();
                }
            };

        DropDownChoice<Class<? extends Event>> events =
            new DropDownChoice<Class<? extends Event>>("eventSelect", new PropertyModel<Class<? extends Event>>(node,
                "event"), eventsModel);
        events.setOutputMarkupId(true);

        Form<Object> form = new Form<Object>("eventForm") {
            private static final long serialVersionUID = -1921082055165607268L;

            @Override
            protected void onSubmit() {
                if (node.getEvent() != null) {
                    setResponsePage(WorkflowEditor.class);
                    if (!action.getEvents().contains(node)) {
                        action.addEvent(node);
                    }
                }
            }
        };

        Button cancelButton = new Button("cancel-button") {
            private static final long serialVersionUID = -2062083789168059582L;

            @Override
            public void onSubmit() {
                setResponsePage(WorkflowEditor.class);
            }
        };

        form.add(events);
        form.add(cancelButton);
        add(form);
    }
}
