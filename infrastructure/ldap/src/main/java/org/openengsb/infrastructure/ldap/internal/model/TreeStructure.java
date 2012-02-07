package org.openengsb.infrastructure.ldap.internal.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.core.security.internal.EntryUtils;
import org.openengsb.core.security.internal.model.EntryElement;

public class TreeStructure {

    public static List<Entry> userStructure(String username) {

        List<Entry> entries = new LinkedList<Entry>();

        Entry entry = EntryFactory.namedObject(username, SchemaConstants.ouUsers());
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

        return entries;
    }

    public static List<Entry> globalPermissionSetStructure(String permissionSet, List<EntryElement> permissions){

        List<Entry> entries = new LinkedList<Entry>();
        List<Entry> permissionEntries = new LinkedList<Entry>();
        Dn parent = SchemaConstants.ouGlobalPermissionSets();

        Entry permissionSetEntry = EntryFactory.namedObject(permissionSet, parent);
        Entry ouDirect = EntryFactory.organizationalUnit("direct", permissionSetEntry.getDn());
        Entry ouChildrenSets = EntryFactory.organizationalUnit("childrenSets", permissionSetEntry.getDn());
        Entry ouAttributes = EntryFactory.organizationalUnit("attributes", permissionSetEntry.getDn());

        for(EntryElement e : permissions){
            permissionEntries.add(EntryFactory.javaObject(e.getType(), e.getValue(), ouDirect.getDn()));
        }
        
        OrderFilter.addIds(permissionEntries, true);
        OrderFilter.makeContainerAware(ouDirect, String.valueOf(permissions.size()));
        OrderFilter.makeContainerAware(ouChildrenSets);

        entries.add(permissionSetEntry);
        entries.add(ouAttributes);
        entries.add(ouDirect);
        entries.add(ouChildrenSets);
        entries.addAll(permissionEntries);

        return entries;
    }

    public static List<Entry> permissions(List<EntryElement> permissions, Dn parent) {
        List<Entry> entries = new LinkedList<Entry>();
        for(EntryElement p : permissions){
            Entry permissionEntry = EntryFactory.javaObject(p.getType(), p.getValue(), parent);
            entries.add(permissionEntry);
        }
        return entries;
    }

    public static String extractAttribute(Entry entry, String attributeTye){
        
        Attribute attribute = entry.get(attributeTye);
        Attribute emptyFlagAttribute = entry.get(SchemaConstants.emptyFlagAttribute);
        
        boolean empty = false;
        try {
            if(attribute != null){
                return attribute.getString();
            } else if(emptyFlagAttribute != null){
                empty = Boolean.valueOf(emptyFlagAttribute.getString());
            }
        } catch (LdapInvalidAttributeValueException e) {
            throw new RuntimeException(e); //TODO replace runtime exception with more specific subtype
        }

        return empty ? new String() : null;
    }

    public static List<Object> extractUserAttributeValues(List<Entry> entries){ //TODO move this to UserDataManager. here should only be basic methods

        List<EntryElement> entryElements = new LinkedList<EntryElement>();

        for(Entry entry : entries){
            String value = extractAttribute(entry, SchemaConstants.descriptionAttribute);
            String type;
            try {
                type = entry.get(SchemaConstants.javaClassNameAttribute).getString();
            } catch (LdapInvalidAttributeValueException e) {
                throw new RuntimeException(e);
            }
            entryElements.add(new EntryElement(type, value));
        }

        return EntryUtils.convertAllEntryElementsToObject(entryElements);
    }

    public static List<String> extractAttribute(SearchCursor cursor, String attributeType){
        List<String> result = new LinkedList<String>();
        try {
            while(cursor.next()){
                Entry entry = cursor.getEntry();
                result.add(extractAttribute(entry, attributeType));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    //TODO ye.. how to do that..
//  public static Permission extractPermission(Entry entry){
//      String value = extractDescription(entry);
//      String type;
//      try {
//          type = entry.get(SchemaConstants.javaClassNameAttribute).getString();
//      } catch (LdapInvalidAttributeValueException e) {
//          throw new RuntimeException(e);
//      }
//      //Permission p = EntryUtils.
////      PermissionData pd = new PermissionData();
////      pd.setType(type);
////      pd.setAttributes("description");
//      
//  }

}
