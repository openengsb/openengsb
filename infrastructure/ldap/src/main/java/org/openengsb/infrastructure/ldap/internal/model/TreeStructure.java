package org.openengsb.infrastructure.ldap.internal.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.security.internal.model.EntryElement;

public class TreeStructure {

    public static Dn openengsbBaseDn() {
        try {
            return new Dn("dc=openengsb,dc=org");
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn userDataBaseDn() {
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "userData");
            return new Dn(rdn, openengsbBaseDn());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUsers() {
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "users");
            return new Dn(rdn, userDataBaseDn());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouGlobalPermissionSets() {
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "permissionSets");
            return new Dn(rdn, userDataBaseDn());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn user(String username) {
        try {
            Rdn rdn = new Rdn(SchemaConstants.cnAttribute, username);
            return new Dn(rdn, ouUsers());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUserAttributes(String username) {
        try {
            Rdn userRdn = new Rdn(SchemaConstants.cnAttribute, username);
            Dn dn = new Dn(userRdn, ouUsers());
            return dn.add(new Rdn(SchemaConstants.ouAttribute, "attributes"));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn userAttribute(String username, String attributename) {
        try {
            Rdn userRdn = new Rdn(SchemaConstants.cnAttribute, attributename);
            return new Dn(userRdn, ouUserAttributes(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouCredentialsDn(String username){
        try {
            Rdn userRdn = new Rdn(SchemaConstants.ouAttribute, "credentials");
            return new Dn(userRdn, user(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn userCredentialsDn(String username, String credentials){
        try {
            Rdn userRdn = new Rdn(SchemaConstants.cnAttribute, credentials);
            return new Dn(userRdn, ouCredentialsDn(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Entry> userStructure(String username) {

        List<Entry> entries = new LinkedList<Entry>();

        try {
            Entry entry = EntryFactory.namedObject(username, ouUsers());
            Entry ouPermissions = EntryFactory.organizationalUnit("permissions", entry.getDn());
            Entry ouDirectPermissions = EntryFactory.organizationalUnit("direct", ouPermissions.getDn());
            Entry ouPermissionSets = EntryFactory.organizationalUnit("permissionSets", ouPermissions.getDn());

            OrderFilter.makeContainerAware(ouDirectPermissions);
            OrderFilter.makeContainerAware(ouPermissionSets);

            entries.add(entry);
            entries.add(ouPermissions);
            entries.add(ouDirectPermissions);
            entries.add(ouPermissionSets);
            entries.add(EntryFactory.organizationalUnit("credentials", entry.getDn()));
            entries.add(EntryFactory.organizationalUnit("attributes", entry.getDn()));
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        return entries;
    }

    public static List<Entry> userAttributeStructure(String username, String attributename, List<EntryElement> entryElements) {

        LinkedList<Entry> entries = new LinkedList<Entry>();

        try {
            //create the container for the attribute's values. Don't add it to the list yet!
            Entry attributeEntry = EntryFactory.namedObject(attributename, ouUserAttributes(username));           
            Dn baseDn = attributeEntry.getDn();

            //Add entries to the list.
            for(EntryElement entryElement : entryElements){
                Entry attributeValue = EntryFactory.javaObject(entryElement.getType(), entryElement.getValue(), baseDn);
                entries.add(attributeValue);
            }

            //apply ordering information on each element. That's why the container entry must not be present yet.
            OrderFilter.addIds(entries, true);
            //now we can also add the container. Important: it must be the first element because of DIT hierarchy.
            entries.addFirst(attributeEntry);

        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        return entries;
    }

    public static Entry credentialsEntry(String username, String credentialsType, String credentialsValue){
        Dn parent = ouCredentialsDn(username);
        Entry entry;
        try {
            entry = EntryFactory.namedDescriptiveObject(credentialsType, credentialsValue, parent);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        return entry;
    }

    public static List<Entry> globalPermissionSetStructure(String permissionSet, Permission[] permissions){

        List<Entry> entries = new LinkedList<Entry>();
        Dn parent = ouGlobalPermissionSets();
        
        Entry permissionSetEntry;
        Entry ouDirect;
        Entry ouChildrenSets;
        Entry ouAttributes;
        List<Entry> permissionEntries;
        
        try {
            permissionSetEntry = EntryFactory.namedObject(permissionSet, parent);
            ouDirect = EntryFactory.organizationalUnit("direct", permissionSetEntry.getDn());
            ouChildrenSets = EntryFactory.organizationalUnit("childrenSets", permissionSetEntry.getDn());
            ouAttributes = EntryFactory.organizationalUnit("attributes", permissionSetEntry.getDn());
            permissionEntries = permissions(permissions, ouDirect.getDn());

            OrderFilter.makeContainerAware(ouDirect, String.valueOf(entries.size()));
            OrderFilter.makeContainerAware(ouChildrenSets);
            
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        
        entries.add(permissionSetEntry);
        entries.add(ouAttributes);
        entries.add(ouDirect);
        entries.add(ouChildrenSets);
        entries.addAll(permissionEntries);

        return entries;
    }

    public static List<Entry> permissions(Permission[] permissions, Dn parent) throws LdapException {
        List<Entry> entries = new LinkedList<Entry>();
        for(Permission permission : permissions){
            String type = permission.getClass().getName();
            String description = permission.describe();
            Entry permissionEntry = EntryFactory.javaObject(type, description, parent);
            entries.add(permissionEntry);
        }
        OrderFilter.addIds(entries, true);
        return entries;
    }

    public static List<Entry> permissions(Permission[] permissions, String maxId, Dn parent) throws LdapException {
        List<Entry> entries = new LinkedList<Entry>();
        for(Permission permission : permissions){
            String type = permission.getClass().getName();
            String description = permission.describe();
            Entry entry = EntryFactory.javaObject(type, description, parent);
            entries.add(entry);
        }
        OrderFilter.addIds(entries, maxId, true);
        return entries;
    }

}
