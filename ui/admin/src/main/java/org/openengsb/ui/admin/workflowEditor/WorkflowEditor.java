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
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowConverter;
import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.WorkflowValidationResult;
import org.openengsb.core.api.workflow.WorkflowValidator;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.NodeRepresentation;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
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
    private WorkflowEditorService workflowEditorService;

    @SpringBean
    RuleManager ruleManager;

    @SpringBean
    WorkflowConverter workflowConverter;

    @SpringBean
    List<WorkflowValidator> validators;

    public WorkflowEditor() {
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        workflowEditorService.loadWorkflowsFromDatabase();
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

        Form<Object> exportForm = new Form<Object>("export") {
            @Override
            protected void onSubmit() {
                try {
                    boolean valid = true;
                    for (WorkflowValidator validator : validators) {
                        WorkflowValidationResult result =
                            validator.validate(workflowEditorService.getCurrentWorkflow());
                        valid = result.isValid() && valid;
                        for (String error : result.getErrors()) {
                            error(error);
                        }
                    }
                    if (valid) {
                        String convert = workflowConverter.convert(workflowEditorService.getCurrentWorkflow());
                        System.out.println(convert);
                        addGlobal(workflowEditorService.getCurrentWorkflow().getRoot());
                        ruleManager.add(new RuleBaseElementId(RuleBaseElementType.Process, workflowEditorService
                            .getCurrentWorkflow().getName()),
                            convert);
                    }
                } catch (RuleBaseException e) {
                    error(e.getMessage());
                }
            }
        };
        add(exportForm);

        Form<Object> saveForm = new Form<Object>("saveForm") {
            @Override
            protected void onSubmit() {
                workflowEditorService.saveCurrentWorkflow();
            }
        };
        add(saveForm);

        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        final Model<WorkflowRepresentation> currentworkflow =
            new Model<WorkflowRepresentation>(workflowEditorService.getCurrentWorkflow());
        DefaultTreeModel model = new DefaultTreeModel(node);
        IColumn[] columns =
            new IColumn[]{
                new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 8, Unit.PROPORTIONAL), "Name",
                    "userObject.description"),
                new AbstractColumn(new ColumnLocation(Alignment.MIDDLE, 4, Unit.PROPORTIONAL), "Add Action") {
                    @Override
                    public Component newCell(MarkupContainer parent, String id, final TreeNode node, int level) {
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                        NodeRepresentation userObject = (NodeRepresentation) treeNode.getUserObject();
                        if (userObject instanceof ActionRepresentation) {
                            return new ActionLinks("links", (ActionRepresentation) userObject, treeNode,
                                currentworkflow);
                        }
                        if (userObject instanceof EventRepresentation) {
                            return new EventLinks("links", (EventRepresentation) userObject, treeNode);
                        }
                        return null;
                    }

                    @Override
                    public IRenderable newCell(TreeNode node, int level) {
                        return null;
                    }
                } };

        table = new TreeTable("treeTable", model, columns);
        String label = "";
        if (workflowEditorService.getWorkflowNames().size() == 0) {
            selectForm.setVisible(false);
        }
        if (currentworkflow.getObject() == null) {
            label = getString("workflow.create.first");
            node.setUserObject(new ActionRepresentation());
            table.setVisible(false);
            exportForm.setVisible(false);
            saveForm.setVisible(false);
        } else {
            label = currentworkflow.getObject().getName();
            node.setUserObject(currentworkflow.getObject().getRoot());
            ActionRepresentation root = currentworkflow.getObject().getRoot();
            addActionsToNode(root.getActions(), node);
            addEventsToNode(root.getEvents(), node);
        }
        add(new Label("currentWorkflowName", label));
        table.getTreeState().expandAll();
        add(table);
    }

    private void addGlobal(ActionRepresentation action) {
        ruleManager.addGlobal(action.getDomain().getName(), action.getLocation());

        for (ActionRepresentation newAction : action.getActions()) {
            addGlobal(newAction);
        }
        for (EventRepresentation event : action.getEvents()) {
            addGlobalForEvents(event);
        }
    }

    private void addGlobalForEvents(EventRepresentation event) {
        for (EventRepresentation newEvent : event.getEvents()) {
            addGlobalForEvents(newEvent);
        }
        for (ActionRepresentation action : event.getActions()) {
            addGlobal(action);
        }
    }

    private void addTreeNodeForEvent(DefaultMutableTreeNode node, EventRepresentation event) {
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(event);
        addActionsToNode(event.getActions(), newChild);
        node.add(newChild);
    }

    private void addActionsToNode(List<ActionRepresentation> actions, DefaultMutableTreeNode newChild) {
        for (ActionRepresentation childAction : actions) {
            addTreeNodeForAction(newChild, childAction);
        }
    }

    private void addEventsToNode(List<EventRepresentation> events, DefaultMutableTreeNode newChild) {
        for (EventRepresentation childEvent : events) {
            addTreeNodeForEvent(newChild, childEvent);
        }
    }

    private void addTreeNodeForAction(DefaultMutableTreeNode node, ActionRepresentation action) {
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
