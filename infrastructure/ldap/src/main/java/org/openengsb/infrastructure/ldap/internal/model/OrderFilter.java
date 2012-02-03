package org.openengsb.infrastructure.ldap.internal.model;

import java.math.BigInteger;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.name.Rdn;

public class OrderFilter {

    public static void makeContainerAware(Entry entry) throws LdapException{
        entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.awareContainerOc);
        entry.add(SchemaConstants.maxIdAttribute, "0");
    }
    
    public static void makeContainerAware(Entry entry, String initialMaxId) throws LdapException{
        entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.awareContainerOc);
        entry.add(SchemaConstants.maxIdAttribute, initialMaxId);
    }
    
    /**
     * Adds an id to each entry, starting with 1.
     * @param entries
     * @param updateRdn If true, the id becomes the Rdn. Use this to handle duplicates.
     * @throws LdapException 
     */
    public static void addIds(List<Entry> entries, boolean updateRdn) throws LdapException{

        int i = 1;
        
        for(Entry entry : entries){

            String id = String.valueOf(i);

            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.uniqueObjectOc);
            entry.add(SchemaConstants.idAttribute, id);

            if(updateRdn){
                Rdn rdn = new Rdn(SchemaConstants.idAttribute, id);
                entry.setDn(entry.getDn().getParent().add(rdn));
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
    public static void addIds(List<Entry> entries, String maxId, boolean updateRdn) throws LdapException{

        int i = 1;
        BigInteger a = new BigInteger(maxId);
        
        for(Entry entry : entries){
            
            BigInteger b = new BigInteger(String.valueOf(i));
            String id = a.add(b).toString();

            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.uniqueObjectOc);
            entry.add(SchemaConstants.idAttribute, id);

            if(updateRdn){
                Rdn rdn = new Rdn(SchemaConstants.idAttribute, id);
                entry.setDn(entry.getDn().getParent().add(rdn));
            }
            i++;
        }
    }

}
