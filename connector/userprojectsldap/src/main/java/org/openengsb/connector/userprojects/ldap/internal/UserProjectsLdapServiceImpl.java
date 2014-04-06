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

public class UserProjectsLdapServiceImpl extends AbstractOpenEngSBConnectorService implements
        UserProjectsDomain {

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
        try {
            for (Assignment assignment : assignments) {
                ldapDao.storeOverwriteExisting(EntryFactory.assignmentStructure(assignment));
            }
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void updatePermissions(List<Permission> permissions) {
        try {
            for (Permission permission : permissions) {
                ldapDao.storeOverwriteExisting(EntryFactory.permissionStructure(permission));
            }
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void updateProjects(List<Project> projects) {
        try {
            for (Project project : projects) {
                ldapDao.storeOverwriteExisting(EntryFactory.projectStructure(project));
            }
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void updateRoles(List<Role> roles) {
        try {
            for (Role role : roles) {
                ldapDao.storeOverwriteExisting(EntryFactory.roleStructure(role));
            }
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void updateUsers(List<User> users) {
        try {
            for (User user : users) {
                ldapDao.storeOverwriteExisting(EntryFactory.userStructure(user));
            }
        } catch (NoSuchNodeException | MissingParentException e) {
            LOGGER.error("LDAP exception", e);
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void deleteAssignments(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            try {
                ldapDao.deleteSubtreeIncludingRoot(DnFactory.assignment(assignment));
            } catch (NoSuchNodeException | MissingParentException e) {
                LOGGER.error("LDAP exception", e);
                throw new LdapRuntimeException(e);
            }
        }
    }

    @Override
    public void deletePermissions(List<Permission> permissions) {
        for (Permission permission : permissions) {
            try {
                ldapDao.deleteSubtreeIncludingRoot(DnFactory.permission(permission));
            } catch (NoSuchNodeException | MissingParentException e) {
                LOGGER.error("LDAP exception", e);
                throw new LdapRuntimeException(e);
            }
        }
    }

    @Override
    public void deleteProjects(List<Project> projects) {
        for (Project project : projects) {
            try {
                ldapDao.deleteSubtreeIncludingRoot(DnFactory.project(project));
            } catch (NoSuchNodeException | MissingParentException e) {
                LOGGER.error("LDAP exception", e);
                throw new LdapRuntimeException(e);
            }
        }
    }

    @Override
    public void deleteRoles(List<Role> roles) {
        for (Role role : roles) {
            try {
                ldapDao.deleteSubtreeIncludingRoot(DnFactory.role(role));
            } catch (NoSuchNodeException | MissingParentException e) {
                LOGGER.error("LDAP exception", e);
                throw new LdapRuntimeException(e);
            }
        }
    }

    @Override
    public void deleteUsers(List<User> users) {
        for (User user : users) {
            try {
                ldapDao.deleteSubtreeIncludingRoot(DnFactory.user(user));
            } catch (NoSuchNodeException | MissingParentException e) {
                LOGGER.error("LDAP exception", e);
                throw new LdapRuntimeException(e);
            }
        }
    }
    
}
