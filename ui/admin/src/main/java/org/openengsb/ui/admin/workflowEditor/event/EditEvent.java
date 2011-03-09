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
import org.openengsb.core.common.Raises;
import org.openengsb.core.common.workflow.editor.Action;
import org.openengsb.core.common.workflow.editor.Event;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;

@AuthorizeInstantiation("ROLE_USER")
public class EditEvent extends BasePage {

    public EditEvent(final Event node, final Action action) {

        IModel<List<Class<? extends org.openengsb.core.common.Event>>> eventsModel =
            new AbstractReadOnlyModel<List<Class<? extends org.openengsb.core.common.Event>>>() {

                @Override
                public List<Class<? extends org.openengsb.core.common.Event>> getObject() {
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

        DropDownChoice<Class<? extends org.openengsb.core.common.Event>> events =
            new DropDownChoice<Class<? extends org.openengsb.core.common.Event>>("eventSelect",
                new PropertyModel<Class<? extends org.openengsb.core.common.Event>>(node, "event"), eventsModel);
        events.setOutputMarkupId(true);

        Form<Object> form = new Form<Object>("eventForm") {
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
