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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.openengsb.connector.userprojects.ldap.internal.LdapRuntimeException;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Credential;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.openengsb.infrastructure.ldap.LdapDao;
import org.openengsb.infrastructure.ldap.MissingParentException;
import org.openengsb.infrastructure.ldap.NoSuchNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class DefaultModelManager implements ModelManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelManager.class);

    private LdapDao ldapDao;

    public DefaultModelManager(LdapDao ldapDao) {
        this.ldapDao = ldapDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openengsb.connector.userprojects.ldap.internal.ldap.ModelManager#findUsers()
     */
    @Override
    public List<User> findUsers() {
        List<User> userList;
        try {
            userList = createUsers(ldapDao.getDirectChildren(DnFactory.users()));
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
        return userList;
    }

    private List<User> createUsers(List<Entry> userEntries) throws NoSuchNodeException, MissingParentException {
        List<User> users = Lists.newArrayList();
        for (Entry userEntry : userEntries) {
            User user = new User();
            user.setUsername(userEntry.getDn().getRdn().getValue().getString());
            user.setAttributes(createAttributes(ldapDao.getDirectChildren(DnFactory.userAttributes(user)),
                    user.getUsername()));
            user.setCredentials(createCredentials(ldapDao.getDirectChildren(DnFactory.userCredentials(user)),
                    user.getUsername()));
            users.add(user);
        }
        return users;
    }

    private List<Attribute> createAttributes(List<Entry> attributeEntries, String owner) {
        List<Attribute> attributes = Lists.newArrayList();
        for (Entry attributeEntry : attributeEntries) {
            Attribute attribute = new Attribute();
            attribute.setAttributeName(attributeEntry.getDn().getRdn().getValue().getString());
            attribute.getValues().addAll(
                    Arrays.asList(StringUtils.split(getLdapAttributeValue(attributeEntry),
                            ServerConfig.multipleValueSeparator)));
            attribute.generateUuid(owner);
            attributes.add(attribute);
        }
        return attributes;
    }

    private String getLdapAttributeValue(Entry entry) {
        return Utils.extractAttributeValueNoEmptyCheck(entry, SchemaConstants.STRING_ATTRIBUTE);
    }

    private List<Credential> createCredentials(List<Entry> credentialEntries, String owner) {
        List<Credential> credentials = Lists.newArrayList();
        for (Entry credentialEntry : credentialEntries) {
            Credential credential = new Credential();
            credential.setType(credentialEntry.getDn().getRdn().getValue().getString());
            credential.setValue(getLdapAttributeValue(credentialEntry));
            credential.generateUuid(owner);
            credentials.add(credential);
        }
        return credentials;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openengsb.connector.userprojects.ldap.internal.ldap.ModelManager#findAssignments()
     */
    @Override
    public List<Assignment> findAssignments() {
        List<Assignment> assignmentList;
        try {
            assignmentList = createAssignments(ldapDao.getDirectChildren(DnFactory.assignments()));
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
        return assignmentList;
    }

    private List<Assignment> createAssignments(List<Entry> assignmentEntries) throws NoSuchNodeException,
        MissingParentException {
        List<Assignment> assignments = Lists.newArrayList();
        for (Entry entry : assignmentEntries) {
            Assignment assignment = new Assignment();
            assignment.setProject(getLdapAttributeValue(ldapDao.lookup(DnFactory.assignmentProject(entry.getDn()))));
            assignment.setUser(getLdapAttributeValue(ldapDao.lookup(DnFactory.assignmentUser(entry.getDn()))));

            assignment.setPermissions(getNamesOfDirectChildren(ldapDao.getDirectChildren(DnFactory
                    .assignmentPermissions(entry.getDn()))));
            assignment.setRoles(getNamesOfDirectChildren(ldapDao.getDirectChildren(DnFactory.assignmentRoles(entry
                    .getDn()))));
            assignment.generateUuid();
            assignments.add(assignment);
        }
        return assignments;
    }

    private List<String> getNamesOfDirectChildren(List<Entry> entries) {
        List<String> names = Lists.newArrayList();
        for (Entry entry : entries) {
            names.add(entry.getDn().getRdn().getValue().getString());
        }
        return names;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openengsb.connector.userprojects.ldap.internal.ldap.ModelManager#findProjects()
     */
    @Override
    public List<Project> findProjects() {
        List<Project> projectList;
        try {
            projectList = createProjects(ldapDao.getDirectChildren(DnFactory.projects()));
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
        return projectList;
    }

    private List<Project> createProjects(List<Entry> projectEntries) throws NoSuchNodeException,
        MissingParentException {
        List<Project> projects = Lists.newArrayList();
        for (Entry entry : projectEntries) {
            Project project = new Project();
            project.setName(entry.getDn().getRdn().getValue().getString());
            project.setAttributes(createAttributes(ldapDao.getDirectChildren(DnFactory.projectAttributes(project)),
                    project.getName()));
            projects.add(project);
        }
        return projects;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openengsb.connector.userprojects.ldap.internal.ldap.ModelManager#findRoles()
     */
    @Override
    public List<Role> findRoles() {
        List<Role> roleList;
        try {
            roleList = createRoles(ldapDao.getDirectChildren(DnFactory.roles()));
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
        return roleList;
    }

    private List<Role> createRoles(List<Entry> roleEntries) throws NoSuchNodeException, MissingParentException {
        List<Role> roles = Lists.newArrayList();
        for (Entry entry : roleEntries) {
            Role role = new Role();
            role.setName(entry.getDn().getRdn().getValue().getString());
            role.setPermissions(getNamesOfDirectChildren(ldapDao.getDirectChildren(DnFactory.rolePermissions(role))));
            role.setRoles(getNamesOfDirectChildren(ldapDao.getDirectChildren(DnFactory.roleSubroles(role))));
            roles.add(role);
        }
        return roles;
    }

}
