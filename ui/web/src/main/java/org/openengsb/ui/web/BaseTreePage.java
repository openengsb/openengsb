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

import java.util.Enumeration;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openengsb.ui.web.tree.ModelBean;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.tree.AbstractTree;


public abstract class BaseTreePage extends BasePage {

    public BaseTreePage() {

        add(new AjaxLink("expandAll") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                getTree().getTreeState().expandAll();
                getTree().updateTree(target);
            }
        });

        add(new AjaxLink("collapseAll") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                getTree().getTreeState().collapseAll();
                getTree().updateTree(target);
            }
        });

        add(new AjaxLink("switchRootless") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                getTree().setRootLess(!getTree().isRootLess());
                getTree().updateTree(target);
            }
        });
    }

    protected abstract AbstractTree getTree();

    protected TreeModel createTreeModel(Map<String, String> context) {
        return new TreeModel() {

            @Override
            public Object getRoot() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getChild(Object parent, int index) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getChildCount(Object parent) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isLeaf(Object node) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getIndexOfChild(Object parent, Object child) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void addTreeModelListener(TreeModelListener l) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void removeTreeModelListener(TreeModelListener l) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    private TreeModel convertToTreeModel(Map<String, String> context) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new ModelBean("root", null));
        for (String key : context.keySet()) {
            String[] nodes = key.split("/");
            DefaultMutableTreeNode currentNode = rootNode;
            for (String node : nodes) {
                if (currentNode.isLeaf()) {
                    currentNode.add(new DefaultMutableTreeNode(new ModelBean(node, "bla")));
                }
                for (@SuppressWarnings("unchecked") Enumeration<DefaultMutableTreeNode> subnodes = currentNode.children(); subnodes.hasMoreElements();) {
                    TreeNode subnode = subnodes.nextElement();
                }

            }

        }
        return new DefaultTreeModel(rootNode);
    }
//    private void add(DefaultMutableTreeNode parent, List<Object> sub) {
//        for (Iterator<Object> i = sub.iterator(); i.hasNext();) {
//            Object o = i.next();
//            if (o instanceof List) {
//                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ModelBean());
//                parent.add(child);
//                add(child, (List<Object>) o);
//            } else {
//                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ModelBean());
//                parent.add(child);
//            }
//        }
//    }
}
