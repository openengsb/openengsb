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

package org.openengsb.ui.admin.workflowEditor;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.tree.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.workflow.editor.Action;
import org.openengsb.core.common.workflow.editor.Event;
import org.openengsb.core.common.workflow.editor.Node;
import org.openengsb.core.common.workflow.editor.Workflow;
import org.openengsb.core.common.workflow.editor.WorkflowEditorService;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.workflowEditor.action.ActionLinks;
import org.openengsb.ui.admin.workflowEditor.action.EditAction;
import org.openengsb.ui.admin.workflowEditor.event.EventLinks;

@AuthorizeInstantiation("ROLE_USER")
public class WorkflowEditor extends BasePage {

    private final TreeTable table;

    private String selected = "";

    private String name = "";

    @SpringBean
    WorkflowEditorService workflowEditorService;

    public WorkflowEditor() {
        Form<Object> selectForm = new Form<Object>("workflowSelectForm") {
            @Override
            protected void onSubmit() {
                workflowEditorService.loadWorkflow(selected);
                setResponsePage(WorkflowEditor.class);
            }
        };
        selectForm.add(new DropDownChoice<String>("workflowSelect", new PropertyModel<String>(this, "selected"),
            workflowEditorService.getWorkflowNames()));
        add(selectForm);

        Form<Object> createForm = new Form<Object>("workflowCreateForm") {
            @Override
            protected void onSubmit() {
                if (name != "" && name != null) {
                    workflowEditorService.createWorkflow(name);
                    name = "";
                    setResponsePage(new EditAction(null, workflowEditorService.getCurrentWorkflow().getRoot()));
                }
            }
        };
        createForm.add(new TextField<String>("name", new PropertyModel<String>(this, "name")));
        add(createForm);

        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        Workflow currentWorkflow = workflowEditorService.getCurrentWorkflow();
        DefaultTreeModel model = new DefaultTreeModel(node);
        IColumn[] columns =
            new IColumn[]{
                new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 8, Unit.PROPORTIONAL), "Name",
                    "userObject.description"),
                new AbstractColumn(new ColumnLocation(Alignment.MIDDLE, 4, Unit.PROPORTIONAL), "Add Action") {
                    @Override
                    public Component newCell(MarkupContainer parent, String id, final TreeNode node, int level) {
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                        Node userObject = (Node) treeNode.getUserObject();
                        if (userObject instanceof Action) {
                            return new ActionLinks("links", (Action) userObject, treeNode);
                        }
                        if (userObject instanceof Event) {
                            return new EventLinks("id", (Event) userObject, treeNode);
                        }
                        return null;
                    }

                    @Override
                    public IRenderable newCell(TreeNode node, int level) {
                        return null;
                    }
                }};

        table = new TreeTable("treeTable", model, columns);
        String label = "";
        if (currentWorkflow == null) {
            label = getString("workflow.create.first");
            node.setUserObject(new Action());
            table.setVisible(false);
            selectForm.setVisible(false);
        } else {
            label = currentWorkflow.getName();
            node.setUserObject(currentWorkflow.getRoot());
            Action root = currentWorkflow.getRoot();
            addActionsToNode(root.getActions(), node);
            addEventsToNode(root.getEvents(), node);
        }
        add(new Label("currentWorkflowName", label));
        table.getTreeState().expandAll();
        add(table);
    }

    private void addTreeNodeForEvent(DefaultMutableTreeNode node, Event event) {
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(event);
        addActionsToNode(event.getActions(), newChild);
        node.add(newChild);
    }

    private void addActionsToNode(List<Action> actions, DefaultMutableTreeNode newChild) {
        for (Action childAction : actions) {
            addTreeNodeForAction(newChild, childAction);
        }
    }

    private void addEventsToNode(List<Event> events, DefaultMutableTreeNode newChild) {
        for (Event childEvent : events) {
            addTreeNodeForEvent(newChild, childEvent);
        }
    }

    private void addTreeNodeForAction(DefaultMutableTreeNode node, Action action) {
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(action);
        addActionsToNode(action.getActions(), newChild);
        addEventsToNode(action.getEvents(), newChild);
        node.add(newChild);
    }

    public final void setWorkflowEditorService(WorkflowEditorService workflowEditorService) {
        this.workflowEditorService = workflowEditorService;
    }

    public final String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public final String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
