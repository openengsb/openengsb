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

import java.util.Collection;
import java.util.List;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.api.security.service.AccessDeniedException;
import org.openengsb.core.api.security.service.PermissionSetAlreadyExistsException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.usersync.exception.AuthenticationException;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SyncronizedUserServiceImpl extends AbstractOpenEngSBService implements SyncronizedUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncronizedUserServiceImpl.class);

    private String currentContext;

    private UserDataManager userManager;

    private PersistInterface persistService;

    private QueryInterface queryService;

    private AuthenticationContext authenticationContext;

    @Override
    public void checkinUser(User user) {

        try {
            addUserInUserManager(user);
        } catch (AccessDeniedException e) {
            LOGGER.warn("Could not add user in UserDataManager", e);
        }

        addUserInPersistence(user);
    }

    @Override
    public void checkinUsers(List<User> users) {

        try {
            for (User user : users) {
                addUserInUserManager(user);
            }
        } catch (AccessDeniedException e) {
            LOGGER.warn("Could not add user in UserDataManager", e);
        }

        addUsersInPersistence(users);
    }

    @Override
    public void deleteUser(User user) {
        deleteUserFromUserManager(user.getUsername());
        deleteUserFromPersistence(user);

        deleteAllAssignmentsForUser(user);
    }

    @Override
    public void deleteUsers(List<User> users) {

        for (User user : users) {
            deleteUserFromUserManager(user.getUsername());
            deleteAllAssignmentsForUser(user);
        }

        deleteUsersFromPersistence(users);
    }

    @Override
    public void deleteUserByName(String userName) {
        deleteUser(new User(userName));
    }

    @Override
    public void deleteUsersByName(List<String> userNames) {
        List<User> users = Lists.newArrayList();

        for (String userName : userNames) {
            this.deleteUserByName(userName);
            users.add(new User(userName));
        }

        deleteUsers(users);
    }

    @Override
    public void checkinProject(Project project) {
        checkinProjects(Lists.newArrayList(project));
    }

    @Override
    public void checkinProjects(List<Project> projects) {
        preparePersistenceAccess();

        EKBCommit commit = getEKBCommit();
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
    public void deleteProject(Project project) {
        deleteProjects(Lists.newArrayList(project));
    }

    @Override
    public void deleteProjects(List<Project> projects) {
        preparePersistenceAccess();
        EKBCommit commit = getEKBCommit();

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
    public void deleteProjectByName(String projectName) {
        deleteProject(new Project(projectName));
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
    public void checkinRole(Role role) {
        addRoleInUserManager(role);
        addRoleInPersistence(role);
    }

    @Override
    public void checkinRoles(List<Role> roles) {
        for (Role role : roles) {
            addRoleInUserManager(role);
        }

        addRolesInPersistence(roles);
    }

    @Override
    public void deleteRole(Role role) {
        deleteRoleFromUserManager(role.toString());

        deleteRoleFromPersistence(role);
    }

    @Override
    public void deleteRoles(List<Role> roles) {
        for (Role role : roles) {
            deleteRoleFromUserManager(role.getName());
        }

        deleteRolesFromPersistence(roles);
    }

    @Override
    public void deleteRoleByName(String roleName) {
        deleteRole(new Role(roleName));
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
    public void checkinAssignment(Assignment assignment) {
        addAssignmentInUserManager(assignment);
        addAssignmentInPersistence(assignment);
    }

    @Override
    public void checkinAssignments(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            addAssignmentInUserManager(assignment);
        }

        addAssignmentsInPersistence(assignments);
    }

    @Override
    public void deleteAssignment(Assignment assignment) {
        deleteAssignmentFromUserManager(assignment);
        deleteAssignmentFromPersistence(assignment);
    }

    @Override
    public void deleteAssignment(String userName, String project) {

        Assignment queryObj = new Assignment();
        queryObj.setUser(userName);
        queryObj.setProject(project);
        List<Assignment> assignments = queryForAssignments(queryObj);

        if (assignments.isEmpty()) {
            LOGGER.warn("Assignment {1}:{2} does not exist.", userName, project);
        }

        deleteAssignment(assignments.get(0));
    }

    @Override
    public void deleteAssignments(List<Assignment> assignments) {

        for (Assignment assignment : assignments) {
            deleteAssignmentFromUserManager(assignment);
        }

        deleteAssignmentsFromPersistence(assignments);

    }

    @Override
    public void deleteAllAssignmentsForProject(String projectName) {
        Assignment queryObj = new Assignment();
        queryObj.setProject(projectName);
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    @Override
    public void deleteAllAssignmentsForProject(Project project) {
        Assignment queryObj = new Assignment();
        queryObj.setProject(project.getName());
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    @Override
    public void deleteAllAssignmentsForUser(String userName) {
        Assignment queryObj = new Assignment();
        queryObj.setUser(userName);
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    @Override
    public void deleteAllAssignmentsForUser(User user) {
        Assignment queryObj = new Assignment();
        queryObj.setUser(user.getUsername());
        List<Assignment> assignments = queryForAssignments(queryObj);

        deleteAssignmentsFromPersistence(assignments);
    }

    @Override
    public AliveState getAliveState() {
        return userManager == null || persistService == null || queryService == null ? AliveState.OFFLINE
            : AliveState.ONLINE;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
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

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // ++ Methods for UserDataManager Access +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public void addUserInUserManager(User user) {
        // Add user
        if (!userManager.getUserList().contains(user.getUsername())) {
            try {
                userManager.createUser(user.getUsername());
            } catch (UserExistsException e) {
                LOGGER.error("User already exists!", e);
                return;
            }
        }

        // Add attributes
        for (Attribute attribute : user.getAttributes()) {
            userManager.setUserAttribute(user.getUsername(), attribute.getAttributeName(), attribute.getValues()
                    .toArray());
        }
    }

    private void deleteUserFromUserManager(String userName) {
        if (userManager.getUserList().contains(userName)) {
            userManager.deleteUser(userName);
        }
    }

    public void addRoleInUserManager(Role role) {

        String setName = role.getName();

        if (!userManager.getPermissionSetList().contains(setName)) {
            try {
                LOGGER.debug("Create permissionset: " + setName);
                userManager.createPermissionSet(setName);
            } catch (PermissionSetAlreadyExistsException e) {
                LOGGER.error("This is Impossible.");
            } catch (Exception e) {
                LOGGER.error("Entry exists exception. This seems impossible.", e);
            }
        }
    }

    private void deleteRoleFromUserManager(String role) {
        if (userManager.getPermissionSetList().contains(role)) {

            // Delete PermissionSet from users
            for (String user : userManager.getUserList()) {
                if (userManager.getPermissionSetsFromUser(user).contains(role)) {
                    userManager.removePermissionSetFromUser(user, role);
                }
            }

            // Delete PermissionSet from PermissionSets
            for (String permissionSet : userManager.getPermissionSetList()) {
                if (userManager.getPermissionSetsFromPermissionSet(permissionSet).contains(role)) {
                    userManager.removePermissionSetFromPermissionSet(permissionSet, role);
                }
            }
        }
    }

    private void addAssignmentInUserManager(Assignment assignment) {
        String user = assignment.getUser();

        for (String permissionSet : assignment.getRoles()) {
            if (userManager.getPermissionSetList().contains(permissionSet)) {
                LOGGER.debug("Adding Role: " + permissionSet);
                userManager.addPermissionSetToUser(user, permissionSet);
            } else {
                LOGGER.debug("Role not found: " + permissionSet);
            }
        }
    }

    private void deleteAssignmentFromUserManager(Assignment assignment) {

        String user = assignment.getUser();

        for (String permissionSet : assignment.getRoles()) {
            userManager.removePermissionSetFromUser(user, permissionSet);
        }
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // ++ Methods for Persistance Access +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void addUserInPersistence(User user) {
        addUsersInPersistence(Sets.newHashSet(user));
    }

    private void addUsersInPersistence(Collection<User> users) {
        preparePersistenceAccess();
        EKBCommit commit = getEKBCommit();

        for (User user : users) {
            List<User> result = queryService.queryByString(User.class, "username:\"" + user.getUsername() + "\"");

            if (result.size() == 0) {
                LOGGER.debug("Create User " + user.getUsername());
                commit.addInsert(user);
            } else if (result.size() == 1) {
                LOGGER.debug("Update User " + user.getUsername());
                commit.addUpdate(user);
            } else {
                LOGGER.debug("Error: Duplicate users in EngSB");
                commit.addUpdate(user);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    private void deleteUserFromPersistence(User user) {
        deleteUsersFromPersistence(Sets.newHashSet(user));
    }

    private void deleteUsersFromPersistence(Collection<User> users) {
        preparePersistenceAccess();
        EKBCommit commit = getEKBCommit();

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

    private void addRoleInPersistence(Role role) {
        addRolesInPersistence(Sets.newHashSet(role));
    }

    private void addRolesInPersistence(Collection<Role> roles) {
        preparePersistenceAccess();
        EKBCommit commit = getEKBCommit();

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

    private void deleteRoleFromPersistence(Role role) {
        deleteRolesFromPersistence(Sets.newHashSet(role));
    }

    private void deleteRolesFromPersistence(Collection<Role> roles) {
        preparePersistenceAccess();
        EKBCommit commit = getEKBCommit();

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

    private void addAssignmentInPersistence(Assignment assignment) {
        addAssignmentsInPersistence(Sets.newHashSet(assignment));
    }

    private void addAssignmentsInPersistence(Collection<Assignment> assignments) {
        preparePersistenceAccess();
        EKBCommit commit = getEKBCommit();

        for (Assignment assignment : assignments) {
            List<Assignment> result =
                queryService.queryByString(Assignment.class, "uuid:\"" + assignment.getUuid() + "\"");

            if (result.size() == 0) {
                LOGGER.debug("New Assignment: " + assignment.getUser() + ":" + assignment.getProject());
                commit.addInsert(assignment);
            } else {
                LOGGER.debug("Update Assignment: " + assignment.getUser() + ":" + assignment.getProject());
                commit.addUpdate(assignment);
            }
        }

        persistService.commit(commit);
        revokePersistenceAccess();
    }

    private void deleteAssignmentFromPersistence(Assignment assignment) {
        deleteAssignmentsFromPersistence(Sets.newHashSet(assignment));
    }

    private void deleteAssignmentsFromPersistence(Collection<Assignment> assignments) {
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

        if (assignment.getUser() != null && !assignment.getUser().equals("")) {
            query += "user:\"" + assignment.getUser() + "\"";
        }

        if (assignment.getProject() != null && !assignment.getProject().equals("")) {
            query += "project:\"" + assignment.getProject() + "\"";
        }

        return queryService.queryByString(Assignment.class, query);
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
            throw new AuthenticationException("A user with DB access must be logged in.");
        }

        currentContext = ContextHolder.get().getCurrentContextId();
        ContextHolder.get().setCurrentContextId("up-context");
    }
    
    private void revokePersistenceAccess() {

        ContextHolder.get().setCurrentContextId(currentContext);
    }
}
