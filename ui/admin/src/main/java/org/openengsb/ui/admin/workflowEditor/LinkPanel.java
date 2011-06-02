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

package org.openengsb.ui.admin.workflowEditor;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public class LinkPanel extends Panel {
    private static final long serialVersionUID = 8289452385623401634L;

    public LinkPanel(String id, final TreeNode node, final TreeTable table, Link<?> link) {
        super(id);
        add(new Link<DefaultMutableTreeNode>("create.node") {
            private static final long serialVersionUID = 8318546187513735403L;

            @Override
            public void onClick() {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                treeNode.add(new DefaultMutableTreeNode("1"));
            }
        });
    }

}
