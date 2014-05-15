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

import org.openengsb.connector.userprojects.ldap.internal.ldap.DefaultModelManager;
import org.openengsb.connector.userprojects.ldap.internal.ldap.ModelManager;
import org.openengsb.domain.userprojects.UserProjectsDomainEvents;
import org.openengsb.domain.userprojects.event.UpdateAssignmentEvent;
import org.openengsb.domain.userprojects.event.UpdateProjectsEvent;
import org.openengsb.domain.userprojects.event.UpdateRolesEvent;
import org.openengsb.domain.userprojects.event.UpdateUserEvent;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.openengsb.infrastructure.ldap.LdapDao;

public final class DefaultSynchronizationService implements SynchronizationService {

    private ModelManager modelManager;

    private UserProjectsDomainEvents events;

    public void setUserProjectsDomainEvents(UserProjectsDomainEvents domainEvents) {
        events = domainEvents;
    }

    @Override
    public void syncFromLdapServerToOpenEngSB(LdapDao ldapDao) {
        modelManager = new DefaultModelManager(ldapDao);
        syncUsers();
        syncProjects();
        syncRoles();
        syncAssignments();
    }

    private void syncUsers() {
        List<User> userList = modelManager.findUsers();
        UpdateUserEvent event = new UpdateUserEvent();
        event.setNewUpdatedUsers(userList);
        events.raiseEvent(event);
    }

    private void syncProjects() {
        List<Project> projectList = modelManager.findProjects();
        UpdateProjectsEvent event = new UpdateProjectsEvent();
        event.setUpdatedProjects(projectList);
        events.raiseEvent(event);
    }

    private void syncRoles() {
        List<Role> roleList = modelManager.findRoles();
        UpdateRolesEvent event = new UpdateRolesEvent();
        event.setUpdatedRoles(roleList);
        events.raiseEvent(event);
    }

    private void syncAssignments() {
        List<Assignment> assignmentList = modelManager.findAssignments();
        UpdateAssignmentEvent event = new UpdateAssignmentEvent();
        event.setUpdatedAssignments(assignmentList);
        events.raiseEvent(event);
    }

}
