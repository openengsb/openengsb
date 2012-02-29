package org.openengsb.separateProject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.core.security.internal.model.EntryElement;
import org.openengsb.core.security.internal.model.EntryValue;
import org.openengsb.core.security.internal.model.PermissionData;
import org.openengsb.core.security.internal.model.PermissionSetData;

public class TreeStructure {
    
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

//        List<Entry> permissionEntries = permissionStructure(data.getPermissions(), null, ouDirect.getDn());
//        OrderFilter.addIds(permissionEntries, true);
        OrderFilter.makeContainerAware(ouDirect);
        OrderFilter.makeContainerAware(ouChildrenSets);

        List<Entry> result = new LinkedList<Entry>();
        result.add(permissionSetEntry);
        result.add(ouAttributes);
        result.add(ouDirect);
        result.add(ouChildrenSets);
        //result.addAll(permissionEntries);

        return result;
    }
    
    /**
     * Returns a list of entries representing given {@link PermissionData}. The list can
     * be inserted into the DIT as is. Attention: To maintain permissions in insertion order,
     * don't forget to update maxId in the parent!
     * @param permissionData
     * @param maxId the maxid of the parent before adding the new permissions.
     * @param parent
     * @return
     */
    public static List<Entry> permissionStructure(Collection<PermissionData> permissionData, String maxId, Dn parent){
        
        List<Entry> permissions = new LinkedList<Entry>();
        List<Entry> properties = new LinkedList<Entry>();
        List<Entry> propertyValues = new LinkedList<Entry>();
        String id = maxId;
        for(PermissionData p : permissionData){
            String permissionType = p.getType();
            Entry permissionEntry = EntryFactory.javaObject(permissionType, null, parent);
            id = OrderFilter.nextId(id);
            OrderFilter.addId(permissionEntry, id, true);
            permissions.add(permissionEntry);
            
            for(EntryValue entryValue : p.getAttributes().values()){
                String propertyKey = entryValue.getKey();
                Entry propertyEntry = EntryFactory.namedObject(propertyKey, permissionEntry.getDn());
                properties.add(propertyEntry);
                
                List<Entry> tempPropertyValues = new LinkedList<Entry>();
                for(EntryElement entryElement : entryValue.getValue()){
                    String type = entryElement.getType();
                    String value = entryElement.getValue();
                    Entry propertyValueEntry = EntryFactory.javaObject(type, value, propertyEntry.getDn());
                    tempPropertyValues.add(propertyValueEntry);
                }
                OrderFilter.addIds(tempPropertyValues, true);
                propertyValues.addAll(tempPropertyValues);
            }
        }
        //OrderFilter.addIds(permissions, maxId, true);
        List<Entry> result = new LinkedList<Entry>();
        result.addAll(permissions);
        result.addAll(properties);
        result.addAll(propertyValues);
        return result;
    }

}
