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
import org.openengsb.infrastructure.ldap.util.TimebasedOrderFilter;

/**
 * Creates subtrees representing permissions and permission sets.
 * */
public final class PermissionsUtils {

    private PermissionsUtils() {
    }

    /**
     * Returns a list of entries representing a permissionSet. The list should not be reordered since its order follows
     * the tree structure of the DIT. It can be inserted into the DIT right away.
     */
    public static List<Entry> globalPermissionSetStructure(String permissionSet) {
        Entry permissionSetEntry = EntryFactory.namedObject(permissionSet, SchemaConstants.ouGlobalPermissionSets());
        Entry ouDirect = EntryFactory.organizationalUnit("direct", permissionSetEntry.getDn());
        Entry ouChildrenSets = EntryFactory.organizationalUnit("childrenSets", permissionSetEntry.getDn());
        Entry ouAttributes = EntryFactory.organizationalUnit("attributes", permissionSetEntry.getDn());
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
            Entry permissionEntry = EntryFactory.javaObject(permissionType, null, baseDn);
            TimebasedOrderFilter.addId(permissionEntry, true);
            permissions.add(permissionEntry);

            for (EntryValue entryValue : p.getAttributes().values()) {
                String propertyKey = entryValue.getKey();
                Entry propertyEntry = EntryFactory.namedObject(propertyKey, permissionEntry.getDn());
                properties.add(propertyEntry);

                for (EntryElement entryElement : entryValue.getValue()) {
                    String type = entryElement.getType();
                    String value = entryElement.getValue();
                    Entry propertyValueEntry = EntryFactory.javaObject(type, value, propertyEntry.getDn());
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

}
