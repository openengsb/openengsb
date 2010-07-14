package org.openengsb.persistence;

import java.util.Collection;
import java.util.HashSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeTraverser {

    public static class Condition {
        public String key;
        public String value;

        public Condition(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }
    }

    private Collection<Condition> conditions = new HashSet<Condition>();

    protected NodeTraverser() {
    }

    /*
     * a node is considered a Leaf when its only cildnode is one textnode
     */
    private static boolean isLeaf(Node node) {
        NodeList children = node.getChildNodes();
        if (children.getLength() != 1) {
            return false;
        }
        return children.item(0).getNodeType() == Node.TEXT_NODE;
    }

    private static String nodeGetTextContent(Node n) {
        return n.getChildNodes().item(0).getNodeValue();
    }

    private void traverseRootNode(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            traverseNode(child, "");
        }
    }

    private void traverseNode(Node node, String parent) {
        String key;
        if (parent.isEmpty()) {
            key = node.getNodeName();
        } else {
            key = parent + "/" + node.getNodeName();
        }
        if (isLeaf(node)) {
            String value = nodeGetTextContent(node); // node.getTextContent();
            conditions.add(new Condition(key, value));
            return;
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            traverseNode(child, key);
        }
    }

    public static Collection<Condition> getConditions(Node node) {
        NodeTraverser n = new NodeTraverser();
        if (node instanceof Document) {
            Document doc = (Document) node;
            node = doc.getDocumentElement();
        }
        n.traverseRootNode(node);
        return n.conditions;
    }

}
