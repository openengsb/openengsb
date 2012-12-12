/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.services.internal.security.ldap;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.core.services.internal.security.model.EntryElement;
import org.openengsb.core.services.internal.security.model.EntryValue;
import org.openengsb.core.services.internal.security.model.PermissionData;

/**
 * Builds entries for various nodes in the DIT. The returned entries have a valid Dn and all provided attributes.
 * */
public final class EntryFactory {

    private EntryFactory() {
    }

    /**
     * Returns an {@link Entry} whose {@link Dn} is baseDn followed by ou as Rdn. The attribute type of the Rdn is
     * 'organizational unit'.
     * */
    public static Entry organizationalUnit(String ou, Dn baseDn) {
        Dn dn = LdapUtils.concatDn(SchemaConstants.ouAttribute, ou, baseDn);
        Entry entry = new DefaultEntry(dn);
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.organizationalUnitOc);
            entry.add(SchemaConstants.ouAttribute, ou);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
        return entry;
    }

    /**
     * Returns an {@link Entry} whose {@link Dn} is baseDn followed by name as Rdn. The attribute type of the Rdn is
     * 'common name'.
     * */
    public static Entry namedObject(String name, Dn baseDn) {
        Dn dn = LdapUtils.concatDn(SchemaConstants.cnAttribute, name, baseDn);
        Entry entry = new DefaultEntry(dn);
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.namedObjectOc);
            entry.add(SchemaConstants.cnAttribute, name);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
        return entry;
    }

    /**
     * Returns an {@link Entry} whose {@link Dn} is baseDn followed by name as Rdn. The attribute type of the Rdn is
     * 'common name'. The description is optional in that it may also be null or the empty String. Callers can rely on
     * this distinction, i.e. storing an empty string will also retrieve an empty string and storing null will also
     * retrieve null.
     * */
    public static Entry namedDescriptiveObject(String name, String description, Dn baseDn) throws LdapRuntimeException {
        Entry entry = namedObject(name, baseDn);
        addDescription(entry, description);
        return entry;
    }

    /**
     * Returns an {@link Entry} whose Dn is baseDn followed by classname as Rdn. The attribute type of the Rdn is
     * javaClassName. The constructorArgument is treated as an optional description as explained in
     * {@link #namedDescriptiveObject(String, String, Dn)}.
     * */
    public static Entry javaObject(String classname, String constructorArgument, Dn baseDn) {
        Dn dn = LdapUtils.concatDn(SchemaConstants.javaClassNameAttribute, classname, baseDn);
        Entry entry = new DefaultEntry(dn);
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.javaClassInstanceOc);
            entry.add(SchemaConstants.javaClassNameAttribute, classname);
            addDescription(entry, constructorArgument);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
        return entry;
    }
    
  
    
    /**
     * Returns a list of entries representing a permissionSet. The list should not be reordered since its order follows
     * the tree structure of the DIT. It can be inserted into the DIT right away.
     */
    public static List<Entry> globalPermissionSetStructure(String permissionSet) {
        Entry permissionSetEntry = namedObject(permissionSet, SchemaConstants.ouGlobalPermissionSets());
        Entry ouDirect = organizationalUnit("direct", permissionSetEntry.getDn());
        Entry ouChildrenSets = organizationalUnit("childrenSets", permissionSetEntry.getDn());
        Entry ouAttributes = organizationalUnit("attributes", permissionSetEntry.getDn());
        return Arrays.asList(permissionSetEntry, ouAttributes, ouDirect, ouChildrenSets);
    }

    /**
     * Returns a list of entries representing a list of {@link PermissionData}. The list should not be reordered since
     * its order follows the tree structure of the DIT. It can be inserted into the DIT right away.
     */
    public static List<Entry> permissionStructureFromPermissionData(Collection<PermissionData> data, Dn baseDn) {

        List<Entry> permissions = new LinkedList<Entry>();
        List<Entry> properties = new LinkedList<Entry>();
        List<Entry> propertyValues = new LinkedList<Entry>();
        List<Entry> result = new LinkedList<Entry>();

        for (PermissionData p : data) {
            String permissionType = p.getType();
            Entry permissionEntry = javaObject(permissionType, null, baseDn);
            TimebasedOrderFilter.addId(permissionEntry, true);
            permissions.add(permissionEntry);

            for (EntryValue entryValue : p.getAttributes().values()) {
                String propertyKey = entryValue.getKey();
                Entry propertyEntry = namedObject(propertyKey, permissionEntry.getDn());
                properties.add(propertyEntry);

                for (EntryElement entryElement : entryValue.getValue()) {
                    String type = entryElement.getType();
                    String value = entryElement.getValue();
                    Entry propertyValueEntry = javaObject(type, value, propertyEntry.getDn());
                    TimebasedOrderFilter.addId(propertyValueEntry, true);
                    propertyValues.add(propertyValueEntry);
                }
            }
        }
        result.addAll(permissions);
        result.addAll(properties);
        result.addAll(propertyValues);
        return result;
    }

    /**
     * 3 possibilities:<br>
     * 1) description is null -> emptyFlag = false, no description attribute <br>
     * 2) description is empty -> emptyFlag = true, no description attribute <br>
     * 3) description exists -> no emptyFlag, description attribute exists and has a value
     * */
    private static void addDescription(Entry entry, String description) {
        try {
            entry.add(SchemaConstants.objectClassAttribute, SchemaConstants.descriptiveObjectOc);
            if (description == null) {
                entry.add(SchemaConstants.emptyFlagAttribute, String.valueOf(false)); // case 1
            } else if (description.isEmpty()) {
                entry.add(SchemaConstants.emptyFlagAttribute, String.valueOf(true)); // case 2
            } else {
                entry.add(SchemaConstants.stringAttribute, description); // case 3
            }
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

}
