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

package org.openengsb.ui.admin.organizeImportsPage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.tree.BaseTree;
import org.apache.wicket.extensions.markup.html.tree.LinkTree;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.workflow.api.RuleBaseException;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.ui.admin.basePage.BasePage;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "WORKFLOW_ADMIN")
@PaxWicketMountPoint(mountPoint = "imports")
public class OrganizeImportsPage extends BasePage {

    private static final long serialVersionUID = 2792895510843828152L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizeImportsPage.class);

    public static final String PAGE_NAME_KEY = "organizeImports.tile";
    public static final String PAGE_DESCRIPTION_KEY = "ogranizeImports.description";

    @Inject
    @Named("ruleManager")
    private RuleManager ruleManager;

    private String importName = "";

    private TextField<String> importField;
    private AjaxButton submitButton;
    private AjaxButton deleteButton;

    private FeedbackPanel feedbackPanel;

    @SuppressWarnings("serial")
    public OrganizeImportsPage() {
        TreeModel treeModel = createTreeModel();

        final LinkTree tree = new LinkTree("tree", treeModel)
        {
            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                DefaultMutableTreeNode mnode = (DefaultMutableTreeNode) node;
                if (!mnode.isLeaf()) {
                    return;
                }
                String imp = (String) mnode.getUserObject();
                importName = imp;

                info("");
                target.add(importField);
                target.add(feedbackPanel);
            }

        };
        tree.getTreeState().expandAll();

        add(tree);

        Form<Object> form = new Form<Object>("editForm", new CompoundPropertyModel<Object>(this));

        submitButton = new AjaxButton("submitButton", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                if (importName == null || importName.equals("")) {
                    String message = new StringResourceModel("emptyError", this, null).getString();
                    error(message);
                    target.add(feedbackPanel);
                    return;
                }

                try {
                    ruleManager.addImport(importName);
                    String message = new StringResourceModel("insertedImport", this, null).getString();
                    LOGGER.info("successfully inserted import " + importName);
                    info(importName + " " + message);
                } catch (RuleBaseException e) {
                    ruleManager.removeImport(importName);
                    LOGGER.debug("error while saving import " + importName, e);
                    String message = new StringResourceModel("savingError", this, null).getString();
                    error(importName + " " + message + "\n" + e.getLocalizedMessage());
                }
                tree.setModelObject(createTreeModel());
                importName = "";
                target.add(importField);
                target.add(tree);
                target.add(feedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Error during submitButton submit action.");
            }
        };
        submitButton.setOutputMarkupId(true);
        form.add(submitButton);

        deleteButton = new AjaxButton("deleteButton", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    ruleManager.removeImport(importName);
                    String message = new StringResourceModel("deletedImport", this, null).getString();
                    info(importName + " " + message);
                    LOGGER.info("successfully deleted import " + importName);
                } catch (RuleBaseException e) {
                    LOGGER.debug("error while deleting import " + importName, e);
                    if (e.getMessage().startsWith("Rule Compilation error")) {
                        ruleManager.addImport(importName);
                        String message = new StringResourceModel("deletingError", this, null).getString();
                        error(importName + " " + message + "\n" + e.getLocalizedMessage());
                    } else {
                        String message = new StringResourceModel("notExistingError", this, null).getString();
                        error(importName + " " + message);
                    }
                    target.add(feedbackPanel);
                    return;
                }
                tree.setModelObject(createTreeModel());

                importName = "";

                target.add(feedbackPanel);
                target.add(importField);
                target.add(tree);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                LOGGER.warn("Error during handling deleteButton request cycle.");
            }
        };
        deleteButton.setOutputMarkupId(true);
        form.add(deleteButton);

        importField = new TextField<String>("importName");
        importField.setOutputMarkupId(true);
        form.add(importField);

        add(form);

        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        add(feedbackPanel);
    }

    private TreeModel createTreeModel() {
        TreeModel model = null;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Imports");

        Collection<String> c = ruleManager.listImports();
        List<String> l = new ArrayList<String>();
        l.addAll(c);
        Collections.sort(l);

        for (String imp : l) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(imp);
            rootNode.add(child);
        }
        model = new DefaultTreeModel(rootNode);
        return model;
    }

    public String getImportName() {
        return importName;
    }

    public void setImportName(String importName) {
        this.importName = importName;
    }
}
