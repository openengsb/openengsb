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

package org.openengsb.infrastructure.ldap.internal;

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.shared.ldap.model.cursor.EntryCursor;
import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.DeleteRequest;
import org.apache.directory.shared.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.shared.ldap.model.message.LdapResult;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.message.SearchRequest;
import org.apache.directory.shared.ldap.model.message.SearchRequestImpl;
import org.apache.directory.shared.ldap.model.message.SearchScope;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.infrastructure.ldap.internal.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDao.class);

    private LdapConnection connection;

    public LdapDao(LdapConnection connection) {
        this.connection = connection;
    }

    public LdapDao() {
    }

    public void setConnection(LdapConnection connection) {
        this.connection = connection;
    }

    public LdapConnection getConnection() {
        return connection;
    }

    /**
     * Inserts an entry into the DIT.
     * 
     * @param entry
     * @throws EntryAlreadyExistsException
     * @throws MissingParentException if an ancestor of the entry is missing.
     */
    public void store(Entry entry) throws EntryAlreadyExistsException, MissingParentException {
        AddRequest addRequest = new AddRequestImpl().setEntry(entry);
        LdapResult result;

        try {
            result = connection.add(addRequest).getLdapResult();
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        if (result.getResultCode() == ResultCodeEnum.ENTRY_ALREADY_EXISTS) {
            throw new EntryAlreadyExistsException(entry);
        } else if (result.getResultCode() == ResultCodeEnum.NO_SUCH_OBJECT) {
            throw new MissingParentException(lastMatch(entry.getDn()));
        } else if (result.getResultCode() != ResultCodeEnum.SUCCESS) {
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }

    /**
     * Inserts an entry into the DIT. If an entry exists, nothing is done.
     * 
     * @param entry
     * @throws MissingParentException if an ancestor of the entry is missing.
     */
    public void storeSkipExisting(Entry entry) throws MissingParentException {
        try {
            store(entry);
        } catch (EntryAlreadyExistsException e) {
            LOGGER.debug("Entry already exists. Skipping.");
        }
    }

    /**
     * Overwrites an entry in the DIT, deleting its whole subtree. <br>
     * ATTENTION when overwriting inner nodes (non-leaves)! If its subtree
     * should remain, use modify instead.
     * 
     * @param entry
     * @throws MissingParentException if an ancestor of the entry is missing.
     */
    public void storeOverwriteExisting(Entry entry) throws MissingParentException {
        try {
            deleteSubtreeIncludingRoot(entry.getDn());
        } catch (NoSuchNodeException e) {
        }
        storeSkipExisting(entry);
    }

    /**
     * Inserts a list of entries into the DIT. The order of the entries is
     * important. If it does not follow the hierarchy in the DIT,
     * NoSuchObjectException will be thrown.
     * 
     * @param entries
     * @throws MissingParentException
     * @throws EntryAlreadyExistsException
     */
    public void store(List<Entry> entries) throws EntryAlreadyExistsException, MissingParentException {
        for (Entry e : entries) {
            store(e);
        }
    }

    /**
     * Inserts a hierarchy of entries. If an entry already exists, nothing is
     * done and the method proceeds with the next entry.
     * 
     * @param entries
     * @return A list of the skipped entries or an empty list if none were
     *         skipped.
     * @throws MissingParentException
     */
    public List<Entry> storeSkipExisting(List<Entry> entries) throws MissingParentException {
        List<Entry> skippedEntries = new LinkedList<Entry>();
        for (Entry e : entries) {
            try {
                store(e);
            } catch (EntryAlreadyExistsException ex) {
                skippedEntries.add(e);
            }
        }
        return skippedEntries;
    }

    /**
     * Inserts a hierarchy of entries. If an entry already exists, the existing
     * entry and its entire subtree is deleted and the new entry including
     * possible subtree is inserted.
     * */
    public void storeOverwriteExisting(List<Entry> entries) throws MissingParentException {
        for (Entry entry : entries) {
            try {
                store(entry);
            } catch (EntryAlreadyExistsException e) {
                deleteSubtreeIncludingRoot(e.getEntry().getDn());
                store(entry);
            }
        }
    }

    /**
     * Returns a SearchCursor over the direct children of Dn parent.
     * 
     * @throws NoSuchNodeException
     * @throws MissingParentException
     * */
    private SearchCursor searchOneLevel(Dn parent) throws NoSuchNodeException, MissingParentException {

        try {
            if (!connection.exists(parent.getParent())) {
                throw new MissingParentException(lastMatch(parent));
            } else if (!connection.exists(parent)) {
                throw new NoSuchNodeException(parent);
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        SearchRequest searchRequest = new SearchRequestImpl();
        searchRequest.setBase(parent);
        searchRequest.setScope(SearchScope.ONELEVEL);

        try {
            searchRequest.setFilter("(objectclass=*)");
            return connection.search(searchRequest);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Entry> getDirectChildren(Dn parent) {
        return extractEntriesFromCursor(searchOneLevel(parent));
    }

    private List<Entry> extractEntriesFromCursor(SearchCursor cursor) {
        List<Entry> result = new LinkedList<Entry>();
        try {
            while (cursor.next()) {
                result.add(cursor.getEntry());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Node> searchSubtreeNode(Dn parent) throws NoSuchNodeException, MissingParentException {

        LinkedList<Node> result = new LinkedList<Node>();
        SearchCursor cursor = searchOneLevel(parent);

        try {
            while (cursor.next()) {
                Node node = new Node(cursor.getEntry());
                result.addFirst(node);
                node.setChildren(searchSubtreeNode(node.getEntry().getDn()));
                for (Node n : node.getChildren()) {
                    n.setParent(node);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Deletes all direct children which match the searchFilter, including their
     * subtrees. Does not delete parent.
     * 
     * @throws NoSuchNodeException if parent does not exist
     * @throws MissingParentException if some node above parent does not exist
     * */
    public void deleteMatchingChildren(Dn parent, String searchFilter) throws MissingParentException,
        NoSuchNodeException {

        try {
            if (!connection.exists(parent.getParent())) {
                throw new MissingParentException(lastMatch(parent));
            } else if (!connection.exists(parent)) {
                throw new NoSuchNodeException(parent);
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        try {
            // ldap search syntax: (&(exp1)(exp2)(exp3))
            EntryCursor entryCursor = connection.search(parent, String.format("(&(objectclass=*)%s)", searchFilter),
                SearchScope.ONELEVEL);
            while (entryCursor.next()) {
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the parent and its entire subtree.<br>
     * 
     * @throws NoSuchNodeException if parent does not exist
     * @throws MissingParentException if some node above parent does not exist
     * */
    public void deleteSubtreeIncludingRoot(Dn parent) throws MissingParentException, NoSuchNodeException {

        try {
            if (!connection.exists(parent.getParent())) {
                throw new MissingParentException(lastMatch(parent));
            } else if (!connection.exists(parent)) {
                throw new NoSuchNodeException(parent);
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        try {
            EntryCursor entryCursor = connection.search(parent, "(objectclass=*)", SearchScope.ONELEVEL);
            while (entryCursor.next()) {
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        DeleteRequest deleteRequest = new DeleteRequestImpl();
        deleteRequest.setName(parent);
        LdapResult result;

        try {
            result = connection.delete(deleteRequest).getLdapResult();
            if (result.getResultCode() != ResultCodeEnum.SUCCESS) {
                throw new RuntimeException(result.getDiagnosticMessage());
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the entire subtree of parent but not parent itself.<br>
     * 
     * @throws NoSuchNodeException if parent does not exist
     * @throws MissingParentException if some node above parent does not exist
     * */
    public void deleteSubtreeExcludingRoot(Dn parent) throws MissingParentException, NoSuchNodeException {

        try {
            if (!connection.exists(parent.getParent())) {
                throw new MissingParentException(lastMatch(parent));
            } else if (!connection.exists(parent)) {
                throw new NoSuchNodeException(parent);
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        try {
            EntryCursor entryCursor = connection.search(parent, "(objectclass=*)", SearchScope.ONELEVEL);
            while (entryCursor.next()) {
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(Dn dn) {
        try {
            return connection.exists(dn);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param dn
     * @return entry
     * @throws NoSuchNodeException if dn does not exist but its parent does
     * @throws MissingParentException if the dn's parent does not exist
     */
    public Entry lookup(Dn dn) throws NoSuchNodeException, MissingParentException {
        Entry entry;
        try {
            entry = connection.lookup(dn);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        if (entry != null) {
            return entry;
        }

        try {
            if (connection.exists(dn.getParent())) {
                throw new NoSuchNodeException(dn);
            } else {
                throw new MissingParentException(lastMatch(dn));
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Iterates over the Dn from leaf to root and returns the first Dn that
     * exists.
     * */
    private Dn lastMatch(final Dn dn) {
        if (dn == null) {
            throw new MissingParentException((Dn) null);
        }
        try {
            if (connection.exists(dn)) {
                return dn;
            } else {
                return lastMatch(dn.getParent());
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

}
