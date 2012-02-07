package org.openengsb.infrastructure.ldap.internal.model;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.name.Dn;

public class OrderFilter {

    public static void makeContainerAware(Entry entry) {
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.awareContainerOc);
            entry.add(SchemaConstants.maxIdAttribute, "0");
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void makeContainerAware(Entry entry, String initialMaxId) {
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.awareContainerOc);
            entry.add(SchemaConstants.maxIdAttribute, initialMaxId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds an id to each entry, starting with 1.
     * @param entries
     * @param updateRdn If true, the id becomes the Rdn. Use this to handle duplicates.
     * @throws LdapException 
     */
    public static void addIds(List<Entry> entries, boolean updateRdn) {

        int i = 1;
        
        for(Entry entry : entries){

            String id = String.valueOf(i);

            try {
                entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.uniqueObjectOc);
                entry.add(SchemaConstants.idAttribute, id);
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }

            if(updateRdn){
                Dn newDn = EntryFactory.concatRdn(SchemaConstants.idAttribute, id, entry.getDn().getParent());
                entry.setDn(newDn);
            }
            i++;
        }
    }

    /**
     * Adds ids to entries, incrementing maxId by one for each entry. First id = maxId+1. 
     * @param entries
     * @param idSequence
     * @param updateRdn If true, the id becomes the Rdn. Use this to handle duplicates.
     * @throws LdapException 
     */
    public static void addIds(List<Entry> entries, String maxId, boolean updateRdn) {

        int i = 1;
        BigInteger a = new BigInteger(maxId);
        
        for(Entry entry : entries){
            
            BigInteger b = new BigInteger(String.valueOf(i));
            String id = a.add(b).toString();

            try {
                entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.uniqueObjectOc);
                entry.add(SchemaConstants.idAttribute, id);
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }

            if(updateRdn){
                Dn newDn = EntryFactory.concatRdn(SchemaConstants.idAttribute, id, entry.getDn().getParent());
                entry.setDn(newDn);
            }
            i++;
        }
    }
    
    public static List<Entry> sortById(SearchCursor cursor){

        TreeMap<BigInteger, Entry> map = new TreeMap<BigInteger, Entry>();

        try {
            while(cursor.next()){
                Entry entry = cursor.getEntry();
                String key = entry.get(SchemaConstants.idAttribute).getString();
                map.put(new BigInteger(key), entry);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new LinkedList<Entry>(map.values());
    }

}
