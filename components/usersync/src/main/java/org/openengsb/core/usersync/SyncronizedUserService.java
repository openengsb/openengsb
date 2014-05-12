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

package org.openengsb.core.usersync;

import java.util.List;

import org.openengsb.core.api.AliveState;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;

public interface SyncronizedUserService {

    // Basic user operations methods
    void checkinUser(User user);
    
    void checkinUsers(List<User> users);

    void deleteUser(User user);

    void deleteUsers(List<User> user);

    void deleteUserByName(String userName);

    void deleteUsersByName(List<String> userNames);

    // Basic project operations
    void checkinProject(Project project);

    void checkinProjects(List<Project> projects);

    void deleteProject(Project project);

    void deleteProjects(List<Project> project);

    void deleteProjectByName(String projectName);

    void deleteProjectsByName(List<String> projectNames);

    // Basic role operations
    void checkinRole(Role role);

    void checkinRoles(List<Role> roles);

    void deleteRole(Role role);

    void deleteRoles(List<Role> role);

    void deleteRoleByName(String roleName);

    void deleteRolesByName(List<String> roleNames);

    // Basic assignment operations
    void checkinAssignment(Assignment assignment);

    void checkinAssignments(List<Assignment> assignments);

    void deleteAssignment(Assignment assignment);

    void deleteAssignment(String userName, String project);

    void deleteAssignments(List<Assignment> assignments);

    // Special Assignment operations
    void deleteAllAssignmentsForProject(String projectName);

    void deleteAllAssignmentsForProject(Project project);

    void deleteAllAssignmentsForUser(String usreName);

    void deleteAllAssignmentsForUser(User user);

    AliveState getAliveState();
}
