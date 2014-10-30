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
import java.util.Set;
import java.util.Timer;

import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.connector.userprojects.file.internal.file.AssignmentFileAccessObject;
import org.openengsb.connector.userprojects.file.internal.file.ProjectFileAccessObject;
import org.openengsb.connector.userprojects.file.internal.file.RoleFileAccessObject;
import org.openengsb.connector.userprojects.file.internal.file.UserFileAccessObject;
import org.openengsb.core.api.context.ContextHolder;
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

    private Timer timer = new Timer();

    public void setUserProjectsDomainEvents(UserProjectsDomainEvents domainEvents) {
        events = domainEvents;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void syncFromFilesToOpenEngSB() {
        userFao = new UserFileAccessObject();
        projectFao = new ProjectFileAccessObject();
        roleFao = new RoleFileAccessObject();
        assignmentFao = new AssignmentFileAccessObject();

        startSync();
    }

    private void startSync() {
        Configuration config = Configuration.get();
        FileWatcher fileWatcher =
            new FileWatcher(config.getUsersFile(), config.getProjectsFile(), config.getRolesFile(),
                    config.getAssignmentsFile()) {

                @Override
                protected void onFilesModified(Set<File> files) {
                    syncFromFiles(files);
                }
            };

        timer.schedule(fileWatcher, 5000, 1000);
    }
    
    private void syncFromFiles(Set<File> files) {
        String oldContext = ContextHolder.get().getCurrentContextId();
        ContextHolder.get().setCurrentContextId("userprojects-file");
        authenticationContext.login("admin", new Password("password"));

        if (files.contains(Configuration.get().getUsersFile())) {
            syncUsers();
        }
        if (files.contains(Configuration.get().getProjectsFile())) {
            syncProjects();
        }
        if (files.contains(Configuration.get().getRolesFile())) {
            syncRoles();
        }
        if (files.contains(Configuration.get().getAssignmentsFile())) {
            syncAssignments();
        }

        ContextHolder.get().setCurrentContextId(oldContext);
    }

    private void syncUsers() {
        if (!Configuration.get().getUsersFile().exists()) {
            return;
        }
        UpdateUserEvent event = new UpdateUserEvent();
        event.setNewUpdatedUsers(userFao.findAllUsers());
        events.raiseEvent(event);
    }

    private void syncProjects() {
        if (!Configuration.get().getProjectsFile().exists()) {
            return;
        }
        UpdateProjectsEvent event = new UpdateProjectsEvent();
        event.setUpdatedProjects(projectFao.findAllProjects());
        events.raiseEvent(event);
    }

    private void syncRoles() {
        if (!Configuration.get().getRolesFile().exists()) {
            return;
        }
        UpdateRolesEvent event = new UpdateRolesEvent();
        event.setUpdatedRoles(roleFao.findAllRoles());
        events.raiseEvent(event);
    }

    private void syncAssignments() {
        if (!Configuration.get().getAssignmentsFile().exists()) {
            return;
        }
        UpdateAssignmentEvent event = new UpdateAssignmentEvent();
        event.setUpdatedAssignments(assignmentFao.findAllAssignments());
        events.raiseEvent(event);
    }

    public void destroy() {
        timer.cancel();
    }
}
