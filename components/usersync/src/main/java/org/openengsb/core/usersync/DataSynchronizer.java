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

import org.openengsb.core.usersync.exception.SynchronizationException;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;

public interface DataSynchronizer {

    void checkinUsers(List<User> users) throws SynchronizationException;

    void deleteUsers(List<User> user) throws SynchronizationException;

    void deleteUsersByName(List<String> userNames) throws SynchronizationException;

    void checkinProjects(List<Project> projects) throws SynchronizationException;

    void deleteProjects(List<Project> project) throws SynchronizationException;

    void deleteProjectsByName(List<String> projectNames) throws SynchronizationException;

    // Basic role operations
    void checkinRoles(List<Role> roles) throws SynchronizationException;

    void deleteRoles(List<Role> role) throws SynchronizationException;

    void deleteRolesByName(List<String> roleNames) throws SynchronizationException;

    // Basic assignment operations
    void checkinAssignments(List<Assignment> assignments) throws SynchronizationException;

    void deleteAssignment(String userName, String project) throws SynchronizationException;

    void deleteAssignments(List<Assignment> assignments) throws SynchronizationException;

    // Special Assignment operations
    /**
     * Deletes all assignments for a specific project.
     * 
     * @param projectName name of the project where all assignments should be deleted.
     */
    void deleteAllAssignmentsForProject(String projectName) throws SynchronizationException;

    /**
     * Deletes all assignments for a specific project.
     * 
     * @param project project where all assignments should be deleted.
     */
    void deleteAllAssignmentsForProject(Project project) throws SynchronizationException;

    /**
     * Deletes all assignments for a specific user.
     * 
     * @param userName name of the user where all assignments should be deleted.
     */
    void deleteAllAssignmentsForUser(String userName) throws SynchronizationException;

    /**
     * Deletes all assignments for a specific user.
     * 
     * @param user user where all assignments should be deleted.
     */
    void deleteAllAssignmentsForUser(User user) throws SynchronizationException;
}
