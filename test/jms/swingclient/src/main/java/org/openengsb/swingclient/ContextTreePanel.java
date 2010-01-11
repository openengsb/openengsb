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
package org.openengsb.swingclient;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openengsb.contextcommon.Context;

public class ContextTreePanel extends JPanel {

    private JTree tree;

    private ContextTreeNode root;

    private ContextPanel contextPanel;

    public ContextTreePanel(ContextPanel contextPanel) {
        this.contextPanel = contextPanel;
        this.setLayout(new BorderLayout());
        tree = new JTree(new DefaultTreeModel(new ContextTreeNode("/")));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new ContextTreeSelectionListener());
        this.add(tree, BorderLayout.CENTER);
    }

    public void updateTree(Context context) {
        int[] selectionRows = tree.getSelectionRows();
        root = transform(context, "", "/");
        tree.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        tree.setSelectionRows(selectionRows);
    }

    public void selectRoot() {
        tree.setSelectionPath(new TreePath(root));
    }

    private ContextTreeNode transform(Context context, String path, String name) {
        ContextTreeNode current = new ContextTreeNode(name);
        current.setValues(toList(getValueMap(context), path));
        for (String child : context.getChildrenNames()) {
            current.addChild(transform(context.getChild(child), path + child + "/", child));
        }
        return current;
    }

    private List<ContextEntry> toList(Map<String, String> values, String path) {
        List<ContextEntry> elements = new ArrayList<ContextEntry>();
        for (Entry<String, String> e : values.entrySet()) {
            elements.add(new ContextEntry(path, e.getKey(), e.getValue()));
        }
        return elements;
    }

    private Map<String, String> getValueMap(Context context) {
        Map<String, String> values = new HashMap<String, String>();
        for (String key : context.getKeys()) {
            values.put(key, context.get(key));
        }
        return values;
    }

    public static class ContextTreeNode implements TreeNode {

        public ContextTreeNode(String name) {
            this.name = name;
        }

        private List<TreeNode> children = new ArrayList<TreeNode>();

        private String name;

        private List<ContextEntry> values;

        private TreeNode parent;

        @Override
        public Enumeration<TreeNode> children() {
            return children();
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public TreeNode getParent() {
            return this.parent;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        public void setParent(ContextTreeNode parent) {
            this.parent = parent;
        }

        public void addChild(ContextTreeNode child) {
            child.setParent(this);
            this.children.add(child);
        }

        public void removeChild(ContextTreeNode child) {
            if (this.children.remove(child)) {
                child.setParent(null);
            }
        }

        public void setValues(List<ContextEntry> values) {
            this.values = values;
        }

        public List<ContextEntry> getValues() {
            return values;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class ContextTreeSelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
            if (newLeadSelectionPath == null) {
                return;
            }
            ContextTreeNode selectedNode = (ContextTreeNode) newLeadSelectionPath.getLastPathComponent();
            contextPanel.updateModel(selectedNode.getValues());
        }

    }

}
