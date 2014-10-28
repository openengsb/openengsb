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
package org.openengsb.connector.userprojects.file.internal;

import java.io.File;

import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.connector.userprojects.file.internal.file.AssignmentFileAccessObject;
import org.openengsb.connector.userprojects.file.internal.file.ProjectFileAccessObject;
import org.openengsb.connector.userprojects.file.internal.file.RoleFileAccessObject;
import org.openengsb.connector.userprojects.file.internal.file.UserFileAccessObject;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.domain.userprojects.UserProjectsDomainEvents;
import org.openengsb.domain.userprojects.event.UpdateAssignmentEvent;
import org.openengsb.domain.userprojects.event.UpdateProjectsEvent;
import org.openengsb.domain.userprojects.event.UpdateRolesEvent;
import org.openengsb.domain.userprojects.event.UpdateUserEvent;

public final class DefaultSynchronizationService implements SynchronizationService {

    private UserFileAccessObject userFao;
    private ProjectFileAccessObject projectFao;
    private RoleFileAccessObject roleFao;
    private AssignmentFileAccessObject assignmentFao;
    
    private UserProjectsDomainEvents events;
    private AuthenticationContext authenticationContext;

    public void setUserProjectsDomainEvents(UserProjectsDomainEvents domainEvents) {
        events = domainEvents;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void syncFromFilesToOpenEngSB(File baseDir) {
        userFao = new UserFileAccessObject(baseDir);
        projectFao = new ProjectFileAccessObject(baseDir);
        roleFao = new RoleFileAccessObject(baseDir);
        assignmentFao = new AssignmentFileAccessObject(baseDir);
        
        authenticationContext.login("admin", new Password("password"));
        
        syncUsers();
        syncProjects();
        syncRoles();
        syncAssignments();
    }

    private void syncUsers() {
        UpdateUserEvent event = new UpdateUserEvent();
        event.setNewUpdatedUsers(userFao.findAllUsers());
        events.raiseEvent(event);
    }

    private void syncProjects() {
        UpdateProjectsEvent event = new UpdateProjectsEvent();
        event.setUpdatedProjects(projectFao.findAllProjects());
        events.raiseEvent(event);
    }
    
    private void syncRoles() {
        UpdateRolesEvent event = new UpdateRolesEvent();
        event.setUpdatedRoles(roleFao.findAllRoles());
        events.raiseEvent(event);
    }

    private void syncAssignments() {
        UpdateAssignmentEvent event = new UpdateAssignmentEvent();
        event.setUpdatedAssignments(assignmentFao.findAllAssignments());
        events.raiseEvent(event);
    }

}
