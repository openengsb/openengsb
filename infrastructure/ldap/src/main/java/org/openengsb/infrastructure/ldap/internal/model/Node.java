package org.openengsb.infrastructure.ldap.internal.model;

import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;

import com.google.common.base.Objects;

public class Node {

    private Node parent;
    private List<Node> children;
    private Entry entry;

    public Node(){

    }

    public Node(Entry entry){
        this.entry = entry;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Node)){
            return false;
        }
        final Node other = (Node)obj;
        return Objects.equal(other.parent, this.parent)
                && Objects.equal(other.entry, this.entry)
                && Objects.equal(other.children, this.children);
    }

    @Override
    public String toString() {
        String s = entry.getDn().getName();
        for(Node node : children){
            s += "\n" + node.toString();
        }
        return s;
    }

}
