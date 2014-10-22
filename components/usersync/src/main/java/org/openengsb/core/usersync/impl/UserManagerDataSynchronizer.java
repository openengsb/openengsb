package org.openengsb.core.usersync.impl;

import java.util.List;

import org.openengsb.core.api.security.service.AccessDeniedException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.usersync.DataSynchronizer;
import org.openengsb.core.usersync.exception.SynchronizationException;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManagerDataSynchronizer implements DataSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagerDataSynchronizer.class);

    private UserDataManager userManager;

    @Override
    public void checkinUsers(List<User> users) throws SynchronizationException {
        try {
            for (User user : users) {
                addUserInUserManager(user);
            }
        } catch (AccessDeniedException e) {
            throw new SynchronizationException("Access Denied", e);
        }
    }

    @Override
    public void deleteUsers(List<User> users) {
        for (User user : users) {
            deleteUserFromUserManager(user.getUsername());
        }
    }

    @Override
    public void deleteUsersByName(List<String> userNames) {
        for (String userName : userNames) {
            deleteUserFromUserManager(userName);
        }
    }

    @Override
    public void checkinProjects(List<Project> projects) {
        // Nothing to do as the UserDataManager does not support projects
    }

    @Override
    public void deleteProjects(List<Project> project) {
        // Nothing to do as the UserDataManager does not support projects
    }

    @Override
    public void deleteProjectsByName(List<String> projectNames) {
        // Nothing to do as the UserDataManager does not support projects
    }

    @Override
    public void checkinRoles(List<Role> roles) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteRoles(List<Role> roles) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteRolesByName(List<String> roleNames) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void checkinAssignments(List<Assignment> assignments) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteAssignment(String userName, String project) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteAssignments(List<Assignment> assignments) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteAllAssignmentsForProject(String projectName) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteAllAssignmentsForProject(Project project) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteAllAssignmentsForUser(String userName) {
        // Nothing to do as the UserDataManager does not support roles
    }

    @Override
    public void deleteAllAssignmentsForUser(User user) {
        // Nothing to do as the UserDataManager does not support roles
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // ++ Methods for UserDataManager Access +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public void addUserInUserManager(User user) {
        if (!userManager.getUserList().contains(user.getUsername())) {
            try {
                userManager.createUser(user.getUsername());
            } catch (UserExistsException e) {
                LOGGER.warn("User already exists!", e);
                return;
            }
        }
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
}
