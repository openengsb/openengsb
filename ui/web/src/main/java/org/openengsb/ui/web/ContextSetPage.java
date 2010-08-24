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
package org.openengsb.ui.web;

import java.util.Map;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.markup.html.form.Form;

import org.apache.wicket.markup.html.tree.AbstractTree;
import org.openengsb.ui.web.tree.PropertyEditableColumn;

@SuppressWarnings("serial")
public class ContextSetPage extends BaseTreePage {

    private TreeTable tree;

    /**
     * Page constructor.
     */
    public ContextSetPage(Map<String, String> context) {
        IColumn columns[] = new IColumn[]{
            new PropertyTreeColumn(new ColumnLocation(Alignment.LEFT, 18, Unit.EM),
            "Tree Column", "userObject.property1"),
            new PropertyEditableColumn(new ColumnLocation(Alignment.LEFT, 12, Unit.EM), "L2",
            "userObject.property2"),
            new PropertyEditableColumn(new ColumnLocation(Alignment.MIDDLE, 2,
            Unit.PROPORTIONAL), "M1", "userObject.property3"),
            new PropertyEditableColumn(new ColumnLocation(Alignment.MIDDLE, 2,
            Unit.PROPORTIONAL), "M2", "userObject.property4"),
            new PropertyEditableColumn(new ColumnLocation(Alignment.MIDDLE, 3,
            Unit.PROPORTIONAL), "M3", "userObject.property5"),
            new PropertyEditableColumn(new ColumnLocation(Alignment.RIGHT, 8, Unit.EM), "R1",
            "userObject.property6"),};

        Form form = new Form("form");
        add(form);

        tree = new TreeTable("treeTable", createTreeModel(context), columns);
        form.add(tree);
        tree.getTreeState().collapseAll();
    }

    /**
     * @see BaseTreePage#getTree()
     */
    @Override
    protected AbstractTree getTree() {
        return tree;
    }
}
