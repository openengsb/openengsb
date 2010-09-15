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

package org.openengsb.ui.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.Context;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.context.ContextService;
import org.openengsb.ui.web.service.DomainService;
import org.openengsb.ui.web.tree.ModelBean;
import org.openengsb.ui.web.tree.PropertyEditableColumn;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

@SuppressWarnings("serial")
public class ContextSetPage extends BasePage {

    @SpringBean
    private ContextService contextService;

    @SpringBean
    private DomainService domainService;

    @SpringBean
    private ContextCurrentService contextCurrentService;

    private final TreeTable tree;

    public ContextSetPage() {
        add(new Label("currentContextId", new LoadableDetachableModel<String>() {

            @Override
            protected String load() {
                return contextCurrentService.getCurrentContextId();
            }
        }));

        add(new AjaxLink<String>("expandAll") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                getTree().getTreeState().expandAll();
                getTree().updateTree(target);
            }
        });

        add(new AjaxLink<String>("collapseAll") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                getTree().getTreeState().collapseAll();
                getTree().updateTree(target);
            }
        });
        IColumn[] columns = new IColumn[]{
            new PropertyTreeColumn(new ColumnLocation(Alignment.LEFT, 18, Unit.EM), "Tree Column",
                "userObject.niceKey"),
            new PropertyEditableColumn(new ColumnLocation(Alignment.LEFT, 12, Unit.EM), "value", "userObject.value",
                domainService)};
        Form<Object> form = new Form<Object>("form");
        Context context = contextService.getContext();
        this.tree = new TreeTable("treeTable", createTreeModel(context), columns);
        this.tree.setRootLess(true);
        this.tree.getTreeState().expandAll();
        form.add(this.tree);
        add(form);
    }

    protected AbstractTree getTree() {
        return this.tree;
    }

    private TreeModel createTreeModel(Context context) {
        return new DefaultTreeModel(createTreeNode(context, "/"));
    }

    private DefaultMutableTreeNode createTreeNode(Context context, String path) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ModelBean(contextService, path, false));
        for (String key : context.getKeys()) {
            node.add(new DefaultMutableTreeNode(new ModelBean(contextService, path + key, true)));
        }
        for (String childName : context.getChildren().keySet()) {
            Context child = context.getChild(childName);
            node.add(createTreeNode(child, path + childName + '/'));
        }
        return node;
    }
}
