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

package org.openengsb.connector.userprojects.ldap.internal.ldap;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.connector.userprojects.ldap.internal.LdapRuntimeException;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Credential;
import org.openengsb.domain.userprojects.model.Permission;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;

/**
 * Builds entries for various nodes in the DIT. The returned entries have a valid Dn and all provided attributes.
 * */
public final class EntryFactory {

    private EntryFactory() {
    }

    /**
     * Returns a list of entries representing an assignment. The list should not be reordered since its order follows
     * the tree structure of the DIT. It can be inserted into the DIT right away.
     */
    public static List<Entry> assignmentStructure(Assignment assignment) {
        List<Entry> entryList = new LinkedList<>();
        entryList.add(namedObject(DnFactory.assignment(assignment)));
        entryList.add(namedDescriptiveObject(DnFactory.assignmentProject(assignment), assignment.getProject()));
        entryList.add(namedDescriptiveObject(DnFactory.assignmentUser(assignment), assignment.getUser()));
        entryList.addAll(createAssignmentPermissionsEntries(assignment));
        entryList.addAll(createAssignmentRolesEntries(assignment));
        return entryList;
    }

    private static List<Entry> createAssignmentPermissionsEntries(Assignment assignment) {
        List<Entry> entryList = new LinkedList<>();
        Entry entry = organizationalUnit(DnFactory.assignmentPermissions(assignment));
        entryList.add(entry);
        for (String permission : assignment.getPermissions()) {
            entryList.add(namedObject(DnFactory.assignmentPermission(assignment, permission)));
        }
        return entryList;
    }

    private static List<Entry> createAssignmentRolesEntries(Assignment assignment) {
        List<Entry> entryList = new LinkedList<>();
        Entry entry = organizationalUnit(DnFactory.assignmentRoles(assignment));
        entryList.add(entry);
        for (String role : assignment.getRoles()) {
            entryList.add(namedObject(DnFactory.assignmentRole(assignment, role)));
        }
        return entryList;
    }

    /**
     * Returns a list of entries representing a permission. The list should not be reordered since its order follows the
     * tree structure of the DIT. It can be inserted into the DIT right away.
     */
    public static List<Entry> permissionStructure(Permission permission) {
        List<Entry> entryList = new LinkedList<>();
        entryList.add(namedObject(DnFactory.permission(permission)));
        entryList.add(namedDescriptiveObject(DnFactory.permissionAction(permission), permission.getAction()));
        entryList
                .add(namedDescriptiveObject(DnFactory.permissionComponent(permission), permission.getComponentName()));
        return entryList;
    }

    /**
     * Returns a list of entries representing a project. The list should not be reordered since its order follows the
     * tree structure of the DIT. It can be inserted into the DIT right away.
     */
    public static List<Entry> projectStructure(Project project) {
        List<Entry> entryList = new LinkedList<>();
        entryList.add(namedObject(DnFactory.project(project)));
        entryList.addAll(createProjectAttributesEntries(project));
        return entryList;
    }

    private static List<Entry> createProjectAttributesEntries(Project project) {
        List<Entry> entryList = new LinkedList<>();
        Entry userAttributesEntry = organizationalUnit(DnFactory.projectAttributes(project));
        entryList.add(userAttributesEntry);
        for (Attribute attribute : project.getAttributes()) {
            entryList.add(namedDescriptiveObject(DnFactory.projectAttribute(project, attribute), attribute
                    .getValues().toArray(new String[] {})));
        }
        return entryList;
    }

    /**
     * Returns a list of entries representing a role. The list should not be reordered since its order follows the tree
     * structure of the DIT. It can be inserted into the DIT right away.
     */
    public static List<Entry> roleStructure(Role role) {
        List<Entry> entryList = new LinkedList<>();
        entryList.add(namedObject(DnFactory.role(role)));
        entryList.addAll(createRolePermissionsEntries(role));
        entryList.addAll(createRoleSubrolesEntries(role));
        return entryList;
    }

    private static List<Entry> createRolePermissionsEntries(Role role) {
        List<Entry> entryList = new LinkedList<>();
        Entry entry = organizationalUnit(DnFactory.rolePermissions(role));
        entryList.add(entry);
        for (String permission : role.getPermissions()) {
            entryList.add(namedObject(DnFactory.rolePermission(role, permission)));
        }
        return entryList;
    }

    private static List<Entry> createRoleSubrolesEntries(Role role) {
        List<Entry> entryList = new LinkedList<>();
        Entry entry = organizationalUnit(DnFactory.roleSubroles(role));
        entryList.add(entry);
        for (String subrole : role.getRoles()) {
            entryList.add(namedObject(DnFactory.roleSubrole(role, subrole)));
        }
        return entryList;
    }

    /**
     * Returns a list of entries representing the {@link User} along with its credentials and attributes. The list
     * should not be reordered since its order follows the tree structure of the DIT. It can be inserted into the DIT
     * right away.
     */
    public static List<Entry> userStructure(User user) {
        List<Entry> entryList = new LinkedList<>();
        entryList.add(namedObject(user.getUsername(), DnFactory.users()));
        entryList.addAll(createCredentialsEntries(user));
        entryList.addAll(createUserAttributesEntries(user));
        return entryList;
    }

    private static List<Entry> createCredentialsEntries(User user) {
        List<Entry> entryList = new LinkedList<>();
        Entry credentialsEntry = organizationalUnit(DnFactory.userCredentials(user));
        entryList.add(credentialsEntry);
        for (Credential credential : user.getCredentials()) {
            entryList.add(namedDescriptiveObject(DnFactory.userCredential(user, credential), credential.getValue()));
        }
        return entryList;
    }

    private static List<Entry> createUserAttributesEntries(User user) {
        List<Entry> entryList = new LinkedList<>();
        Entry userAttributesEntry = organizationalUnit(DnFactory.userAttributes(user));
        entryList.add(userAttributesEntry);
        for (Attribute attribute : user.getAttributes()) {
            entryList.add(namedDescriptiveObject(DnFactory.userAttribute(user, attribute), attribute.getValues()
                    .toArray(new String[] {})));
        }
        return entryList;
    }

    /**
     * Returns an {@link Entry} with the given {@link Dn}. The attribute type of the Rdn is 'organizational unit'.
     * */
    public static Entry organizationalUnit(Dn dn) {
        Entry entry = new DefaultEntry(dn);
        try {
            entry.add(SchemaConstants.OBJECT_CLASS_ATTRIBUTE, SchemaConstants.ORGANIZATIONAL_UNIT_OC);
            entry.add(SchemaConstants.OU_ATTRIBUTE, dn.getRdn().getName());
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
        return entry;
    }

    /**
     * Returns an {@link Entry} having the given {@link Dn}. The attribute type of the Rdn is 'common name'.
     * */
    public static Entry namedObject(Dn dn) {
        Entry entry = new DefaultEntry(dn);
        try {
            entry.add(SchemaConstants.OBJECT_CLASS_ATTRIBUTE, SchemaConstants.NAMED_OBJECT_OC);
            entry.add(SchemaConstants.CN_ATTRIBUTE, dn.getRdn().getName());
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
        Dn dn = Utils.concatDn(SchemaConstants.CN_ATTRIBUTE, name, baseDn);
        return namedObject(dn);
    }

    /**
     * Returns an {@link Entry} having the given {@link Dn}. The attribute type of the Rdn is 'common name'. The
     * description is optional in that it may also be null or the empty String. Callers can rely on this distinction,
     * i.e. storing an empty string will also retrieve an empty string and storing null will also retrieve null.
     * */
    public static Entry namedDescriptiveObject(Dn dn, String... descriptions) {
        Entry entry = namedObject(dn);
        addDescriptions(entry, descriptions);
        return entry;
    }

    /**
     * 3 possibilities:<br>
     * 1) description is null -> emptyFlag = false, no description attribute <br>
     * 2) description is empty -> emptyFlag = true, no description attribute <br>
     * 3) description exists -> no emptyFlag, description attribute exists and has a value
     * */
    private static void addDescriptions(Entry entry, String... descriptions) {
        try {
            entry.add(SchemaConstants.OBJECT_CLASS_ATTRIBUTE, SchemaConstants.DESCRIPTIVE_OBJECT_OC);
            if (descriptions == null) {
                // case 1
                entry.add(SchemaConstants.EMPTY_FLAG_ATTRIBUTE, String.valueOf(false));
            } else if (descriptions.length == 1 && descriptions[0].isEmpty()) {
                // case 2
                entry.add(SchemaConstants.EMPTY_FLAG_ATTRIBUTE, String.valueOf(true));
            } else {
                // case 3
                entry.add(SchemaConstants.STRING_ATTRIBUTE,
                        StringUtils.join(descriptions, ServerConfig.multipleValueSeparator));
            }
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

}
