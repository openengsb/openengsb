package org.openengsb.infrastructure.ldap.internal.test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.ldap.client.api.DefaultSchemaLoader;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.entry.Value;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.infrastructure.ldap.internal.EntryAlreadyExistsException;
import org.openengsb.infrastructure.ldap.internal.LdapDao;
import org.openengsb.infrastructure.ldap.internal.MissingParentException;
import org.openengsb.infrastructure.ldap.internal.NoSuchNodeException;
import org.openengsb.infrastructure.ldap.internal.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapDaoTest {

    private Dn baseDn;
    private String testEntryName;

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDaoTest.class);
    private LdapDao dao;
    private LdapConnection connection;

    private String ouOc = "organizationalUnit";
    private String topOc = "top";
    private String ocAttribute = "objectClass";
    private String ouAttribute = "ou";

    @Before
    public void doBefore() throws Exception {
        setupConnection();
        setupTests();
        dao = new LdapDao(connection);
    }

    @After
    public void tearDown() throws Exception {
        clearDIT();
        closeConnection();
    }

    private void setupConnection() throws Exception {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setCredentials("secret");
        bindRequest.setDn(new Dn("uid=admin,ou=system"));
        connection = new LdapNetworkConnection("localhost", 10389);
        connection.setTimeOut(0);
        connection.connect();
        connection.bind(bindRequest);
        ((LdapNetworkConnection) connection).loadSchema(new DefaultSchemaLoader(connection));

    }

    private void closeConnection() throws Exception {
        connection.unBind();
        connection.close();
    }

    private void setupTests() throws Exception {
        baseDn = new Dn("ou=test,dc=openengsb,dc=org");
        testEntryName = "testEntry";
    }

    private void clearDIT() throws Exception {
        dao.deleteSubtreeExcludingRoot(baseDn);
    }

    /**
     * IMPORTANT: Supports only Rdns with ou attribute.
     * */
    private Entry newTestEntry(String name, Dn parent) throws Exception {
        Entry e = new DefaultEntry(new Dn(new Rdn(ouAttribute, name), parent));
        e.add(ocAttribute, ouOc);
        e.add(ocAttribute, topOc);
        e.add(ouAttribute, name);
        return e;
    }

    /**
     * IMPORTANT: Supports only Rdns with ou attribute.
     * */
    private Entry newTestEntry(Dn dn) throws Exception {
        return newTestEntry(dn.getRdn().getValue().getString(), dn.getParent());
    }

    /**
     * baseDn(p1(c11,c12), p2(c21))
     * */
    private List<Entry> newTestHierarchy() throws Exception {
        String p1 = "p1";
        String p2 = "p2";
        String c21 = "c21";
        List<Entry> entries = new LinkedList<Entry>();
        entries.add(newTestEntry(p1, baseDn));
        entries.add(newTestEntry(p2, baseDn));
        entries.add(newTestEntry(c21, entries.get(1).getDn()));
        return entries;
    }

    private List<Value<?>> values(String attributeType, Entry entry) {
        Iterator<Value<?>> it = entry.get(attributeType).iterator();
        List<Value<?>> values = new LinkedList<Value<?>>();
        while (it.hasNext()) {
            values.add(it.next());
        }
        return values;
    }

    private boolean contains(List<Node> nodes, Entry entry) {
        for (Node n : nodes) {
            if (n.getEntry().equals(entry)) {
                return true;
            }
        }
        return false;
    }

    private Node which(List<Node> nodes, Entry entry) {
        for (Node n : nodes) {
            if (n.getEntry().equals(entry)) {
                return n;
            }
        }
        return null;
    }

    // TODO add test for missing parent exception. is the correct node returned?

    @Test
    public void testStore_shouldPersistEntry() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        dao.store(testEntry);
        assertThat(connection.exists(testEntry.getDn()), is(true));
        Entry e = connection.lookup(testEntry.getDn());
        LOGGER.debug(e.toString());
        assertThat(e.contains(ocAttribute, ouOc, topOc), is(true));
        assertThat(e.contains(ouAttribute, testEntryName), is(true));
        assertThat(e.getAttributes().size(), is(testEntry.getAttributes().size()));
        assertThat(values(ocAttribute, e).size(), is(values(ocAttribute, testEntry).size()));
        assertThat(values(ouAttribute, e).size(), is(values(ouAttribute, testEntry).size()));
    }

    @Test(expected = EntryAlreadyExistsException.class)
    public void testStore_expectEntryAlreadyExistsException() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        connection.add(testEntry);
        dao.store(testEntry);
    }

    @Test(expected = MissingParentException.class)
    public void testStore_expectMissingParentException() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        testEntry.setDn(testEntry.getDn().add("ou=child"));
        dao.store(testEntry);
    }

    @Test
    public void testStoreSkipExisting_shouldPersistEntry() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        dao.storeSkipExisting(testEntry);
        assertThat(connection.exists(testEntry.getDn()), is(true));
        Entry e = connection.lookup(testEntry.getDn());
        assertThat(e.contains(ocAttribute, ouOc, topOc), is(true));
        assertThat(e.contains(ouAttribute, testEntryName), is(true));
        assertThat(e.getAttributes().size(), is(testEntry.getAttributes().size()));
        assertThat(values(ocAttribute, e).size(), is(values(ocAttribute, testEntry).size()));
        assertThat(values(ouAttribute, e).size(), is(values(ouAttribute, testEntry).size()));
    }

    @Test
    public void testStoreSkipExisting_shouldSkip() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        Entry skippedEntry = newTestEntry(testEntry.getDn());
        skippedEntry.add(ouAttribute, "secondValue");
        connection.add(testEntry);
        Entry originalResult = connection.lookup(testEntry.getDn());
        dao.storeSkipExisting(skippedEntry);
        Entry newResult = connection.lookup(testEntry.getDn());
        assertThat(newResult, is(originalResult));
    }

    @Test(expected = MissingParentException.class)
    public void testStoreSkipExisting_expectMissingParentException() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        testEntry.setDn(testEntry.getDn().add("ou=child"));
        dao.storeSkipExisting(testEntry);
    }

    @Test
    public void testStoreOverwriteExisting_shouldPersistEntry() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        dao.storeOverwriteExisting(testEntry);
        assertThat(connection.exists(testEntry.getDn()), is(true));
        Entry e = connection.lookup(testEntry.getDn());
        assertThat(e.contains(ocAttribute, ouOc, topOc), is(true));
        assertThat(e.contains(ouAttribute, testEntryName), is(true));
        assertThat(e.getAttributes().size(), is(testEntry.getAttributes().size()));
        assertThat(values(ocAttribute, e).size(), is(values(ocAttribute, testEntry).size()));
        assertThat(values(ouAttribute, e).size(), is(values(ouAttribute, testEntry).size()));
    }

    @Test
    public void testStoreOverwriteExisting_shouldOverwrite() throws Exception {
        String differentValue = "secondValue";
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        Entry updatedEntry = newTestEntry(testEntry.getDn());
        updatedEntry.add(ouAttribute, differentValue);
        connection.add(testEntry);
        dao.storeOverwriteExisting(updatedEntry);
        Entry newResult = connection.lookup(testEntry.getDn());
        assertThat(newResult.contains(ouAttribute, differentValue), is(true));
    }

    @Test(expected = MissingParentException.class)
    public void testStoreOverwriteExisting_expectMissingParentException() throws Exception {
        Entry testEntry = newTestEntry(testEntryName, baseDn);
        testEntry.setDn(testEntry.getDn().add("ou=child"));
        dao.storeOverwriteExisting(testEntry);
    }

    @Test
    public void testStoreList_shouldPersistEntries() throws Exception {
        List<Entry> entries = newTestHierarchy();
        dao.store(entries);
        for (Entry testEntry : entries) {
            assertThat(connection.exists(testEntry.getDn()), is(true));
            Entry e = connection.lookup(testEntry.getDn());
            assertThat(e.contains(ocAttribute, ouOc, topOc), is(true));
            assertThat(e.contains(ouAttribute, testEntry.getDn().getRdn().getValue()), is(true));
            assertThat(e.getAttributes().size(), is(testEntry.getAttributes().size()));
            assertThat(values(ocAttribute, e).size(), is(values(ocAttribute, testEntry).size()));
            assertThat(values(ouAttribute, e).size(), is(values(ouAttribute, testEntry).size()));
        }
    }

    @Test(expected = EntryAlreadyExistsException.class)
    public void testStoreList_expectEntryAlreadyExistsException() throws Exception {
        List<Entry> entries = newTestHierarchy();
        connection.add(entries.get(0));
        dao.store(entries);
    }

    @Test(expected = MissingParentException.class)
    public void testStoreList_expectMissingParentException() throws Exception {
        List<Entry> entries = newTestHierarchy();
        entries.remove(1);// entries(1) is parent of entries(2)
        dao.store(entries);
    }

    @Test
    public void testStoreSkipExistingList_shouldPersistEntries() throws Exception {
        List<Entry> entries = newTestHierarchy();
        dao.storeSkipExisting(entries);
        for (Entry testEntry : entries) {
            assertThat(connection.exists(testEntry.getDn()), is(true));
            Entry e = connection.lookup(testEntry.getDn());
            assertThat(e.contains(ocAttribute, ouOc, topOc), is(true));
            assertThat(e.contains(ouAttribute, testEntry.getDn().getRdn().getValue()), is(true));
            assertThat(e.getAttributes().size(), is(testEntry.getAttributes().size()));
            assertThat(values(ocAttribute, e).size(), is(values(ocAttribute, testEntry).size()));
            assertThat(values(ouAttribute, e).size(), is(values(ouAttribute, testEntry).size()));
        }
    }

    @Test
    public void testStoreSkipExistingList_shouldSkip() throws Exception {
        List<Entry> entries = newTestHierarchy();
        List<Entry> originalResults = new LinkedList<Entry>();
        for (Entry e : entries) {
            connection.add(e);
            originalResults.add(connection.lookup(e.getDn()));
            e.add(ouAttribute, "secondValue");
        }
        dao.storeSkipExisting(entries);
        for (Entry testEntry : entries) {
            Entry e = connection.lookup(testEntry.getDn());
            assertThat(e, is(originalResults.get(entries.indexOf(testEntry))));
        }
    }

    @Test(expected = MissingParentException.class)
    public void testStoreSkipExistingList_expectMissingParentException() throws Exception {
        List<Entry> entries = newTestHierarchy();
        entries.remove(1);// entries(1) is parent of entries(2)
        dao.storeSkipExisting(entries);
    }

    @Test
    public void testStoreOverwriteExistingList_shouldPersistEntries() throws Exception {
        List<Entry> entries = newTestHierarchy();
        dao.storeOverwriteExisting(entries);
        for (Entry testEntry : entries) {
            assertThat(connection.exists(testEntry.getDn()), is(true));
            Entry e = connection.lookup(testEntry.getDn());
            assertThat(e.contains(ocAttribute, ouOc, topOc), is(true));
            assertThat(e.contains(ouAttribute, testEntry.getDn().getRdn().getValue()), is(true));
            assertThat(e.getAttributes().size(), is(testEntry.getAttributes().size()));
            assertThat(values(ocAttribute, e).size(), is(values(ocAttribute, testEntry).size()));
            assertThat(values(ouAttribute, e).size(), is(values(ouAttribute, testEntry).size()));
        }
    }

    @Test
    public void testStoreOverwriteExistingList_shouldOverwrite() throws Exception {
        String differentValue = "secondValue";
        List<Entry> entries = newTestHierarchy();
        List<Entry> originalResults = new LinkedList<Entry>();
        for (Entry e : entries) {
            connection.add(e);
            originalResults.add(connection.lookup(e.getDn()));
            e.add(ouAttribute, differentValue);
        }
        dao.storeOverwriteExisting(entries);
        for (Entry testEntry : entries) {
            Entry e = connection.lookup(testEntry.getDn());
            assertThat(e.contains(ouAttribute, differentValue), is(true));
        }
    }

    @Test(expected = MissingParentException.class)
    public void testStoreOverwriteExistingList_expectMissingParentException() throws Exception {
        List<Entry> entries = newTestHierarchy();
        entries.remove(1);// entries(1) is parent of entries(2)
        dao.storeOverwriteExisting(entries);
    }

    @Test
    public void testGetDirectChildren_shouldReturnDirectChildren() throws Exception {
        List<Entry> entries = newTestHierarchy();
        for (Entry e : entries) {
            connection.add(e);
        }
        Entry child0 = connection.lookup(entries.get(0).getDn());
        Entry child1 = connection.lookup(entries.get(1).getDn());
        List<Entry> children = dao.getDirectChildren(baseDn);
        assertThat(children, hasItem(child0));
        assertThat(children, hasItem(child1));
        assertThat(children.size(), is(2));
    }

    @Test
    public void testGetDirectChildrenFromLeaf_shouldReturnEmptyList() throws Exception {
        List<Entry> children = dao.getDirectChildren(baseDn);
        assertThat(children.isEmpty(), is(true));
    }

    @Test(expected = NoSuchNodeException.class)
    public void testGetDirectChildren_expectNoSuchNodeException() throws Exception {
        dao.getDirectChildren(new Dn("ou=hello"));
    }

    @Test(expected = MissingParentException.class)
    public void testGetDirectChildren_expectMissingParentException() throws Exception {
        dao.getDirectChildren(new Dn("ou=hello,ou=goodbye"));
    }

    @Test
    public void testSearchSubtree_shouldReturnSubtree() throws Exception {
        List<Entry> entries = newTestHierarchy();
        List<Entry> result = new LinkedList<Entry>();
        for (Entry e : entries) {
            connection.add(e);
            result.add(connection.lookup(e.getDn()));
        }
        List<Node> nodes = dao.searchSubtree(baseDn);
        assertThat(contains(nodes, result.get(0)), is(true));
        assertThat(contains(nodes, result.get(1)), is(true));
        Node parent = which(nodes, result.get(1));
        assertThat(contains(parent.getChildren(), result.get(2)), is(true));
    }

    @Test
    public void testSearchSubtreeFromLeaf_shouldReturnEmptyList() throws Exception {
        List<Node> subtree = dao.searchSubtree(baseDn);
        assertThat(subtree.isEmpty(), is(true));
    }

    @Test(expected = NoSuchNodeException.class)
    public void testSearchSubtreeFromLeaf_expectNoSuchNodeException() throws Exception {
        dao.searchSubtree(new Dn("ou=hello"));
    }

    @Test(expected = MissingParentException.class)
    public void testSearchSubtreeFromLeaf_expectMissingParentException() throws Exception {
        dao.searchSubtree(new Dn("ou=hello,ou=goodbye"));
    }

    @Test
    public void testDeleteSubtreeIncludingRoot_shouldDeleteSubtree() throws Exception {
        List<Entry> entries = newTestHierarchy();
        for (Entry e : entries) {
            connection.add(e);
        }
        assertThat(connection.exists(entries.get(1).getDn()), is(true));
        assertThat(connection.exists(entries.get(2).getDn()), is(true));
        dao.deleteSubtreeIncludingRoot(entries.get(1).getDn());
        assertThat(connection.exists(entries.get(0).getDn()), is(true));
        assertThat(connection.exists(entries.get(1).getDn()), is(false));
        assertThat(connection.exists(entries.get(2).getDn()), is(false));
    }

    @Test(expected = NoSuchNodeException.class)
    public void testDeleteSubtreeIncludingRoot_expectNoSuchNodeException() throws Exception {
        dao.deleteSubtreeIncludingRoot(new Dn("ou=hello"));
    }

    @Test(expected = MissingParentException.class)
    public void testDeleteSubtreeIncludingRoot_expectMissingParentException() throws Exception {
        dao.deleteSubtreeIncludingRoot(new Dn("ou=hello,ou=goodbye"));
    }

    @Test
    public void testDeleteSubtreeExcludingRoot_shouldDeleteSubtree() throws Exception {
        List<Entry> entries = newTestHierarchy();
        for (Entry e : entries) {
            connection.add(e);
        }
        assertThat(connection.exists(entries.get(2).getDn()), is(true));
        dao.deleteSubtreeExcludingRoot(entries.get(1).getDn());
        assertThat(connection.exists(entries.get(0).getDn()), is(true));
        assertThat(connection.exists(entries.get(1).getDn()), is(true));
        assertThat(connection.exists(entries.get(2).getDn()), is(false));
    }

    @Test(expected = NoSuchNodeException.class)
    public void testDeleteSubtreeExcludingRoot_expectNoSuchNodeException() throws Exception {
        dao.deleteSubtreeExcludingRoot(new Dn("ou=hello"));
    }

    @Test(expected = MissingParentException.class)
    public void testDeleteSubtreeExcludingRoot_expectMissingParentException() throws Exception {
        dao.deleteSubtreeExcludingRoot(new Dn("ou=hello,ou=goodbye"));
    }

    @Test
    public void testExists_shouldReturnTrue() {
        assertThat(dao.exists(baseDn), is(true));
    }

    @Test
    public void testExists_shouldReturnFalse() throws Exception {
        assertThat(dao.exists(new Dn("ou=hello")), is(false));
    }

    @Test
    public void testLookup_shouldReturnEntry() throws Exception {
        Entry e = connection.lookup(baseDn);
        Entry e1 = dao.lookup(baseDn);
        assertThat(e1, is(e));
    }

    @Test(expected = NoSuchNodeException.class)
    public void testLookup_expectNoSuchNodeException() throws Exception {
        dao.lookup(new Dn("ou=hello"));
    }

    @Test(expected = MissingParentException.class)
    public void testLookup_expectMissingParentException() throws Exception {
        dao.lookup(new Dn("ou=hello,ou=goodbye"));
    }
}
