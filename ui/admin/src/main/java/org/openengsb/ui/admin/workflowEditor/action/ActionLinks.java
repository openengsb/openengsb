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
import org.openengsb.core.workflow.editor.Action;
import org.openengsb.core.workflow.editor.Event;
import org.openengsb.ui.admin.workflowEditor.WorkflowEditor;
import org.openengsb.ui.admin.workflowEditor.event.EditEvent;

public class ActionLinks extends Panel {

    public ActionLinks(String id, final Action action, final DefaultMutableTreeNode treeNode) {
        super(id);
        add(new Link<DefaultMutableTreeNode>("create-action") {
            @Override
            public void onClick() {
                Action action2 = new Action();
                treeNode.add(new DefaultMutableTreeNode(action2));
                setResponsePage(new EditAction(action, action2));
            }
        });
        add(new Link<DefaultMutableTreeNode>("create-event") {
            @Override
            public void onClick() {
                Event event = new Event();
                setResponsePage(new EditEvent(event, action));
            }
        });
        Link<DefaultMutableTreeNode> remove = new Link<DefaultMutableTreeNode>("remove") {
            @Override
            public void onClick() {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeNode.getParent();
                Object userObject = parent.getUserObject();
                if (userObject instanceof Action) {
                    ((Action) userObject).getActions().remove(action);
                }
                if (userObject instanceof Event) {
                    ((Event) userObject).getActions().remove(action);
                }
                setResponsePage(WorkflowEditor.class);
            }
        };
        if (treeNode.getParent() == null) {
            remove.setVisible(false);
        }
        add(remove);
    }
}
