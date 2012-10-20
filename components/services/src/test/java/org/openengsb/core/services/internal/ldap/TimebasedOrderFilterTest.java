package org.openengsb.core.services.internal.ldap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.junit.Test;
import org.openengsb.core.services.internal.security.ldap.TimebasedOrderFilter;
import org.openengsb.infrastructure.ldap.internal.LdapDaoException;
import org.openengsb.infrastructure.ldap.internal.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimebasedOrderFilterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimebasedOrderFilterTest.class);

    @Test
    public void testAddId_expectIdAttribute() throws LdapDaoException {
        Entry e = new DefaultEntry();
        assertThat(e.containsAttribute(TimebasedOrderFilter.ID_ATTRIBUTE), is(false));
        TimebasedOrderFilter.addId(e, false);
        assertThat(e.containsAttribute(TimebasedOrderFilter.ID_ATTRIBUTE), is(true));
    }

    @Test
    public void testAddIdsSeparately_expectIncreasingOrder() throws LdapInvalidAttributeValueException, LdapDaoException {
        Entry e1 = new DefaultEntry();
        Entry e2 = new DefaultEntry();
        TimebasedOrderFilter.addId(e1, false);
        TimebasedOrderFilter.addId(e2, false);
        String id1 = e1.get(TimebasedOrderFilter.ID_ATTRIBUTE).getString();
        String id2 = e2.get(TimebasedOrderFilter.ID_ATTRIBUTE).getString();
        assertThat(id1, lessThan(id2));
        LOGGER.debug(id1);
        LOGGER.debug(id2);
    }

    @Test
    public void testAddIdsOnList_expectIncreasingOrder() throws LdapInvalidAttributeValueException, LdapDaoException {
        List<Entry> entries = new LinkedList<Entry>();
        Entry e1;
        Entry e2;
        String id1;
        String id2;
        // populate list
        for (int i = 0; i < 20; i++) {
            entries.add(new DefaultEntry());
        }
        TimebasedOrderFilter.addIds(entries, false);
        Iterator<Entry> it = entries.iterator();
        e1 = it.next();
        // compare ids
        while (it.hasNext()) {
            e2 = it.next();
            id1 = e1.get(TimebasedOrderFilter.ID_ATTRIBUTE).getString();
            id2 = e2.get(TimebasedOrderFilter.ID_ATTRIBUTE).getString();
            assertThat(id1, lessThan(id2));
            e1 = e2;
        }
    }

    @Test
    public void testAddIdUpdateRdnOnEmptyDn_expectCreateRdn() throws LdapInvalidAttributeValueException, LdapDaoException {
        Entry e = new DefaultEntry();
        assertThat(e.getDn(), is(Dn.EMPTY_DN));
        TimebasedOrderFilter.addId(e, true);
        Rdn rdn = e.getDn().getRdn();
        assertThat(rdn.getType(), is(TimebasedOrderFilter.ID_ATTRIBUTE));
        assertThat(rdn.getValue().getString(), is(TimebasedOrderFilter.extractIdAttribute(e)));
    }

    @Test
    public void testAddIdUpdateExistingRdn_expectReplaceRdn() throws LdapInvalidDnException,
        LdapInvalidAttributeValueException, LdapDaoException {
        Rdn rdnUnchanged = new Rdn("ou", "parent");
        // give a dn of dept 2 to the entry
        Entry e = new DefaultEntry(new Dn(new Rdn("ou", "child"), rdnUnchanged));
        // add id with update flag set
        TimebasedOrderFilter.addId(e, true);
        // make sure that the parent part of the dn remains unchanged
        assertThat(e.getDn().getRdn(1), is(rdnUnchanged));
        // make sure that the child part has been updated
        Rdn rdnUpdated = e.getDn().getRdn();
        assertThat(rdnUpdated.getType(), is(TimebasedOrderFilter.ID_ATTRIBUTE));
        assertThat(rdnUpdated.getValue().getString(), is(TimebasedOrderFilter.extractIdAttribute(e)));
    }

    @Test
    public void testSortById_expectSorted() throws LdapDaoException {
        List<Entry> original = new LinkedList<Entry>();
        List<Entry> reversed = new LinkedList<Entry>();
        // add 2 entries to first list
        original.add(new DefaultEntry());
        original.add(new DefaultEntry());
        // mark both entries with an id
        TimebasedOrderFilter.addIds(original, false);
        // add the same entries to second list but in reversed order
        reversed.add(original.get(1));
        reversed.add(original.get(0));
        // sort the second list
        TimebasedOrderFilter.sortById(reversed);
        // now both lists must have the same order
        assertThat(reversed.get(0), is(original.get(0)));
        assertThat(reversed.get(1), is(original.get(1)));
    }

    @Test
    public void testSortByIdNode_expectSorted() throws LdapDaoException {
        List<Node> original = new LinkedList<Node>();
        List<Node> reversed = new LinkedList<Node>();
        original.add(new Node(new DefaultEntry()));
        original.add(new Node(new DefaultEntry()));
        TimebasedOrderFilter.addId(original.get(0).getEntry(), false);
        TimebasedOrderFilter.addId(original.get(1).getEntry(), false);
        reversed.add(original.get(1));
        reversed.add(original.get(0));
        TimebasedOrderFilter.sortByIdNode(reversed);
        assertThat(reversed.get(0), is(original.get(0)));
        assertThat(reversed.get(1), is(original.get(1)));
    }

}
