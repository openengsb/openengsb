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

/**
 * Dao which provides CRUD methods for Ldap {@link Entry}s.
 * */
public class LdapDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDao.class);

    private LdapConnection connection;

    /**
     * Constructs a new LdapDao communicating with an Ldap server over given connection.
     * */
    public LdapDao(LdapConnection connection) {
        this.connection = connection;
    }

    /**
     * Sets the {@link LdapConnection} used by this dao to communicate with an Ldap server.
     * */
    public void setConnection(LdapConnection connection) {
        this.connection = connection;
    }

    /**
     * Returns the {@link LdapConnection} used by this dao to communicate with an Ldap server.
     * */
    public LdapConnection getConnection() {
        return connection;
    }

    /**
     * Inserts an entry into the DIT. Throws {@link EntryAlreadyExistsException} if an entry with given Dn already
     * exists. Throws {@link MissingParentException} if an ancestor of the entry is missing.
     */
    public void store(Entry entry) throws EntryAlreadyExistsException, MissingParentException {
        AddRequest addRequest = new AddRequestImpl().setEntry(entry);
        LdapResult result;
        try {
            result = connection.add(addRequest).getLdapResult();
        } catch (LdapException e) {
            throw new LdapDaoException(e);
        }
        if (result.getResultCode() == ResultCodeEnum.ENTRY_ALREADY_EXISTS) {
            throw new EntryAlreadyExistsException(entry);
        } else if (result.getResultCode() == ResultCodeEnum.NO_SUCH_OBJECT) {
            throw new MissingParentException(lastMatch(entry.getDn()));
        } else if (result.getResultCode() != ResultCodeEnum.SUCCESS) {
            throw new LdapDaoException(result.getDiagnosticMessage());
        }
    }

    /**
     * Inserts an entry into the DIT. If the entry exists, nothing is done. Throws {@link MissingParentException} if an
     * ancestor of the entry is missing.
     */
    public void storeSkipExisting(Entry entry) throws MissingParentException {
        try {
            store(entry);
        } catch (EntryAlreadyExistsException e) {
            LOGGER.debug("Entry already exists. Skipping.");
        }
    }

    /**
     * Inserts an entry into the DIT. If the entry exists, it is overwritten. Overwriting leaves is straight forward.
     * Overwriting inner nodes will first delete the node's entire subtree, then overwrite the node. Throws
     * {@link MissingParentException} if an ancestor of the entry is missing.
     */
    public void storeOverwriteExisting(Entry entry) throws MissingParentException {
        try {
            deleteSubtreeIncludingRoot(entry.getDn());
        } catch (NoSuchNodeException e) {
        }
        store(entry);
    }

    /**
     * Inserts a hierarchy (tree) of entries into the DIT. The hierarchy is passed as a list. The order of the entries
     * in the list is important. Throws {@link MissingParentException} if an ancestor of some entry is missing. In
     * particular this will happen if the list is not ordered properly. If one of the entries already exists, an
     * {@link EntryAlreadyExistsException} is thrown. Note that this action is not atomic.
     */
    public void store(List<Entry> entries) throws EntryAlreadyExistsException, MissingParentException {
        for (Entry e : entries) {
            store(e);
        }
    }

    /**
     * Behaves like {@link #store(List)} except that duplicates do not throw an exception but are collected in a list
     * which will be returned at success. Note that this action is not atomic.
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
     * Behaves like {@link #store(List)} except that duplicates do not throw an exception but are overwritten as
     * described in {@link #storeOverwriteExisting(Entry)}. Note that this action is not atomic.
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
     * Returns all entries found exactly one level below parent. Throws {@link NoSuchNodeException} if resolving the
     * argument Dn fails at its leaf. Throws {@link MissingParentException} if resolving fails earlier.
     * */
    public List<Entry> getDirectChildren(Dn parent) throws NoSuchNodeException, MissingParentException {
        return extractEntriesFromCursor(searchOneLevel(parent));
    }

    /**
     * Returns a list of {@link Node}s representing the subtrees of the argument Dn.
     * */
    public List<Node> searchSubtree(Dn parent) throws NoSuchNodeException, MissingParentException {
        SearchCursor cursor = searchOneLevel(parent);
        return extractNodesFromCursor(cursor);
    }

    /**
     * Deletes root and its entire subtree. Throws NoSuchNodeException if root does not exist. Throws
     * MissingParentException if some node above root does not exist.
     * */
    public void deleteSubtreeIncludingRoot(Dn root) throws MissingParentException, NoSuchNodeException {
        deleteSubtreeExcludingRoot(root);
        deleteLeaf(root);
    }

    /**
     * Deletes the entire subtree of root but not root itself. Throws NoSuchNodeException if root does not exist. Throws
     * MissingParentException if some node above root does not exist.
     * */
    public void deleteSubtreeExcludingRoot(Dn root) throws MissingParentException, NoSuchNodeException {
        existsCheck(root);
        try {
            EntryCursor entryCursor = connection.search(root, "(objectclass=*)", SearchScope.ONELEVEL);
            while (entryCursor.next()) {
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new LdapDaoException(e);
        }
    }

    /**
     * Returns true if dn exists, false otherwise.
     * */
    public boolean exists(Dn dn) {
        try {
            return connection.exists(dn);
        } catch (LdapException e) {
            throw new LdapDaoException(e);
        }
    }

    /**
     * Returns the {@link Entry} with given {@link Dn}. If dn does not exist but its parent does, a
     * {@link NoSuchNodeException} is thrown. If one of dn's ancestors does not exist, a {@link MissingParentException}
     * is thrown.
     */
    public Entry lookup(Dn dn) throws NoSuchNodeException, MissingParentException {
        existsCheck(dn);
        try {
            return connection.lookup(dn);
        } catch (LdapException e) {
            throw new LdapDaoException(e);
        }
    }

    /**
     * Throws appropriate exceptions for connection.exists(dn).
     * */
    private void existsCheck(Dn dn) {
        try {
            if (!connection.exists(dn.getParent())) {
                throw new MissingParentException(lastMatch(dn));
            } else if (!connection.exists(dn)) {
                throw new NoSuchNodeException(dn);
            }
        } catch (LdapException e) {
            throw new LdapDaoException(e);
        }
    }

    /**
     * Returns a SearchCursor over the direct children of Dn parent. Throws {@link NoSuchNodeException} if resolving the
     * argument Dn fails at its leaf. Throws {@link MissingParentException} if resolving fails earlier.
     * */
    private SearchCursor searchOneLevel(Dn parent) throws NoSuchNodeException, MissingParentException {
        try {
            if (!connection.exists(parent.getParent())) {
                throw new MissingParentException(lastMatch(parent));
            } else if (!connection.exists(parent)) {
                throw new NoSuchNodeException(parent);
            }
        } catch (LdapException e) {
            throw new LdapDaoException(e);
        }
        SearchRequest searchRequest = new SearchRequestImpl();
        searchRequest.setBase(parent);
        searchRequest.setScope(SearchScope.ONELEVEL);
        try {
            searchRequest.setFilter("(objectclass=*)");
            return connection.search(searchRequest);
        } catch (LdapException e) {
            throw new LdapDaoException(e);
        }
    }

    private List<Entry> extractEntriesFromCursor(SearchCursor cursor) {
        List<Entry> result = new LinkedList<Entry>();
        try {
            while (cursor.next()) {
                result.add(cursor.getEntry());
            }
        } catch (Exception e) {
            throw new LdapDaoException(e);
        }
        return result;
    }

    private List<Node> extractNodesFromCursor(SearchCursor cursor) {
        LinkedList<Node> result = new LinkedList<Node>();
        try {
            while (cursor.next()) {
                Node node = new Node(cursor.getEntry());
                result.addFirst(node);
                node.setChildren(searchSubtree(node.getEntry().getDn()));
                for (Node n : node.getChildren()) {
                    n.setParent(node);
                }
            }
        } catch (Exception e) {
            throw new LdapDaoException(e);
        }
        return result;
    }

    private void deleteLeaf(Dn dn) {
        DeleteRequest deleteRequest = new DeleteRequestImpl();
        deleteRequest.setName(dn);
        LdapResult result;
        try {
            result = connection.delete(deleteRequest).getLdapResult();
        } catch (LdapException e) {
            throw new LdapDaoException(e);
        }
        if (result.getResultCode() != ResultCodeEnum.SUCCESS) {
            throw new LdapDaoException(result.getDiagnosticMessage());
        }
    }

    /**
     * Iterates over the Dn from leaf to root and returns the first Dn that exists.
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
            throw new LdapDaoException(e);
        }
    }

}
