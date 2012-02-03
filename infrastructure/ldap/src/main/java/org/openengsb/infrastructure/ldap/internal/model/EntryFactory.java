package org.openengsb.infrastructure.ldap.internal.model;

import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;

/**
 * Builds entries for various nodes in the DIT. The returned entries have
 * a valid Dn and all provided attributes.
 * */
public class EntryFactory {

    //private static final Logger LOGGER = LoggerFactory.getLogger(EntryFactory.class);
    
    public static Entry organizationalUnit(String ou, Dn parent) throws LdapException{

        Rdn rdn = new Rdn(SchemaConstants.ouAttribute, ou);
        Dn dn = new Dn(rdn, parent);
        
        Entry entry = new DefaultEntry(dn);
        entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.organizationalUnitOc);
        entry.add(SchemaConstants.ouAttribute, ou);

        return entry;
    }
    
    public static Entry namedObject(String name, Dn parent) throws LdapException{

        Rdn rdn = new Rdn(SchemaConstants.cnAttribute, name);
        Dn dn = new Dn(rdn, parent);
       
        Entry entry = new DefaultEntry(dn);
        entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.namedObjectOc);
        entry.add(SchemaConstants.cnAttribute, name);
        
        return entry;
    }

    public static Entry namedDescriptiveObject(String name, String description, Dn parent) throws LdapException{

        Entry entry = namedObject(name, parent);
        entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.descriptiveObjectOc);
        addDescription(entry, description);
        
        return entry;
    }
       
    public static Entry javaObject(String classname, String description, Dn parent) throws LdapException{

        Rdn rdn = new Rdn(SchemaConstants.javaClassNameAttribute, classname);
        Dn dn = new Dn(rdn, parent);
        
        Entry entry = new DefaultEntry(dn);
        entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.javaClassInstanceOc);
        entry.add(SchemaConstants.javaClassNameAttribute, classname);
        addDescription(entry, description);
        
        return entry;
    }
    
    private static void addDescription(Entry entry, String description) throws LdapException {
        if(description != null ){
            if(description.isEmpty()){
                entry.add(SchemaConstants.emptyFlagAttribute, "TRUE");
            } else {
                entry.add(SchemaConstants.descriptionAttribute, description);    
            }
        } else {
            entry.add(SchemaConstants.emptyFlagAttribute, "FALSE");
        }
    }
    
}
