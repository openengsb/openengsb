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

package org.openengsb.domain.userprojects;

import java.util.List;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;


/**
 * This is the interface of the User/Projects-Domain. The methods are called by the EngSB to tell the tool which objects
 * should be created, updated or deleted. This functionality must be provided by the connector.
 * 
 * Besides this functional interface, which has to be implemented by connectors, this domain also provides the event
 * interface {@link UserProjectsDomainEvents}, which can be used by connectors.
 */
@SecurityAttribute("domain.userprojects")
public interface UserProjectsDomain extends Domain {

    /**
     * Should update all users in the tool. If some of the users don't exist they should be created.
     * 
     * @param user List of users that should be updated.
     */
    void updateUsers(List<User> users);

    /**
     * Should update all projects in the tool. If some of the projects don't exist they should be created.
     * 
     * @param user List of projects that should be updated.
     */
    void updateProjects(List<Project> projects);

    /**
     * Should update all roles in the tool. If some of the roles don't exist they should be created.
     * 
     * @param user List of roles that should be updated.
     */
    void updateRoles(List<Role> roles);

    /**
     * Should update all assignments in the tool. If some of the assignments don't exist they should be created.
     * 
     * @param user List of assignments that should be updated.
     */
    void updateAssignments(List<Assignment> assignments);

    void deleteUsers(List<User> users);

    void deleteProjects(List<Project> projects);

    void deleteRoles(List<Role> roles);

    void deleteAssignments(List<Assignment> assignments);
}
