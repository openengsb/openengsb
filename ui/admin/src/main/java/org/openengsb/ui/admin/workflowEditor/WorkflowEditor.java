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
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.security.SecurityAttribute;
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
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "WORKFLOW_ADMIN")
@PaxWicketMountPoint(mountPoint = "workflows")
public class WorkflowEditor extends BasePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowEditor.class);

    private TreeTable table;

    private String selected = "";

    private String name = "";

    @PaxWicketBean
    private WorkflowEditorService workflowEditorService;

    @PaxWicketBean
    RuleManager ruleManager;

    @PaxWicketBean
    WorkflowConverter workflowConverter;

    @PaxWicketBean
    List<WorkflowValidator> validators;

    @SuppressWarnings("unchecked")
    public WorkflowEditor() {
        createFeedbackPanel();
        try {
            workflowEditorService.loadWorkflowsFromDatabase();
        } catch (PersistenceException e1) {
            error(e1.getMessage());
        }
        Form<Object> selectForm = createSelectWorkflowForm();
        createCreateWorkflowForm();
        Form<Object> removeWorkflowForm = createRemoveWorkflowForm();
        Form<Object> exportForm = createExportForm();
        Form<Object> saveForm = createSaveForm();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        final Model<WorkflowRepresentation> currentworkflow =
            new Model<WorkflowRepresentation>(workflowEditorService.getCurrentWorkflow());
        String label = "";
        if (workflowEditorService.getWorkflowNames().size() == 0) {
            selectForm.setVisible(false);
        }
        createTable(node, currentworkflow);
        if (currentworkflow.getObject() == null) {
            label = getString("workflow.create.first");
            node.setUserObject(new ActionRepresentation());
            setFormsInvisible(removeWorkflowForm, exportForm, saveForm);
        } else {
            label = currentworkflow.getObject().getName();
            node.setUserObject(currentworkflow.getObject().getRoot());
            ActionRepresentation root = currentworkflow.getObject().getRoot();
            addActionsToNode(root.getActions(), node);
            addEventsToNode(root.getEvents(), node);
        }
        add(new Label("currentWorkflowName", label));
    }

    public void createFeedbackPanel() {
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }

    private void createTable(DefaultMutableTreeNode node, final Model<WorkflowRepresentation> currentworkflow) {
        DefaultTreeModel model = new DefaultTreeModel(node);
        IColumn[] columns = createTableColumns(currentworkflow);
        table = new TreeTable("treeTable", model, columns);
        table.getTreeState().expandAll();
        add(table);
    }

    private void setFormsInvisible(Form<Object>... exportForms) {
        table.setVisible(false);
        for (Form<Object> form : exportForms) {
            form.setVisible(false);
        }
    }

    private IColumn[] createTableColumns(final Model<WorkflowRepresentation> currentworkflow) {
        IColumn[] columns =
            new IColumn[]{
                new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 8, Unit.PROPORTIONAL), "Name",
                    "userObject.description"),
                new AbstractColumn(new ColumnLocation(Alignment.MIDDLE, 4, Unit.PROPORTIONAL), "Add Action") {
                    private static final long serialVersionUID = 742666782257868383L;

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
        return columns;
    }

    private Form<Object> createSelectWorkflowForm() {
        Form<Object> selectForm = new Form<Object>("workflowSelectForm") {
            private static final long serialVersionUID = 1030454289375165169L;

            @Override
            protected void onSubmit() {
                workflowEditorService.loadWorkflow(selected);
                setResponsePage(WorkflowEditor.class);
            }
        };
        selectForm.add(new DropDownChoice<String>("workflowSelect", new PropertyModel<String>(this, "selected"),
            workflowEditorService.getWorkflowNames()));
        add(selectForm);
        return selectForm;
    }

    private void createCreateWorkflowForm() {
        Form<Object> createForm = new Form<Object>("workflowCreateForm") {
            private static final long serialVersionUID = -1254767212676185569L;

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
    }

    private Form<Object> createRemoveWorkflowForm() {
        Form<Object> removeForm = new Form<Object>("workflowRemoveForm") {
            private static final long serialVersionUID = 2916102299731955162L;

            @Override
            protected void onSubmit() {
                try {
                    LOGGER.info("Removing workflow " + workflowEditorService.getCurrentWorkflow().getName());
                    workflowEditorService.removeCurrentWorkflow();
                    setResponsePage(WorkflowEditor.class);

                } catch (PersistenceException e) {
                    error(e);
                    LOGGER.error("Removing Workflow failed: " + e.getMessage());
                }
            }
        };
        add(removeForm);
        return removeForm;
    }

    private Form<Object> createSaveForm() {
        Form<Object> saveForm = new Form<Object>("saveForm") {
            private static final long serialVersionUID = 2916102299731955162L;

            @Override
            protected void onSubmit() {
                try {
                    workflowEditorService.saveCurrentWorkflow();
                } catch (PersistenceException e) {
                    error(e.getMessage());
                }
            }
        };
        add(saveForm);
        return saveForm;
    }

    private Form<Object> createExportForm() {
        Form<Object> exportForm = new Form<Object>("export") {
            private static final long serialVersionUID = -4832165565205293266L;

            @Override
            protected void onSubmit() {
                try {
                    boolean valid = true;
                    LOGGER.info("Validation Workflow: " + workflowEditorService.getCurrentWorkflow().getName());
                    for (WorkflowValidator validator : validators) {
                        WorkflowValidationResult result =
                            validator.validate(workflowEditorService.getCurrentWorkflow());
                        valid = result.isValid() && valid;
                        for (String error : result.getErrors()) {
                            LOGGER.info("Workflow Validation Error: " + error);
                            error(error);
                        }
                    }
                    if (valid) {
                        String convert = workflowConverter.convert(workflowEditorService.getCurrentWorkflow());
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
        return exportForm;
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
