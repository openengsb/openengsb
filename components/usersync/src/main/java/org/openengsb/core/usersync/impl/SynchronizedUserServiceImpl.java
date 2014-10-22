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
package org.openengsb.core.usersync.impl;

import java.util.List;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.usersync.DataSynchronizer;
import org.openengsb.core.usersync.SynchronizedUserService;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SynchronizedUserServiceImpl extends AbstractOpenEngSBService implements SynchronizedUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizedUserServiceImpl.class);

    private List<DataSynchronizer> dataSynchronizers;

    @Override
    public void checkinUser(User user) {
        checkinUsers(Lists.newArrayList(user));
    }

    @Override
    public void checkinUsers(List<User> users) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.checkinUsers(users);
        }
    }

    @Override
    public void deleteUser(User user) {
        deleteUsers(Lists.newArrayList(user));
    }

    @Override
    public void deleteUsers(List<User> users) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteUsers(users);
        }
    }

    @Override
    public void deleteUserByName(String userName) {
        deleteUsersByName(Lists.newArrayList(userName));
    }

    @Override
    public void deleteUsersByName(List<String> userNames) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteUsersByName(userNames);
        }
    }

    @Override
    public void checkinProject(Project project) {
        checkinProjects(Lists.newArrayList(project));
    }

    @Override
    public void checkinProjects(List<Project> projects) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.checkinProjects(projects);
        }
    }

    @Override
    public void deleteProject(Project project) {
        deleteProjects(Lists.newArrayList(project));
    }

    @Override
    public void deleteProjects(List<Project> projects) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteProjects(projects);
        }
    }

    @Override
    public void deleteProjectByName(String projectName) {
        deleteProjectsByName(Lists.newArrayList(projectName));
    }

    @Override
    public void deleteProjectsByName(List<String> projectNames) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteProjectsByName(projectNames);
        }
    }

    @Override
    public void checkinRole(Role role) {
        checkinRoles(Lists.newArrayList(role));
    }

    @Override
    public void checkinRoles(List<Role> roles) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.checkinRoles(roles);
        }
    }

    @Override
    public void deleteRole(Role role) {
        deleteRoles(Lists.newArrayList(role));
    }

    @Override
    public void deleteRoles(List<Role> roles) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteRoles(roles);
        }
    }

    @Override
    public void deleteRoleByName(String roleName) {
        deleteRolesByName(Lists.newArrayList(roleName));
    }

    @Override
    public void deleteRolesByName(List<String> roleNames) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteRolesByName(roleNames);
        }
    }

    @Override
    public void checkinAssignment(Assignment assignment) {
        checkinAssignments(Lists.newArrayList(assignment));
    }

    @Override
    public void checkinAssignments(List<Assignment> assignments) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.checkinAssignments(assignments);
        }
    }

    @Override
    public void deleteAssignment(Assignment assignment) {
        deleteAssignments(Lists.newArrayList(assignment));
    }

    @Override
    public void deleteAssignment(String userName, String project) {
        // TODO Implement me.
    }

    @Override
    public void deleteAssignments(List<Assignment> assignments) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.checkinAssignments(assignments);
        }
    }

    @Override
    public void deleteAllAssignmentsForProject(String projectName) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteAllAssignmentsForProject(projectName);
        }
    }

    @Override
    public void deleteAllAssignmentsForProject(Project project) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteAllAssignmentsForProject(project);
        }
    }

    @Override
    public void deleteAllAssignmentsForUser(String userName) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteAllAssignmentsForUser(userName);
        }
    }

    @Override
    public void deleteAllAssignmentsForUser(User user) {
        for (DataSynchronizer synchronizer : dataSynchronizers) {
            synchronizer.deleteAllAssignmentsForUser(user);
        }
    }

    public void setDataSynchronizers(List<DataSynchronizer> dataSynchronizers) {
        this.dataSynchronizers = dataSynchronizers;
    }

    @Override
    public AliveState getAliveState() {
        return dataSynchronizers == null ? AliveState.OFFLINE : AliveState.ONLINE;
    }

}
