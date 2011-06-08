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

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;
import org.openengsb.ui.admin.workflowEditor.end.SetEnd;
import org.openengsb.ui.admin.workflowEditor.event.EditEvent;

public class ActionLinks extends Panel {
    private static final long serialVersionUID = -126359261924290715L;

    public ActionLinks(String id, final ActionRepresentation action, final DefaultMutableTreeNode treeNode,
             final Model<WorkflowRepresentation> workflow) {
        super(id);
        add(new Link<DefaultMutableTreeNode>("create-action") {
            private static final long serialVersionUID = -6648019848863123255L;

            @Override
            public void onClick() {
                ActionRepresentation action2 = new ActionRepresentation();
                treeNode.add(new DefaultMutableTreeNode(action2));
                setResponsePage(new EditAction(action, action2));
            }
        });
        add(new Link<DefaultMutableTreeNode>("create-event") {
            private static final long serialVersionUID = 5145766462451841332L;

            @Override
            public void onClick() {
                EventRepresentation event = new EventRepresentation();
                setResponsePage(new EditEvent(event, action));
            }
        });
        Link<DefaultMutableTreeNode> remove = new Link<DefaultMutableTreeNode>("remove") {
            private static final long serialVersionUID = 6219724794357053341L;

            @Override
            public void onClick() {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeNode.getParent();
                Object userObject = parent.getUserObject();
                if (userObject instanceof ActionRepresentation) {
                    ((ActionRepresentation) userObject).getActions().remove(action);
                }
                if (userObject instanceof EventRepresentation) {
                    ((EventRepresentation) userObject).getActions().remove(action);
                }
                setResponsePage(WorkflowEditor.class);
            }
        };
        add(new Link<DefaultMutableTreeNode>("set-end") {
            private static final long serialVersionUID = -4353846781597001437L;

            @Override
            public void onClick() {
                Object userObject = treeNode.getUserObject();
                if (userObject instanceof ActionRepresentation) {
                    setResponsePage(new SetEnd(workflow.getObject(), (ActionRepresentation) userObject));
                } else {
                    setResponsePage(WorkflowEditor.class);
                }
            }
        });
        if (treeNode.getParent() == null) {
            remove.setVisible(false);
        }
        add(remove);
    }
}
