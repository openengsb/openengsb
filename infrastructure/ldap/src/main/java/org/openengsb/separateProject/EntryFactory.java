package org.openengsb.separateProject;

import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.infrastructure.ldap.util.LdapUtils;

/**
 * Builds entries for various nodes in the DIT. The returned entries have
 * a valid Dn and all provided attributes.
 * */
public class EntryFactory {

    public static Entry organizationalUnit(String ou, Dn parent) {
        Dn dn = LdapUtils.concatDn(SchemaConstants.ouAttribute, ou, parent);
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
        Dn dn = LdapUtils.concatDn(SchemaConstants.cnAttribute, name, parent);
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
            addDescription(entry, description);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        return entry;
    }

    public static Entry javaObject(String classname, String constructorArgument, Dn parent) {
        Dn dn = LdapUtils.concatDn(SchemaConstants.javaClassNameAttribute, classname, parent);
        Entry entry = new DefaultEntry(dn);
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.javaClassInstanceOc);
            entry.add(SchemaConstants.javaClassNameAttribute, classname);
            addDescription(entry, constructorArgument);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        return entry;
    }
    
    /**
     * 3 possibilities:<br>
     * 1) description is null -> emptyFlag = false, no description attribute <br>
     * 2) description is empty -> emptyFlag = true, no description attribute <br>
     * 3) description exists -> no emptyFlag, description attribute exists and has a value
     * */
    private static void addDescription(Entry entry, String description) throws LdapException {
        entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.descriptiveObjectOc);
        if(description == null){
            entry.add(SchemaConstants.emptyFlagAttribute, String.valueOf(false)); //case 1
        }else if(description.isEmpty()){
            entry.add(SchemaConstants.emptyFlagAttribute, String.valueOf(true)); //case 2
        }else{
            entry.add(SchemaConstants.stringAttribute, description); //case 3
        }
    }

}
