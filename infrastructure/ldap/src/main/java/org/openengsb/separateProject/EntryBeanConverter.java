package org.openengsb.separateProject;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.core.security.internal.model.EntryElement;
import org.openengsb.core.security.internal.model.EntryValue;
import org.openengsb.core.security.internal.model.PermissionData;
import org.openengsb.core.security.internal.model.PermissionSetData;

public class EntryBeanConverter {
    
    /**
     * Returns a list of entries representing given {@link PermissionSetData}. The entries can be
     * inserted right away.
     * @param data
     * @return
     */
    public static List<Entry> globalPermissionSetStructure(String permissionSet){

        Entry permissionSetEntry = EntryFactory.namedObject(permissionSet, SchemaConstants.ouGlobalPermissionSets());
        Entry ouDirect = EntryFactory.organizationalUnit("direct", permissionSetEntry.getDn());
        Entry ouChildrenSets = EntryFactory.organizationalUnit("childrenSets", permissionSetEntry.getDn());
        Entry ouAttributes = EntryFactory.organizationalUnit("attributes", permissionSetEntry.getDn());

        return Arrays.asList(permissionSetEntry, ouAttributes, ouDirect, ouChildrenSets);
    }
    
    /**
     * Returns a list of entries representing given {@link PermissionData}. The list can
     * be inserted into the DIT as is.
     * @param permissionData
     * @param parent
     * @return
     */
    public static List<Entry> permissionStructureFromPermissionData(Collection<PermissionData> permissionData, Dn parent){
        
        List<Entry> permissions = new LinkedList<Entry>();
        List<Entry> properties = new LinkedList<Entry>();
        List<Entry> propertyValues = new LinkedList<Entry>();
        List<Entry> result = new LinkedList<Entry>();
        
        for(PermissionData p : permissionData){
            String permissionType = p.getType();
            Entry permissionEntry = EntryFactory.javaObject(permissionType, null, parent);
            OrderFilter.addId(permissionEntry, true);
            permissions.add(permissionEntry);
            
            for(EntryValue entryValue : p.getAttributes().values()){
                String propertyKey = entryValue.getKey();
                Entry propertyEntry = EntryFactory.namedObject(propertyKey, permissionEntry.getDn());
                properties.add(propertyEntry);
                
                for(EntryElement entryElement : entryValue.getValue()){
                    String type = entryElement.getType();
                    String value = entryElement.getValue();
                    Entry propertyValueEntry = EntryFactory.javaObject(type, value, propertyEntry.getDn());
                    OrderFilter.addId(propertyValueEntry, true);
                    propertyValues.add(propertyValueEntry);
                }
            }
        }
        
        result.addAll(permissions);
        result.addAll(properties);
        result.addAll(propertyValues);
        return result;
    }

}
