package org.openengsb.infrastructure.ldap.internal.dao;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.shared.ldap.model.cursor.EntryCursor;
import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.DefaultAttribute;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.DeleteRequest;
import org.apache.directory.shared.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.shared.ldap.model.message.LdapResult;
import org.apache.directory.shared.ldap.model.message.ModifyRequest;
import org.apache.directory.shared.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.message.SearchRequest;
import org.apache.directory.shared.ldap.model.message.SearchRequestImpl;
import org.apache.directory.shared.ldap.model.message.SearchScope;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.infrastructure.ldap.internal.EntryAlreadyExistsException;
import org.openengsb.infrastructure.ldap.internal.MissingOrderException;
import org.openengsb.infrastructure.ldap.internal.MissingParentException;
import org.openengsb.infrastructure.ldap.internal.NoSuchNodeException;
import org.openengsb.infrastructure.ldap.internal.ObjectClassViolationException;
import org.openengsb.infrastructure.ldap.internal.model.SchemaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//TODO move this into another package outside of SchemaConstants, entryFactory and TreeStructure. If no imports from there are necessary it's good job.
public class LdapDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapDao.class);

    private LdapConnection connection;

    public LdapDao(LdapConnection connection) {
        this.connection = connection;
    }

    public LdapDao() {
    }

    public void setConnection(LdapConnection connection){
        this.connection = connection;
    }

    public LdapConnection getConnection() {
        return connection;
    }

    /**
     * @throws MissingParentException 
     * */
    public void modify(Dn dn, Attribute... attributes) throws NoSuchNodeException, ObjectClassViolationException, MissingParentException {
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setName(dn);
        LdapResult result;

        for(Attribute a : attributes){
            modifyRequest.replace(a);
        }

        try {
            result = connection.modify(modifyRequest).getLdapResult();
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        if(result.getResultCode() == ResultCodeEnum.NO_SUCH_OBJECT){
            try {
                if(connection.exists(dn.getParent())){
                    throw new NoSuchNodeException(dn);
                } else {
                    throw new MissingParentException(lastMatch(dn));
                }
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        } else if(result.getResultCode() == ResultCodeEnum.OBJECT_CLASS_VIOLATION){
            throw new ObjectClassViolationException();
        } else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            LOGGER.debug(result.getDiagnosticMessage());
            throw new RuntimeException(result.getDiagnosticMessage());    
        }
    }

    /**
     * Inserts an entry into the DIT.
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
        if(result.getResultCode() == ResultCodeEnum.ENTRY_ALREADY_EXISTS){
            throw new EntryAlreadyExistsException(entry);
        } else if(result.getResultCode() == ResultCodeEnum.NO_SUCH_OBJECT){
            throw new MissingParentException(lastMatch(entry.getDn()));
        } else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());    
        }
    } 

    /**
     * Inserts an entry into the DIT. If an entry exists, nothing is done.
     * @param entry
     * @throws MissingParentException if an ancestor of the entry is missing.
     */
    public void storeSkipExisting(Entry entry) throws MissingParentException {
        try {
            store(entry);
        } catch (EntryAlreadyExistsException e) {
            LOGGER.debug("entry already exists, doing nothing");
            return;
        }
    }

    /**
     * Overwrites an entry in the DIT, deleting its whole subtree. <br>
     * ATTENTION when overwriting inner nodes (non-leaves)! If its subtree should remain, use modify instead.
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
     * Inserts a list of entries into the DIT. The order of the entries is important.
     * If it does not follow the hierarchy in the DIT, NoSuchObjectException will be thrown.
     * @param entries
     * @throws MissingParentException 
     * @throws EntryAlreadyExistsException
     */
    public void store(List<Entry> entries) throws EntryAlreadyExistsException, MissingParentException{
        for(Entry e : entries){
            store(e);
        }
    }

    /**
     * Inserts a hierarchy of entries. If an entry already exists, nothing is done and the method
     * proceeds with the next entry.
     * @param entries
     * @return A list of the skipped entries or an empty list if none were skipped.
     * @throws MissingParentException
     */
    public List<Entry> storeSkipExisting(List<Entry> entries) throws MissingParentException{
        List<Entry> skippedEntries = new LinkedList<Entry>();
        for(Entry e : entries){
            try {
                store(e);
            } catch (EntryAlreadyExistsException ex) {
                skippedEntries.add(e);
            }
        }
        return skippedEntries;
    }

    /**
     * Inserts a hierarchy of entries. If an entry already exists, the existing entry and
     * its entire subtree is deleted and the new entry including possible subtree is inserted.
     * 
     * */
    public void storeOverwriteExisting(List<Entry> entries) throws MissingParentException {
        for(Entry entry : entries){
            try {
                store(entry);
            } catch (EntryAlreadyExistsException e) {
                Dn existing = e.getEntry().getDn();
                try {
                    deleteSubtreeIncludingRoot(existing);
                } catch (NoSuchNodeException e2) {
                    //TODO
                }
                try {
                    store(entry);
                } catch (EntryAlreadyExistsException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    /**
     * Retrieves a SearchCursor over the direct children of Dn parent.
     * @throws NoSuchNodeException 
     * @throws MissingParentException 
     * */
    public SearchCursor searchOneLevel(Dn parent) throws NoSuchNodeException, MissingParentException{

        try {
            if(!connection.exists(parent)){
                throw new NoSuchNodeException(parent);
            }else if(!connection.exists(parent.getParent())){
                throw new MissingParentException(lastMatch(parent));
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

    /**
     * Deletes all direct children of baseDn which match searchFilter. Their subtrees
     * are deleted as well, regardless of searchFilter.
     * Deletes the baseDn and its entire subtree.<br>
     * @throws NoSuchNodeException if baseDn does not exist
     * @throws MissingParentException if some node above baseDn does not exist
     * */
    public void deleteSubtreeExcludingRoot(Dn baseDn, String searchFilter) throws MissingParentException, NoSuchNodeException {

        try {
            if(!connection.exists(baseDn.getParent())){
                throw new MissingParentException(lastMatch(baseDn));
            } else if(!connection.exists(baseDn)){
                throw new NoSuchNodeException(baseDn);
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        try {
            //(&(exp1)(exp2)(exp3))
            EntryCursor entryCursor = connection.search(baseDn, String.format("(&(objectclass=*)%s)", searchFilter), SearchScope.ONELEVEL);
            while(entryCursor.next()){
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the baseDn and its entire subtree.<br>
     * @throws NoSuchNodeException if baseDn does not exist
     * @throws MissingParentException if some node above baseDn does not exist
     * */
    public void deleteSubtreeIncludingRoot(Dn baseDn) throws MissingParentException, NoSuchNodeException {

        try {
            if(!connection.exists(baseDn.getParent())){
                throw new MissingParentException(lastMatch(baseDn));
            } else if(!connection.exists(baseDn)){
                throw new NoSuchNodeException(baseDn);
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        try {
            EntryCursor entryCursor = connection.search(baseDn, "(objectclass=*)", SearchScope.ONELEVEL);
            while(entryCursor.next()){
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        DeleteRequest deleteRequest = new DeleteRequestImpl();
        deleteRequest.setName(baseDn);
        LdapResult result;

        try {
            result = connection.delete(deleteRequest).getLdapResult();
            if(result.getResultCode() != ResultCodeEnum.SUCCESS){
                throw new RuntimeException(result.getDiagnosticMessage());
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Deletes the entire subtree of baseDn but not baseDn itself.<br>
     * @throws NoSuchNodeException if baseDn does not exist
     * @throws MissingParentException if some node above baseDn does not exist
     * */
    public void deleteSubtreeExcludingRoot(Dn baseDn) throws MissingParentException, NoSuchNodeException {

        try {
            if(!connection.exists(baseDn.getParent())){
                throw new MissingParentException(lastMatch(baseDn));
            } else if(!connection.exists(baseDn)){
                throw new NoSuchNodeException(baseDn);
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        try {
            EntryCursor entryCursor = connection.search(baseDn, "(objectclass=*)", SearchScope.ONELEVEL);
            while(entryCursor.next()){
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMaxId(Dn containerDn, String maxId) throws NoSuchNodeException, MissingOrderException, MissingParentException{
        Attribute attribute = new DefaultAttribute(SchemaConstants.maxIdAttribute, maxId);
        try {
            modify(containerDn, attribute);
        } catch (ObjectClassViolationException e) {
            throw new MissingOrderException();
        }
    }

    private String calculateNewMaxId(String oldMaxId, int additionalItems){
        BigInteger a = new BigInteger(oldMaxId);
        BigInteger b = new BigInteger(String.valueOf(additionalItems));
        BigInteger c = a.add(b);
        return c.toString();
    }

    /**
     * @param containerDn the parent Dn of the entries to be added
     * @param additionalItems
     * @return the maxId before the update
     * @throws NoSuchNodeException
     * @throws MissingOrderException
     * @throws MissingParentException
     */
    public String updateMaxId(Dn containerDn, int additionalItems) throws NoSuchNodeException, MissingOrderException, MissingParentException {
        String oldMaxId = lookupMaxId(containerDn);
        String newMaxId = calculateNewMaxId(oldMaxId, additionalItems);
        setMaxId(containerDn, newMaxId);
        return oldMaxId;
    }

    private String lookupMaxId(Dn containerDn) throws NoSuchNodeException, MissingOrderException, MissingParentException {

        Entry entry = lookup(containerDn);
        Attribute maxId = entry.get(SchemaConstants.maxIdAttribute);

        if(maxId != null){
            try {
                return maxId.getString();
            } catch (LdapInvalidAttributeValueException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new MissingOrderException();
        }
    }

    public boolean exists(Dn dn){
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
    public Entry lookup(Dn dn) throws NoSuchNodeException, MissingParentException{
        Entry entry;
        try {
            entry = connection.lookup(dn);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        if(entry != null){
            return entry;
        }

        try {
            if(connection.exists(dn.getParent())){
                throw new NoSuchNodeException(dn);
            } else {
                throw new MissingParentException(lastMatch(dn));
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Iterates over the Dn from leaf to root and returns the first Dn that exists.
     * */
    private Dn lastMatch(final Dn dn){
        try {
            if(connection.exists(dn)){
                return dn;
            } else {
                return lastMatch(dn.getParent());
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

}
