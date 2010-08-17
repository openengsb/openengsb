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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

public class SendMessages extends BasePage {

    @SpringBean
    DomainService domainService;

    private final BaseTree tree;

    public SendMessages() {
        tree = new ClickableLinkTree("tree", createModel());
        tree.setRootLess(true);
        add(tree);
        tree.getTreeState().collapseAll();
    }

    private TreeModel createModel() {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Select Instance");
        TreeModel model = new DefaultTreeModel(node);
        for (DomainProvider provider : domainService.domains()) {
            this.addDomainProvider(provider, node);
        }
        return model;
    }

    private void addDomainProvider(DomainProvider provider, DefaultMutableTreeNode node) {
        DefaultMutableTreeNode providerNode = new DefaultMutableTreeNode(provider.getName());
        node.add(providerNode);
        for (ServiceManager manager : domainService.serviceManagersForDomain(provider.getDomainInterface())) {
            DefaultMutableTreeNode serviceManagerNode = new DefaultMutableTreeNode(manager.getDescriptor().getName());
            providerNode.add(serviceManagerNode);
            for (ServiceReference serviceReference : this.domainService.serviceReferencesForConnector(provider
                    .getDomainInterface())) {
                DefaultMutableTreeNode referenceNode = new DefaultMutableTreeNode(serviceReference, false);
                serviceManagerNode.add(referenceNode);
            }
        }
    }

    private static class ClickableLinkTree extends LinkTree {

        public ClickableLinkTree(String name, TreeModel model) {
            super(name, model);
        }

        @Override
        protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                if (treeNode.isLeaf()) {
                    ServiceReference reference = (ServiceReference) treeNode.getUserObject();
                    Object service = reference.getBundle().getBundleContext().getService(reference);
                    if (service instanceof ExampleDomain) {
                        ((ExampleDomain) service).doSomething("Hello World");
                    }
                }
            }
        }
    }
}