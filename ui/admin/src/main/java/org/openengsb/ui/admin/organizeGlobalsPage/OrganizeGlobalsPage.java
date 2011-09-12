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

package org.openengsb.ui.admin.organizeGlobalsPage;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.common.SecurityAttribute;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecurityAttribute("WORKFLOW_ADMIN")
@PaxWicketMountPoint(mountPoint = "globals")
public class OrganizeGlobalsPage extends BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizeGlobalsPage.class);

    @PaxWicketBean
    private RuleManager ruleManager;

    private String globalName = "";
    private String className = "";

    private TextField<String> nameField;
    private TextField<String> classNameField;
    private AjaxButton submitButton;
    private AjaxButton deleteButton;

    private FeedbackPanel feedbackPanel;

    @SuppressWarnings("serial")
    public OrganizeGlobalsPage() {
        TreeModel treeModel = createTreeModel();

        final LinkTree tree = new LinkTree("tree", treeModel)
        {
            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) node;
                if (!mnode.isLeaf()) {
                    return;
                }
                Global global = (Global) mnode.getUserObject();
                globalName = global.getName();
                className = global.getClassname();
                target.addComponent(nameField);
                target.addComponent(classNameField);
            }

        };
        tree.getTreeState().expandAll();

        add(tree);

        Form<Object> form = new Form<Object>("editForm", new CompoundPropertyModel<Object>(this));

        submitButton = new AjaxButton("submitButton", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                if (globalName == null || globalName.equals("") || className == null || className.equals("")) {
                    String message = new StringResourceModel("emptyError", this, null).getString();
                    error(message);
                    target.addComponent(feedbackPanel);
                    return;
                }

                try {
                    ruleManager.addGlobal(className, globalName);
                    String message = new StringResourceModel("insertedGlobal", this, null).getString();
                    info(globalName + " " + message);
                    LOGGER.info("successfully inserted global " + globalName);
                } catch (RuleBaseException e) {
                    LOGGER.debug("error while inserting global " + globalName, e);
                    String temp = ruleManager.getGlobalType(globalName);
                    try {
                        // it comes here if the global already exists
                        ruleManager.removeGlobal(globalName);
                        ruleManager.addGlobal(className, globalName);
                        String message = new StringResourceModel("updatedGlobal", this, null).getString();
                        info(globalName + " " + message);
                    } catch (RuleBaseException ex) {
                        LOGGER.debug("error while updating global " + globalName, e);
                        // it restores the old value if the new value for global is invalid
                        ruleManager.removeGlobal(globalName);
                        ruleManager.addGlobal(temp, globalName);
                        String message = new StringResourceModel("savingError", this, null).getString();
                        error(globalName + " " + message + "\n" + ex.getLocalizedMessage());
                    }
                }
                tree.setModelObject(createTreeModel());
                globalName = "";
                className = "";

                target.addComponent(nameField);
                target.addComponent(classNameField);
                target.addComponent(tree);
                target.addComponent(feedbackPanel);
            }

        };
        submitButton.setOutputMarkupId(true);
        form.add(submitButton);

        deleteButton = new AjaxButton("deleteButton", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String temp = ruleManager.getGlobalType(globalName);
                try {
                    ruleManager.removeGlobal(globalName);
                    String message = new StringResourceModel("deletedGlobal", this, null).getString();
                    info(globalName + " " + message);
                    LOGGER.info("successfully deleted global " + globalName);
                } catch (RuleBaseException e) {
                    LOGGER.debug("error while deleting global " + globalName, e);
                    if (e.getMessage().startsWith("Rule Compilation error")) {
                        ruleManager.addGlobal(temp, globalName);
                        String message = new StringResourceModel("deletingError", this, null).getString();
                        error(globalName + " " + message + "\n" + e.getLocalizedMessage());
                    } else {
                        String message = new StringResourceModel("notExistingError", this, null).getString();
                        error(globalName + " " + message);
                    }
                    target.addComponent(feedbackPanel);
                    return;
                }
                tree.setModelObject(createTreeModel());

                globalName = "";
                className = "";

                target.addComponent(feedbackPanel);
                target.addComponent(nameField);
                target.addComponent(classNameField);
                target.addComponent(tree);
            }
        };
        deleteButton.setOutputMarkupId(true);
        form.add(deleteButton);

        nameField = new TextField<String>("globalName");
        nameField.setOutputMarkupId(true);
        form.add(nameField);

        classNameField = new TextField<String>("className");
        classNameField.setOutputMarkupId(true);
        form.add(classNameField);

        add(form);

        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        add(feedbackPanel);
    }

    private TreeModel createTreeModel() {
        Map<String, String> globals = ruleManager.listGlobals();
        ArrayList<Global> glob = new ArrayList<Global>();
        for (String key : globals.keySet()) {
            glob.add(new Global(key, globals.get(key)));
        }

        TreeModel model = null;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Globals");
        for (Global global : glob) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(global);
            rootNode.add(child);
        }
        model = new DefaultTreeModel(rootNode);
        return model;
    }

    public String getGlobalName() {
        return globalName;
    }

    public void setGlobalName(String globalName) {
        this.globalName = globalName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
