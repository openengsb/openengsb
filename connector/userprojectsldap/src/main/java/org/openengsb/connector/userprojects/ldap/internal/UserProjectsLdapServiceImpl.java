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

import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.connector.userprojects.ldap.internal.ldap.DnFactory;
import org.openengsb.connector.userprojects.ldap.internal.ldap.EntryFactory;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.userprojects.UserProjectsDomain;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Permission;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.openengsb.infrastructure.ldap.LdapDao;
import org.openengsb.infrastructure.ldap.MissingParentException;
import org.openengsb.infrastructure.ldap.NoSuchNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProjectsLdapServiceImpl extends AbstractOpenEngSBConnectorService implements UserProjectsDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProjectsLdapServiceImpl.class);
    private LdapDao ldapDao;

    public UserProjectsLdapServiceImpl() {
    }

    public UserProjectsLdapServiceImpl(LdapDao ldapDao) {
        this.ldapDao = ldapDao;
    }

    @Override
    public AliveState getAliveState() {
        if (ldapDao == null) {
            return AliveState.OFFLINE;
        }
        return AliveState.ONLINE;
    }

    public void setLdapDao(LdapDao dao) {
        ldapDao = dao;
    }

    @Override
    public void updateAssignments(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            storeOverwriteExisting(EntryFactory.assignmentStructure(assignment));
        }
    }

    private void storeOverwriteExisting(List<Entry> entryList) {
        try {
            ldapDao.storeOverwriteExisting(entryList);
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void updatePermissions(List<Permission> permissions) {
        for (Permission permission : permissions) {
            storeOverwriteExisting(EntryFactory.permissionStructure(permission));
        }
    }

    @Override
    public void updateProjects(List<Project> projects) {
        for (Project project : projects) {
            storeOverwriteExisting(EntryFactory.projectStructure(project));
        }
    }

    @Override
    public void updateRoles(List<Role> roles) {
        for (Role role : roles) {
            storeOverwriteExisting(EntryFactory.roleStructure(role));
        }
    }

    @Override
    public void updateUsers(List<User> users) {
        for (User user : users) {
            storeOverwriteExisting(EntryFactory.userStructure(user));
        }
    }

    @Override
    public void deleteAssignments(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            deleteSubtreeIncludingRoot(DnFactory.assignment(assignment));
        }
    }

    private void deleteSubtreeIncludingRoot(Dn dn) {
        try {
            ldapDao.deleteSubtreeIncludingRoot(dn);
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void deletePermissions(List<Permission> permissions) {
        for (Permission permission : permissions) {
            deleteSubtreeIncludingRoot(DnFactory.permission(permission));
        }
    }

    @Override
    public void deleteProjects(List<Project> projects) {
        for (Project project : projects) {
            deleteSubtreeIncludingRoot(DnFactory.project(project));
        }
    }

    @Override
    public void deleteRoles(List<Role> roles) {
        for (Role role : roles) {
            deleteSubtreeIncludingRoot(DnFactory.role(role));
        }
    }

    @Override
    public void deleteUsers(List<User> users) {
        for (User user : users) {
            deleteSubtreeIncludingRoot(DnFactory.user(user));
        }
    }

}
