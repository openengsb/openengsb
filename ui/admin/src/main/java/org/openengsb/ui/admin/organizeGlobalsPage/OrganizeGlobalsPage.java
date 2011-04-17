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
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.ui.admin.basePage.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("ROLE_USER")
public class OrganizeGlobalsPage extends BasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizeGlobalsPage.class);

    @SpringBean
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
                    error("at least one of the textfields is empty, please insert data");
                    target.addComponent(feedbackPanel);
                    return;
                }

                try {
                    ruleManager.addGlobal(className, globalName);
                    info("Successfully inserted global " + globalName);
                } catch (RuleBaseException e) {
                    String temp = ruleManager.getGlobalType(globalName);
                    try {
                        // it comes here if the global already exists
                        ruleManager.removeGlobal(globalName);
                        ruleManager.addGlobal(className, globalName);
                        info("Successfully updated global " + globalName);
                    } catch (RuleBaseException ex) {
                        // it restores the old value if the new value for global is invalid
                        ruleManager.removeGlobal(globalName);
                        ruleManager.addGlobal(temp, globalName);
                        error("Could not update global " + globalName + " due to:\n" + ex.getLocalizedMessage());
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
                    info("Successfully deleted global " + globalName);
                } catch (RuleBaseException e) {
                    LOGGER.error("error");
                    if (e.getMessage().startsWith("Rule Compilation error")) {
                        ruleManager.addGlobal(temp, globalName);
                        error("Could not delete global " + globalName + " due to:\n" + e.getLocalizedMessage());
                    } else {
                        error("global \"" + globalName + "\" isn't in the rulebase");
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
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("globals");
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
