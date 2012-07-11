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

public class OrderFilter {

    public static final String idAttribute = "org-openengsb-uuid";
    public static final String uniqueObjectOc = "org-openengsb-uniqueObject";

    /**
     * Adds a timebased uuid to entry.
     * 
     * @param entry
     * @param updateRdn
     *            If true, the uuid becomes the rdn. Use to handle duplicates.
     */
    public static void addId(Entry entry, boolean updateRdn) {
        String uuid = newUUID().toString();
        try {
            entry.add(SchemaConstants.objectClassAttribute, uniqueObjectOc);
            entry.add(idAttribute, uuid);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        if (updateRdn) {
            Dn newDn = LdapUtils.concatDn(idAttribute, uuid, entry.getDn().getParent());
            entry.setDn(newDn);
        }
    }

    /**
     * Iterates over entries and adds a timebased uuid to each entry.
     * 
     * @param entries
     * @param updateRdn
     *            If true, the uuid becomes the rdn. Use to handle duplicates.
     */
    public static void addIds(List<Entry> entries, boolean updateRdn) {
        for (Entry entry : entries) {
            addId(entry, updateRdn);
        }
    }

    private static UUID newUUID() {
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
        return uuidGenerator.generate();
    }

    public static void sortById(List<Entry> entries) {
        Collections.sort(entries, new IdComparator());
    }

    public static void sortByIdNode(List<Node> nodes) {
        Collections.sort(nodes, new IdComparatorNode());
    }

    public static String extractIdAttribute(Entry entry) {
        return LdapUtils.extractAttributeNoEmptyCheck(entry, idAttribute);
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
