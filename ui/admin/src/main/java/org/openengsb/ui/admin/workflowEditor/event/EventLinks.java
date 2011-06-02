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

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;
import org.openengsb.ui.admin.workflowEditor.action.EditAction;

public class EventLinks extends Panel {
    private static final long serialVersionUID = 8819268020993116722L;

    public EventLinks(String id, final EventRepresentation event,
            final DefaultMutableTreeNode treeNode) {
        super(id);
        add(new Link<DefaultMutableTreeNode>("create.action") {
            private static final long serialVersionUID = 7571612176992513728L;

            @Override
            public void onClick() {
                ActionRepresentation action = new ActionRepresentation();
                setResponsePage(new EditAction(event, action));
            }
        });
        add(new Link<DefaultMutableTreeNode>(
                "remove") {
            private static final long serialVersionUID = 7728882780573424915L;

            @Override
            public void onClick() {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeNode
                        .getParent();
                Object userObject = parent.getUserObject();
                if (userObject instanceof ActionRepresentation) {
                    ((ActionRepresentation) userObject).getEvents().remove(event);
                }
                setResponsePage(WorkflowEditor.class);
            }
        });
    }
}
