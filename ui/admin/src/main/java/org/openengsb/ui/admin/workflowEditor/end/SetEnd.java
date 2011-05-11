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

package org.openengsb.ui.admin.workflowEditor.end;

import java.util.List;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EndRepresentation;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;

@AuthorizeInstantiation("ROLE_USER")
public class SetEnd extends BasePage {

    private EndRepresentation end;

    public SetEnd(final WorkflowRepresentation workflow, final ActionRepresentation action) {
        IModel<List<EndRepresentation>> endModel = new AbstractReadOnlyModel<List<EndRepresentation>>() {

            @Override
            public List<EndRepresentation> getObject() {
                return workflow.getEndNodes();
            }
        };
        Form<Object> form = new Form<Object>("endSelectForm");
        final EndRepresentation endNode = new EndRepresentation();
        form.add(new DropDownChoice("endSelect", new PropertyModel(this, "end"), endModel));
        form.add(new Button("select") {
            @Override
            public void onSubmit() {
                action.setEnd(end);
                setResponsePage(WorkflowEditor.class);
            }
        });
        form.add(new TextField<String>("name", new PropertyModel<String>(endNode, "name")));
        form.add(new Button("create") {
            @Override
            public void onSubmit() {
                workflow.addEndNode(endNode);
                action.setEnd(endNode);
                setResponsePage(WorkflowEditor.class);
            }
        });
        form.add(new Button("cancel") {
            @Override
            public void onSubmit() {
                setResponsePage(WorkflowEditor.class);
            }
        });
        add(form);
    }

    public EndRepresentation getEnd() {
        return end;
    }

    public void setEnd(EndRepresentation end) {
        this.end = end;
    }

}
