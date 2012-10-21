package org.openengsb.core.services.internal.ldap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.junit.Test;
import org.openengsb.core.services.internal.security.ldap.TimebasedOrderFilter;
import org.openengsb.infrastructure.ldap.internal.LdapDaoException;
import org.openengsb.infrastructure.ldap.internal.model.Node;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public class TimebasedOrderFilterTest {
    private String idAttribute = "org-openengsb-uuid";
    private String uniqueOc = "org-openengsb-uniqueObject";

    //TODO use this for injection
    public void setIdAttribute(String idAttribute){
        this.idAttribute = idAttribute;
    }
    
    public void setUniqueOc(String uniqueOc) {
        this.uniqueOc = uniqueOc;
    }
    
    @Test
    public void testAddId_expectIdAttributeAndOC() throws Exception {
        Entry e = new DefaultEntry();
        TimebasedOrderFilter.addId(e, false);
        assertThat(e.hasObjectClass(uniqueOc), is(true));
        assertThat(e.containsAttribute(idAttribute), is(true));
        assertThat(e.get(idAttribute).get(), notNullValue());
        String id = e.get(idAttribute).getString();
        assertThat(UUID.fromString(id), is(UUID.class));
    }

    @Test
    public void testAddIdsOnList_expectIdAttributeAndOC() throws Exception {
        List<Entry> entries = new LinkedList<Entry>();
        for (int i = 0; i < 5; i++) {
            entries.add(new DefaultEntry());
        }
        TimebasedOrderFilter.addIds(entries, false);
        String id;
        for (Entry e : entries) {
            assertThat(e.hasObjectClass(uniqueOc), is(true));
            assertThat(e.containsAttribute(idAttribute), is(true));
            assertThat(e.get(idAttribute).get(), notNullValue());
            id = e.get(idAttribute).getString();
            assertThat(UUID.fromString(id), is(UUID.class));
        }
    }

    @Test
    public void testAddIdUpdateRdnOnEmptyDn_expectCreateRdn() throws Exception, LdapDaoException {
        Entry e = new DefaultEntry();
        TimebasedOrderFilter.addId(e, true);
        Rdn rdn = e.getDn().getRdn();
        assertThat(rdn.getType(), is(idAttribute));
        assertThat(rdn.getValue().getString(), is(e.get(idAttribute).getString()));
    }

    @Test
    public void testAddIdUpdateExistingRdn_expectReplaceRdn() throws Exception, LdapInvalidAttributeValueException,
        LdapDaoException {
        Rdn rdnUnchanged = new Rdn("ou", "parent");
        // give a dn of depth 2 to the entry
        Entry e = new DefaultEntry(new Dn(new Rdn("ou", "child"), rdnUnchanged));
        // add id with update flag set
        TimebasedOrderFilter.addId(e, true);
        // make sure that the parent part of the dn remains unchanged
        assertThat(e.getDn().getRdn(1), is(rdnUnchanged));
        // make sure that the child part has been updated
        Rdn rdnUpdated = e.getDn().getRdn();
        assertThat(rdnUpdated.getType(), is(idAttribute));
        assertThat(rdnUpdated.getValue().getString(), is(e.get(idAttribute).getString()));
    }

    @Test
    public void testSortById_expectSorted() throws Exception {
        Entry first = newEntryWithId();
        Entry second = newEntryWithId();
        List<Entry> reversed = new LinkedList<Entry>();
        // add the entries in reversed order
        reversed.add(second);
        reversed.add(first);
        // sort them
        TimebasedOrderFilter.sortById(reversed);
        assertThat(reversed.get(0), is(first));
        assertThat(reversed.get(1), is(second));
    }

    @Test
    public void testSortByIdNode_expectSorted() throws Exception {
        Node first = new Node(newEntryWithId());
        Node second = new Node(newEntryWithId());
        List<Node> reversed = new LinkedList<Node>();
        // add the nodes in reversed order
        reversed.add(second);
        reversed.add(first);
        // sort them
        TimebasedOrderFilter.sortByIdNode(reversed);
        assertThat(reversed.get(0), is(first));
        assertThat(reversed.get(1), is(second));
    }

    private Entry newEntryWithId() throws Exception {
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
        Entry e = new DefaultEntry();
        e.add(idAttribute, uuidGenerator.generate().toString());
        return e;
    }

}
