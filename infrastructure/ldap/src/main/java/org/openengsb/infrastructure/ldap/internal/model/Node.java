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

package org.openengsb.infrastructure.ldap.internal.model;//TODO: this should not be in internal package

import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;

import com.google.common.base.Objects;

/**
 * This class is used to represent the tree structure of the DIT or any subtree.
 * */
public class Node {

    private Node parent;
    private List<Node> children;
    private Entry entry;

    /**
     * Constructs a new node with given {@link Entry}.
     * */
    public Node(Entry entry) {
        this.entry = entry;
    }

    /**
     * Returns the immediate ancestor of this node.
     * */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the immediate ancestor of this node.
     * */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Returns a list of this node's children.
     * */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Sets this node's children.
     * */
    public void setChildren(List<Node> children) {
        this.children = children;
    }

    /**
     * Returns this node's underlying {@link Entry}.
     * */
    public Entry getEntry() {
        return entry;
    }

    /**
     * Sets this node's underlying {@link Entry}.
     * */
    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    /**
     * Two Nodes are equal if they have the same {@link Entry}, parent and children.
     * */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        }
        final Node other = (Node) obj;
        return Objects.equal(other.parent, this.parent) && Objects.equal(other.entry, this.entry)
            && Objects.equal(other.children, this.children);
    }

    @Override
    public String toString() {
        String s = entry.getDn().getName();
        for (Node node : children) {
            s += "\n" + node.toString();
        }
        return s;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parent, children, entry);
    }

}
