/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.ui.web.tree;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.model.PropertyModel;

@SuppressWarnings("serial")
public class PropertyEditableColumn extends PropertyRenderableColumn {

    public PropertyEditableColumn(ColumnLocation location, String header, String propertyExpression) {
        super(location, header, propertyExpression);
    }

    @Override
    public Component newCell(MarkupContainer parent, String id, TreeNode node, int level) {
        return new EditablePanel(id, new PropertyModel<String>(node, getPropertyExpression()));
    }

    @Override
    public IRenderable newCell(TreeNode node, int level) {
        if (getTreeTable().getTreeState().isNodeSelected(node)) {
            return null;
        } else {
            return super.newCell(node, level);
        }
    }
}
