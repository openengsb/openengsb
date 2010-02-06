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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openengsb.contextcommon.Context;

public class ContextTreePanel extends JPanel {

    JTree tree;

    private ContextTreeNode root;

    ContextPanel contextPanel;

    public ContextTreePanel(ContextPanel contextPanel) {
        this.contextPanel = contextPanel;
        this.setLayout(new BorderLayout());
        tree = new JTree(new DefaultTreeModel(new ContextTreeNode("/", "")));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new ContextTreeSelectionListener());

        JPopupMenu popup = new JPopupMenu();

        JMenuItem newItem = new JMenuItem("New Subtree");
        JMenuItem deleteItem = new JMenuItem("Delete Subtree");

        newItem.addActionListener(new NewContextSubTreeAction(this));
        deleteItem.addActionListener(new DeleteContextSubTreeAction(this));

        popup.add(newItem);
        popup.add(deleteItem);

        tree.addMouseListener(new PopupListener(popup));

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

    public ContextTreeNode getSelectedNode() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath == null) {
            return null;
        }
        return (ContextTreeNode) selectionPath.getLastPathComponent();
    }

    private ContextTreeNode transform(Context context, String path, String name) {
        ContextTreeNode current = new ContextTreeNode(name, path);
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

    private class PopupListener extends MouseAdapter {
        private JPopupMenu popup;

        public PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int row = tree.getRowForLocation(e.getX(), e.getY());
                tree.setSelectionRow(row);
                if (row == -1) {
                    return;
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public static class ContextTreeNode implements TreeNode {

        private List<TreeNode> children = new ArrayList<TreeNode>();

        private String name;

        private String path;

        private List<ContextEntry> values;

        private TreeNode parent;

        public ContextTreeNode(String name, String path) {
            this.name = name;
            this.path = path;
        }

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

        public String getPath() {
            return path;
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
                contextPanel.table.newEntry.setEnabled(false);
                return;
            }
            contextPanel.table.newEntry.setEnabled(true);
            ContextTreeNode selectedNode = (ContextTreeNode) newLeadSelectionPath.getLastPathComponent();
            contextPanel.table.updateModel(selectedNode.getValues());

        }

    }

}
