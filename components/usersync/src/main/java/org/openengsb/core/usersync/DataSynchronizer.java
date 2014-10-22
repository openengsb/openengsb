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
