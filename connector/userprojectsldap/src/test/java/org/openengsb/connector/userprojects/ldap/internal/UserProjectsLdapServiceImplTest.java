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

package org.openengsb.connector.userprojects.ldap.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.connector.userprojects.ldap.internal.ldap.DnFactory;
import org.openengsb.connector.userprojects.ldap.internal.ldap.SchemaConstants;
import org.openengsb.connector.userprojects.ldap.internal.ldap.ServerConfig;
import org.openengsb.connector.userprojects.ldap.internal.ldap.Utils;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Credential;
import org.openengsb.domain.userprojects.model.Permission;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.openengsb.infrastructure.ldap.MissingParentException;
import org.openengsb.infrastructure.ldap.NoSuchNodeException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(FrameworkRunner.class)
@ApplyLdifFiles({ "ldap/openengsbSchema.ldif", "ldap/partitionBasicStructure.ldif" })
@CreateDS(allowAnonAccess = true, name = "default", partitions = { @CreatePartition(name = "openengsb",
        suffix = "dc=openengsb,dc=org") })
@CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP") })
public class UserProjectsLdapServiceImplTest extends BaseTest {

    @Test
    public void testUpdateAssignments_shouldCreateAssignment() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Assignment assignment = createTestAssignment();
        ldapService.updateAssignments(Lists.newArrayList(assignment));
        assertThat(ldapDao.exists(DnFactory.assignment(assignment)), is(true));
        assertCorrectlyStored(assignment);
    }

    private void assertCorrectlyStored(Assignment assignment) throws NoSuchNodeException, MissingParentException,
        LdapInvalidAttributeValueException {
        Entry entry = ldapDao.lookup(DnFactory.assignmentProject(assignment));
        assertThat(entry.get(SchemaConstants.STRING_ATTRIBUTE).getString(), is(assignment.getProject()));

        entry = ldapDao.lookup(DnFactory.assignmentUser(assignment));
        assertThat(entry.get(SchemaConstants.STRING_ATTRIBUTE).getString(), is(assignment.getUser()));

        List<Entry> entryList = ldapDao.getDirectChildren(DnFactory.assignmentPermissions(assignment));
        Collection<String> actualCollection = Lists.newArrayList();
        for (Entry entry2 : entryList) {
            actualCollection.add(entry2.getDn().getRdn().getValue().getString());
        }
        assertTrue(CollectionUtils.isEqualCollection(actualCollection, assignment.getPermissions()));

        entryList = ldapDao.getDirectChildren(DnFactory.assignmentRoles(assignment));
        actualCollection.clear();
        for (Entry entry2 : entryList) {
            actualCollection.add(entry2.getDn().getRdn().getValue().getString());
        }
        assertTrue(CollectionUtils.isEqualCollection(actualCollection, assignment.getRoles()));
    }

    @Test
    public void testUpdateAssignments_shouldUpdateAssignment() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Assignment assignment = createTestAssignment();
        ldapService.updateAssignments(Lists.newArrayList(assignment));
        assignment.getPermissions().add("new");
        assignment.getRoles().add("new");
        ldapService.updateAssignments(Lists.newArrayList(assignment));
        assertCorrectlyStored(assignment);
    }

    @Test
    public void testUpdatePermissions_shouldCreatePermission() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Permission permission = createTestPermission();
        ldapService.updatePermissions(Lists.newArrayList(permission));
        assertThat(ldapDao.exists(DnFactory.permission(permission)), is(true));
        assertCorrectlyStored(permission);
    }

    private Permission createTestPermission() {
        return new Permission("comp", "read");
    }

    private void assertCorrectlyStored(Permission permission) throws NoSuchNodeException, MissingParentException,
        LdapInvalidAttributeValueException {
        Entry entry = ldapDao.lookup(DnFactory.permissionComponent(permission));
        assertThat(entry.get(SchemaConstants.STRING_ATTRIBUTE).getString(), is(permission.getComponentName()));

        entry = ldapDao.lookup(DnFactory.permissionAction(permission));
        assertThat(entry.get(SchemaConstants.STRING_ATTRIBUTE).getString(), is(permission.getAction()));
    }

    @Test
    public void testUpdatePermissions_shouldUpdatePermission() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Permission permission = createTestPermission();
        ldapService.updatePermissions(Lists.newArrayList(permission));
        permission.setComponentName(permission.getComponentName() + "new");
        permission.setAction(permission.getAction() + "new");
        ldapService.updatePermissions(Lists.newArrayList(permission));
        assertCorrectlyStored(permission);
    }

    @Test
    public void testUpdateProjects_shouldCreateProject() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Project project = createTestProject();
        ldapService.updateProjects(Lists.newArrayList(project));
        assertThat(ldapDao.exists(DnFactory.project(project)), is(true));
        assertAttributesCorrectlyStored(project);
    }

    private void assertAttributesCorrectlyStored(Project project) throws NoSuchNodeException, MissingParentException,
        LdapInvalidAttributeValueException {
        for (Attribute attribute : project.getAttributes()) {
            Entry attributeEntry = ldapDao.lookup(DnFactory.projectAttribute(project, attribute));
            assertAttributeCorrectlyStored(attributeEntry, attribute);
        }
    }

    private void assertAttributeCorrectlyStored(Entry attributeEntry, Attribute attribute)
        throws NoSuchNodeException, MissingParentException {
        Set<String> actualAttributeValues =
            Sets.newHashSet(StringUtils.split(
                    Utils.extractAttributeValueNoEmptyCheck(attributeEntry, SchemaConstants.STRING_ATTRIBUTE),
                    ServerConfig.multipleValueSeparator));
        Set<String> expectedAttributeValues = Sets.newHashSet();
        for (Object value : attribute.getValues()) {
            expectedAttributeValues.add((String) value);
        }
        assertThat(actualAttributeValues, equalTo(expectedAttributeValues));
    }

    @Test
    public void testUpdateProjects_shouldUpdateProject() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Project project = createTestProject();
        ldapService.updateProjects(Lists.newArrayList(project));
        project.getAttributes().add(createTestAttribute("attr88", "val88"));
        ldapService.updateProjects(Lists.newArrayList(project));
        assertAttributesCorrectlyStored(project);
    }

    @Test
    public void testUpdateRoles_shouldCreateRole() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Role role = createTestRole();
        ldapService.updateRoles(Lists.newArrayList(role));
        assertThat(ldapDao.exists(DnFactory.role(role)), is(true));
        assertCorrectlyStored(role);
    }

    private void assertCorrectlyStored(Role role) throws NoSuchNodeException, MissingParentException {
        List<Entry> entryList = ldapDao.getDirectChildren(DnFactory.rolePermissions(role));
        List<String> actualCollection = Lists.newArrayList();
        for (Entry entry : entryList) {
            actualCollection.add(entry.getDn().getRdn().getValue().getString());
        }
        assertTrue(CollectionUtils.isEqualCollection(actualCollection, role.getPermissions()));

        entryList = ldapDao.getDirectChildren(DnFactory.roleSubroles(role));
        actualCollection.clear();
        for (Entry entry : entryList) {
            actualCollection.add(entry.getDn().getRdn().getValue().getString());
        }
        assertTrue(CollectionUtils.isEqualCollection(actualCollection, role.getRoles()));
    }

    @Test
    public void testUpdateRoles_shouldUpdateRole() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        Role role = createTestRole();
        ldapService.updateRoles(Lists.newArrayList(role));
        role.getPermissions().add("update");
        role.getRoles().clear();
        ldapService.updateRoles(Lists.newArrayList(role));
        assertCorrectlyStored(role);
    }

    @Test
    public void testUpdateUsers_shouldCreateUser() throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        User user = createTestUser();
        ldapService.updateUsers(Lists.newArrayList(user));
        assertThat(ldapDao.exists(DnFactory.user(user)), is(true));
        assertCredentialsCorrectlyStored(user);
        assertAttributesCorrectlyStored(user);
    }

    private void assertCredentialsCorrectlyStored(User user) throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        for (Credential credential : user.getCredentials()) {
            assertCredentialCorrectlyStored(user, credential);
        }
    }

    private void assertCredentialCorrectlyStored(User user, Credential credential) throws NoSuchNodeException,
        MissingParentException, LdapInvalidAttributeValueException {
        Entry credentialEntry = ldapDao.lookup(DnFactory.userCredential(user, credential));
        assertThat(credentialEntry.get(SchemaConstants.STRING_ATTRIBUTE).getString(), is(credential.getValue()));
    }

    private void assertAttributesCorrectlyStored(User user) throws LdapInvalidAttributeValueException,
        NoSuchNodeException, MissingParentException {
        for (Attribute attribute : user.getAttributes()) {
            Entry attributeEntry = ldapDao.lookup(DnFactory.userAttribute(user, attribute));
            assertAttributeCorrectlyStored(attributeEntry, attribute);
        }
    }

    @Test
    public void testUpdateUsers_shouldUpdateUser() throws NoSuchNodeException, MissingParentException,
        LdapInvalidAttributeValueException {
        User user = createTestUser();
        ldapService.updateUsers(Lists.newArrayList(user));

        Credential credential = user.getCredentials().iterator().next();
        credential.setValue(credential.getValue() + "new");

        Attribute attribute1 = user.getAttributes().iterator().next();
        attribute1.getValues().add("new");

        Attribute attribute2 = createTestAttribute("attribute2", "value");
        user.getAttributes().add(attribute2);

        ldapService.updateUsers(Lists.newArrayList(user));

        assertCredentialsCorrectlyStored(user);
        assertAttributesCorrectlyStored(user);
    }

    @Test
    public void testDeleteAssignments_shouldDelete() {
        Assignment assignment = createTestAssignment();
        List<Assignment> list = Lists.newArrayList(assignment);
        ldapService.updateAssignments(list);
        Dn dn = DnFactory.assignment(assignment);
        assertThat(ldapDao.exists(dn), is(true));
        ldapService.deleteAssignments(list);
        assertThat(ldapDao.exists(dn), is(false));
    }

    @Test
    public void testDeletePermissions_shouldDelete() {
        Permission permission = createTestPermission();
        List<Permission> list = Lists.newArrayList(permission);
        ldapService.updatePermissions(list);
        Dn dn = DnFactory.permission(permission);
        assertThat(ldapDao.exists(dn), is(true));
        ldapService.deletePermissions(list);
        assertThat(ldapDao.exists(dn), is(false));
    }

    @Test
    public void testDeleteProjects_shouldDelete() {
        Project project = createTestProject();
        List<Project> list = Lists.newArrayList(project);
        ldapService.updateProjects(list);
        Dn dn = DnFactory.project(project);
        assertThat(ldapDao.exists(dn), is(true));
        ldapService.deleteProjects(list);
        assertThat(ldapDao.exists(dn), is(false));
    }

    @Test
    public void testDeleteRoles_shouldDelete() {
        Role role = createTestRole();
        List<Role> list = Lists.newArrayList(role);
        ldapService.updateRoles(list);
        Dn dn = DnFactory.role(role);
        assertThat(ldapDao.exists(dn), is(true));
        ldapService.deleteRoles(list);
        assertThat(ldapDao.exists(dn), is(false));
    }

    @Test
    public void testDeleteUsers_shouldDelete() {
        User user = createTestUser();
        List<User> list = Lists.newArrayList(user);
        ldapService.updateUsers(list);
        Dn dn = DnFactory.user(user);
        assertThat(ldapDao.exists(dn), is(true));
        ldapService.deleteUsers(list);
        assertThat(ldapDao.exists(dn), is(false));
    }
}
