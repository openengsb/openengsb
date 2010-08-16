package org.openengsb.ui.web;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

public class SendMessages extends BasePage {

    @SpringBean
    DomainService domainService;

    Log log = LogFactory.getLog(SendMessages.class);

    private BaseTree tree;

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
                    System.out.println(reference);
                }
            }
        }
    }
}