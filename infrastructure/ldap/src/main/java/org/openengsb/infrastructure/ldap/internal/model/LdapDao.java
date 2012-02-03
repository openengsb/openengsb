package org.openengsb.infrastructure.ldap.internal.model;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.shared.ldap.model.cursor.EntryCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.DefaultAttribute;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.entry.Value;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.DeleteRequest;
import org.apache.directory.shared.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.shared.ldap.model.message.LdapResult;
import org.apache.directory.shared.ldap.model.message.ModifyRequest;
import org.apache.directory.shared.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.message.SearchScope;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.schema.AttributeType;
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

    public void setConnection(LdapConnection connection){
        this.connection = connection;
    }

    public LdapConnection getConnection() {
        return connection;
    }

    /**
     * Inserts an entry into the DIT.
     * @param entry
     * @throws EntryAlreadyExistsException
     * @throws NoSuchObjectException if an ancestor of the entry is missing.
     */
    public void store(Entry entry) throws EntryAlreadyExistsException, NoSuchObjectException {
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
//            LOGGER.warn(result.getMatchedDn().getName());
//            LOGGER.warn(entry.getDn().getName());
            throw new NoSuchObjectException(result.getMatchedDn());    
        } else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());    
        }
    } 

    public void modify(Dn dn, Attribute... attributes) throws NoSuchObjectException, ObjectClassViolationException {
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
            throw new NoSuchObjectException(result.getMatchedDn());    
        } else if(result.getResultCode() == ResultCodeEnum.OBJECT_CLASS_VIOLATION){
            throw new ObjectClassViolationException();
        } else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            LOGGER.debug(result.getDiagnosticMessage());
            throw new RuntimeException(result.getDiagnosticMessage());    
        }
    }



    /**
     * Inserts an entry into the DIT. If an entry exists, it is modified.
     * @param entry
     * @throws EntryAlreadyExistsException
     * @throws NoSuchObjectException if an ancestor of the entry is missing.
     */
    public void storeOverwriteExisting(Entry entry) throws NoSuchObjectException {
        try {
            store(entry);
        } catch (EntryAlreadyExistsException e) {
            Dn dn = entry.getDn();
            List<Attribute> attributes = extractAttributes(entry);
            try {
                modify(dn, attributes.toArray(new Attribute[0]));
            } catch (ObjectClassViolationException e1) {
                throw new RuntimeException(e1);
            }
        }
    } 

    /**
     * Inserts a hierarchy of entries into the DIT. The order of the provided List is important.
     * If it does not follow the hierarchy in the DIT, NoSuchObjectException will be thrown.
     * @param entries
     * @throws NoSuchObjectException 
     * @throws EntryAlreadyExistsException
     */
    public void store(List<Entry> entries) throws EntryAlreadyExistsException, NoSuchObjectException{
        for(Entry e : entries){
            store(e);
        }
    }

    /**
     * Inserts a hierarchy of entries. If an entry already exists, nothing is done and the method
     * proceeds with the next entry.
     * @param entries
     * @return A list of the skipped entries or an empty list if none were skipped.
     * @throws NoSuchObjectException
     */
    public List<Entry> storeSkipExisting(List<Entry> entries) throws NoSuchObjectException{
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
     * @throws NoSuchObjectException 
     * */
    public void storeOverwriteExisting(List<Entry> entries) throws NoSuchObjectException {
        for(Entry entry : entries){
            try {
                store(entry);
            } catch (EntryAlreadyExistsException e) {
                Dn existing = e.getEntry().getDn();
                deleteSubtreeIncludingRoot(existing);
                try {
                    store(entry);
                } catch (EntryAlreadyExistsException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    /**
     * Deletes the baseDn and its entire subtree.
     * @throws NoSuchObjectException 
     * */
    public void deleteSubtreeIncludingRoot(Dn baseDn) throws NoSuchObjectException{

        DeleteRequest deleteRequest = new DeleteRequestImpl();
        deleteRequest.setName(baseDn);
        
        try {
            EntryCursor entryCursor = connection.search(baseDn, "(objectclass=*)", SearchScope.ONELEVEL);
            
            while(entryCursor.next()){
                deleteSubtreeIncludingRoot(entryCursor.get().getDn());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LdapResult result;
        try {
            //LOGGER.warn("deleting: " + deleteRequest.getName());
            result = connection.delete(deleteRequest).getLdapResult();
//            LOGGER.warn("resultcode: " + result.getResultCode().toString());
//            LOGGER.warn("is null? " + (result.getMatchedDn()==null)+" is empty? " +result.getMatchedDn().isEmpty()+" value: " + result.getMatchedDn().getName());

            if(result.getResultCode() != ResultCodeEnum.SUCCESS){
                //LOGGER.warn("diagnostic massage: " + result.getDiagnosticMessage());
                //LOGGER.warn("parent: " + baseDn.getParent());
                throw new NoSuchObjectException(baseDn.getParent());
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    private void setMaxId(Dn containerDn, String maxId) throws NoSuchObjectException, MissingOrderException{
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
    
    public String updateMaxId(Dn containerDn, int additionalItems) throws NoSuchObjectException, MissingOrderException {
        String oldMaxId = queryMaxId(containerDn);
        String newMaxId = calculateNewMaxId(oldMaxId, additionalItems);
        setMaxId(containerDn, newMaxId);
        return oldMaxId;
    }
    
    private String queryMaxId(Dn containerDn) throws NoSuchObjectException, MissingOrderException {
        List<String> values = getAttributeValues(containerDn, SchemaConstants.maxIdAttribute);
        if(values.isEmpty()){
            throw new MissingOrderException();
        }
        return values.get(0);
    }
    
    private List<Attribute> extractAttributes(Entry entry){
        List<Attribute> attributes = new LinkedList<Attribute>();
        for(AttributeType attributeType : entry.getAttributeTypes()){
            attributes.add(entry.get(attributeType));
        }
        return attributes;
    }
    
    //TODO handle description attribute which can be empty. check the boolean flag etc..
    /**
     * performs connection.lookup().
     * returns empty list if attribute is not present
     * */
    public List<String> getAttributeValues(Dn dn, String attributeType) throws NoSuchObjectException{
        
        Entry entry;
        try {
            entry = connection.lookup(dn, attributeType);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        
        if(entry == null){
            throw new NoSuchObjectException(dn);
        }
        
        Attribute attribute = entry.get(attributeType);
        
        if(attribute == null){
            return Collections.emptyList();
        }

        List<String> values = new LinkedList<String>();
        for(Value<?> value : attribute){
            values.add(value.getString());
        }
        return values;
    }
    
}
