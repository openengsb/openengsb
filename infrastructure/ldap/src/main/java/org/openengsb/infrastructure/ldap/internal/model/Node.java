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

package org.openengsb.infrastructure.ldap.internal.model;

import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;

import com.google.common.base.Objects;

public class Node {

    private Node parent;
    private List<Node> children;
    private Entry entry;

    public Node() {

    }

    public Node(Entry entry) {
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
