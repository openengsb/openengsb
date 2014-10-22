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

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.usersync.DataSynchronizer;
import org.openengsb.core.usersync.exception.SynchronizationException;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class EkbDataSynchronizer implements DataSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagerDataSynchronizer.class);

    private String currentContext;

    private AuthenticationContext authenticationContext;

    private PersistInterface persistService;

    private QueryInterface queryService;

    @Override
    public void checkinUsers(List<User> users) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (User user : users) {
            List<User> result = queryService.queryByString(User.class, "username:\"" + user.getUsername() + "\"");

            if (result.size() == 0) {
                LOGGER.info("Create User " + user.getUsername());
                commit.addInsert(user);
            } else if (result.size() == 1) {
                LOGGER.info("Update User " + user.getUsername());
                commit.addUpdate(user);
            } else {
                LOGGER.error("Error: Duplicate users in EngSB");
                commit.addUpdate(user);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteUsers(List<User> users) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (User user : users) {
            List<User> result = queryService.queryByString(User.class, "username:\"" + user.getUsername() + "\"");

            if (result.size() == 0) {
                LOGGER.warn("User {1} does not exist.", user.getUsername());
            } else {
                commit.addDelete(user);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteUsersByName(List<String> userNames) {
        List<User> users = new ArrayList<>();

        for (String userName : userNames) {
            users.add(new User(userName));
        }

        deleteUsers(users);
    }

    @Override
    public void checkinProjects(List<Project> projects) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (Project project : projects) {
            List<Project> result = queryService.queryByString(Project.class, "name:\"" + project.getName() + "\"");

            if (result.size() == 0) {
                commit.addInsert(project);
            } else {
                commit.addUpdate(project);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteProjects(List<Project> projects) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (Project project : projects) {
            List<Project> result = queryService.queryByString(Project.class, "name:\"" + project.getName() + "\"");

            if (result.size() == 0) {
                LOGGER.warn("Project {1} does not exist.", project.getName());
            } else {
                commit.addDelete(project);
            }
            deleteAllAssignmentsForProject(project);
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteProjectsByName(List<String> projectNames) {
        List<Project> projects = Lists.newArrayList();

        for (String projectName : projectNames) {
            projects.add(new Project(projectName));
        }

        deleteProjects(projects);
    }

    @Override
    public void checkinRoles(List<Role> roles) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (Role role : roles) {
            List<Role> result = queryService.queryByString(Role.class, "name:\"" + role.getName() + "\"");

            if (result.size() == 0) {
                commit.addInsert(role);
            } else {
                commit.addUpdate(role);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteRoles(List<Role> roles) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (Role role : roles) {
            List<Role> result = queryService.queryByString(Role.class, "name:\"" + role.getName() + "\"");

            if (result.size() == 0) {
                LOGGER.warn("User {1} does not exist.", role.getName());
            } else {
                commit.addDelete(role);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteRolesByName(List<String> roleNames) {
        List<Role> roles = Lists.newArrayList();

        for (String roleName : roleNames) {
            roles.add(new Role(roleName));
        }

        deleteRoles(roles);
    }

    @Override
    public void checkinAssignments(List<Assignment> assignments) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (Assignment assignment : assignments) {
            String query =
                "userName:\"" + assignment.getUserName() + "\" and projectName:\"" + assignment.getProjectName()
                    + "\"";
            List<Assignment> result = queryService.queryByString(Assignment.class, query);

            if (result.size() == 0) {
                commit.addInsert(assignment);
            } else {
                commit.addUpdate(assignment);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteAssignment(String userName, String project) {
        Assignment queryObj = new Assignment();
        queryObj.setUserName(userName);
        queryObj.setProjectName(project);
        List<Assignment> assignments = queryForAssignments(queryObj);

        if (assignments.isEmpty()) {
            LOGGER.warn("Assignment {1}:{2} does not exist.", userName, project);
        }

        deleteAssignments(assignments);
    }

    @Override
    public void deleteAssignments(List<Assignment> assignments) {
        EKBCommit commit = getEKBCommit();
        preparePersistenceAccess();

        for (Assignment assignment : assignments) {
            List<Role> result = queryService.queryByString(Role.class, "uuid:\"" + assignment.getUuid() + "\"");

            if (result.size() == 0) {
                LOGGER.warn("User {1} does not exist.", assignment.getUuid());
            } else {
                commit.addDelete(assignment);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    @Override
    public void deleteAllAssignmentsForProject(String projectName) {
        Assignment queryObj = new Assignment();
        queryObj.setProjectName(projectName);
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    @Override
    public void deleteAllAssignmentsForProject(Project project) {
        Assignment queryObj = new Assignment();
        queryObj.setProjectName(project.getName());
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    @Override
    public void deleteAllAssignmentsForUser(String userName) {
        Assignment queryObj = new Assignment();
        queryObj.setUserName(userName);
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    @Override
    public void deleteAllAssignmentsForUser(User user) {
        Assignment queryObj = new Assignment();
        queryObj.setUserName(user.getUsername());
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    public void setPersistService(PersistInterface persistService) {
        this.persistService = persistService;
    }

    public void setQueryService(QueryInterface queryService) {
        this.queryService = queryService;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    private void deleteAssignmentsFromPersistence(List<Assignment> assignments) {
        preparePersistenceAccess();
        EKBCommit commit = getEKBCommit();

        for (Assignment assignment : assignments) {
            List<Role> result = queryService.queryByString(Role.class, "uuid:\"" + assignment.getUuid() + "\"");

            if (result.size() == 0) {
                LOGGER.warn("User {1} does not exist.", assignment.getUuid());
            } else {
                commit.addDelete(assignment);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    private List<Assignment> queryForAssignments(Assignment assignment) {

        String query = "";

        if (assignment.getUserName() != null && !assignment.getUserName().equals("")) {
            query += "userName:\"" + assignment.getUserName() + "\"";
        }

        if (assignment.getProjectName() != null && !assignment.getProjectName().equals("")) {
            query += "projectName:\"" + assignment.getProjectName() + "\"";
        }

        preparePersistenceAccess();
        List<Assignment> result = queryService.queryByString(Assignment.class, query);
        revokePersistenceAccess();

        return result;
    }

    private EKBCommit getEKBCommit() {
        EKBCommit result = new EKBCommit();

        result.setDomainId("userprojects");
        result.setConnectorId("upload");
        result.setInstanceId("upload");

        return result;
    }

    private void preparePersistenceAccess() {

        if (authenticationContext.getAuthenticatedPrincipal() == null
            || !(authenticationContext.getAuthenticatedPrincipal() instanceof String)) {
            throw new SynchronizationException("A user with DB access must be logged in.");
        }

        currentContext = ContextHolder.get().getCurrentContextId();
        ContextHolder.get().setCurrentContextId("up-context");
    }

    private void revokePersistenceAccess() {

        ContextHolder.get().setCurrentContextId(currentContext);
    }
}
