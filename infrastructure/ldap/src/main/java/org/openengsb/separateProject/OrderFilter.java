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

package org.openengsb.separateProject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.infrastructure.ldap.internal.model.Node;
import org.openengsb.infrastructure.ldap.util.LdapUtils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.UUIDComparator;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

/**
 * Assists in maintaining order over entries. Many Java collections allow
 * duplicates or return their elements in a predefined order. Ldap supports no
 * duplicates and does not specify how implementations return their entries. This
 * class solves the following:<br>
 * 1) Order objects. <br>
 * 2) Make non-distinct objects distinct.
 * 
 * */
public final class OrderFilter {

    public static final String ID_ATTRIBUTE = "org-openengsb-uuid";
    public static final String UNIQUE_OBJECT_OC = "org-openengsb-uniqueObject";

    private OrderFilter() {
    }

    /**
     * Adds a timebased uuid to entry.
     * 
     * @param entry
     * @param updateRdn If true, the uuid becomes the rdn. Use to handle
     *        duplicates.
     */
    public static void addId(Entry entry, boolean updateRdn) {
        String uuid = newUUID().toString();
        try {
            entry.add(SchemaConstants.objectClassAttribute, UNIQUE_OBJECT_OC);
            entry.add(ID_ATTRIBUTE, uuid);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        if (updateRdn) {
            Dn newDn = LdapUtils.concatDn(ID_ATTRIBUTE, uuid, entry.getDn().getParent());
            entry.setDn(newDn);
        }
    }

    /**
     * Iterates over entries and adds a timebased uuid to each entry.
     * 
     * @param entries
     * @param updateRdn If true, the uuid becomes the rdn. Use to handle
     *        duplicates.
     */
    public static void addIds(List<Entry> entries, boolean updateRdn) {
        for (Entry entry : entries) {
            addId(entry, updateRdn);
        }
    }

    public static void sortById(List<Entry> entries) {
        Collections.sort(entries, new IdComparator());
    }

    
    public static void sortByIdNode(List<Node> nodes) {
        Collections.sort(nodes, new IdComparatorNode());
    }

    public static String extractIdAttribute(Entry entry) {
        return LdapUtils.extractAttributeNoEmptyCheck(entry, ID_ATTRIBUTE);
    }

    private static UUID newUUID() {
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
        return uuidGenerator.generate();
    }

    private static class IdComparator implements Comparator<Entry> {

        @Override
        public int compare(Entry e1, Entry e2) {
            String id1 = extractIdAttribute(e1);
            String id2 = extractIdAttribute(e2);

            if (id1 == null && id2 == null) {
                return 0;
            }
            if (id1 == null && id2 != null) {
                return -1;
            }
            if (id1 != null && id2 == null) {
                return 1;
            }
            return UUIDComparator.staticCompare(UUID.fromString(id1), UUID.fromString(id2));
        }

    }

    private static class IdComparatorNode implements Comparator<Node> {

        @Override
        public int compare(Node n1, Node n2) {

            Entry e1 = n1.getEntry();
            Entry e2 = n2.getEntry();

            if (e1 == null && e2 == null) {
                return 0;
            }
            if (e1 == null && e2 != null) {
                return -1;
            }
            if (e1 != null && e2 == null) {
                return 1;
            }
            return new IdComparator().compare(e1, e2);
        }
    }

}
