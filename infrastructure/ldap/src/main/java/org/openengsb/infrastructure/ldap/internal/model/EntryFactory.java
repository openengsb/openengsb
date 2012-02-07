package org.openengsb.infrastructure.ldap.internal.model;

import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;

/**
 * Builds entries for various nodes in the DIT. The returned entries have
 * a valid Dn and all provided attributes.
 * */
public class EntryFactory {

    //private static final Logger LOGGER = LoggerFactory.getLogger(EntryFactory.class);

    public static Entry organizationalUnit(String ou, Dn parent) {

        Dn dn = concatRdn(SchemaConstants.ouAttribute, ou, parent);
        Entry entry = new DefaultEntry(dn);

        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.organizationalUnitOc);
            entry.add(SchemaConstants.ouAttribute, ou);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        return entry;
    }

    public static Entry namedObject(String name, Dn parent) {

        Dn dn = concatRdn(SchemaConstants.cnAttribute, name, parent);
        Entry entry = new DefaultEntry(dn);
        
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.namedObjectOc);
            entry.add(SchemaConstants.cnAttribute, name);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        return entry;
    }

    public static Entry namedDescriptiveObject(String name, String description, Dn parent) {

        Entry entry = namedObject(name, parent);
        
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.descriptiveObjectOc);
            addDescription(entry, description);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        return entry;
    }

    public static Entry javaObject(String classname, String description, Dn parent) {

        Dn dn = concatRdn(SchemaConstants.javaClassNameAttribute, classname, parent);
        Entry entry = new DefaultEntry(dn);
        
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.javaClassInstanceOc);
            entry.add(SchemaConstants.javaClassNameAttribute, classname);
            addDescription(entry, description);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        return entry;
    }

    /**
     * 3 possibilities:<br>
     * 1) description is null >> emptyFlag == false, no description attribute <br>
     * 2) description is empty >> emptyFlag == true, no description attribute <br>
     * 3) description exists >> no emptyFlag, description attribute exists and has a value
     * */
    private static void addDescription(Entry entry, String description) throws LdapException {
        if(description != null ){
            if(description.isEmpty()){
                entry.add(SchemaConstants.emptyFlagAttribute, "TRUE"); //case 2
            } else {
                entry.add(SchemaConstants.descriptionAttribute, description); //case 3
            }
        } else {
            entry.add(SchemaConstants.emptyFlagAttribute, "FALSE"); //case 1
        }
    }

    //TODO move this to some class like ldapUtils because it is used in other classes too, eg OrderFilter, which should not depend on this class
    public static Dn concatRdn(String rdnAttribute, String rdnValue, Dn basedn){
        try {
            Rdn rdn = new Rdn(rdnAttribute, rdnValue);
            return basedn.add(rdn);
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
}
