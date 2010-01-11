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

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.openengsb.contextcommon.Context;

public class ContextTreePanel extends JPanel {

    private JTree tree;

    private ContextTreeNode root;

    public ContextTreePanel(Context context) {
        this.setLayout(new BorderLayout());
        root = transform(context, "/");
        tree = new JTree(new DefaultTreeModel(root));
        this.add(tree, BorderLayout.CENTER);
    }

    public void updateTree(Context context) {
        root = transform(context, "/");
        tree.setModel(new DefaultTreeModel(root));
    }

    private ContextTreeNode transform(Context context, String name) {
        ContextTreeNode current = new ContextTreeNode();
        current.setName(name);
        current.setValues(getValueMap(context));
        for (String child : context.getChildrenNames()) {
            current.addChild(transform(context.getChild(child), child));
        }
        return current;
    }

    private Map<String, String> getValueMap(Context context) {
        Map<String, String> values = new HashMap<String, String>();
        for (String key : context.getKeys()) {
            values.put(key, context.get(key));
        }
        return values;
    }

    public static class ContextTreeNode implements TreeNode {

        private List<TreeNode> children = new ArrayList<TreeNode>();

        private String name;

        private Map<String, String> values;

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
            return children.size() == 0;
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

        public void setValues(Map<String, String> values) {
            this.values = values;
        }

        public Map<String, String> getValues() {
            return values;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
