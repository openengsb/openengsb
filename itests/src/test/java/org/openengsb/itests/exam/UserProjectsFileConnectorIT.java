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
package org.openengsb.itests.exam;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.domain.userprojects.UserProjectsDomain;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.PaxExam;

import com.google.common.collect.Lists;

@RunWith(PaxExam.class)
public class UserProjectsFileConnectorIT extends AbstractPreConfiguredExamTestHelper {

    private static final String BASE_DIR = "data/userprojectsfile";
    private static final File ASSIGNMENTS_FILE = new File(BASE_DIR, "assignments");
    private static final File PROJECTS_FILE = new File(BASE_DIR, "projects");
    private static final File ROLES_FILE = new File(BASE_DIR, "roles");
    private static final File USERS_FILE = new File(BASE_DIR, "users");

    private List<Assignment> assignments = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Role> roles = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    @Inject
    private QueryInterface queryInterface;

    @Before
    public void setup() throws Exception {
        authenticateAsAdmin();
        setupAssignments();
        setupProjects();
        setupRoles();
        setupUsers();
    }

    private void setupAssignments() {
        Assignment assignment = new Assignment("user1", "project1");
        assignment.setRoles(Lists.newArrayList("role1", "role2"));
        assignments.add(assignment);

        assignments.add(new Assignment("user2", "project2"));
        assignments.add(new Assignment("user1", "project2"));
    }

    private void setupProjects() {
        projects.add(new Project("project1"));
        projects.add(new Project("project2"));
    }

    private void setupRoles() {
        roles.add(new Role("role1"));
        roles.add(new Role("role2"));
        Role role = new Role("role3");
        role.setRoles(Arrays.asList("role1", "role2"));
        roles.add(role);
        roles.add(new Role("role4"));
    }

    private void setupUsers() {
        users.add(new User("user1"));
        users.add(new User("user2"));
    }

    @Test
    public void testSyncFromFilesToOpenEngSB_shouldThrowTheRespectiveEvents() throws InterruptedException,
        IOException {
        assertTrue(queryInterface.queryForActiveModels(Assignment.class).isEmpty());
        assertTrue(queryInterface.queryForActiveModels(Project.class).isEmpty());
        assertTrue(queryInterface.queryForActiveModels(Role.class).isEmpty());
        assertTrue(queryInterface.queryForActiveModels(User.class).isEmpty());

        getOsgiService(UserProjectsDomain.class, 5000);
        copyFiles();
        Thread.sleep(10000);

        assertAssignmentsExist(queryInterface.queryForActiveModels(Assignment.class));
        assertProjectsExist(queryInterface.queryForActiveModels(Project.class));
        assertRolesExist(queryInterface.queryForActiveModels(Role.class));
        assertUsersExist(queryInterface.queryForActiveModels(User.class));

        for (User user : users) {
            System.out.println(user.getUsername());
        }

    }

    private void assertAssignmentsExist(List<Assignment> actualAssignments) {
        Assert.assertThat(actualAssignments,
                CoreMatchers.hasItems(assignments.toArray(new Assignment[assignments.size()])));
    }

    private void assertProjectsExist(List<Project> actualProjects) {
        Assert.assertThat(actualProjects, CoreMatchers.hasItems(projects.toArray(new Project[projects.size()])));
    }

    private void assertRolesExist(List<Role> actualRoles) {
        Assert.assertThat(actualRoles, CoreMatchers.hasItems(roles.toArray(new Role[roles.size()])));
    }

    private void assertUsersExist(List<User> actualUsers) {
        Assert.assertThat(actualUsers, CoreMatchers.hasItems(users.toArray(new User[users.size()])));
    }

    private void copyFiles() throws IOException {
        copyFile(ASSIGNMENTS_FILE);
        copyFile(PROJECTS_FILE);
        copyFile(ROLES_FILE);
        copyFile(USERS_FILE);
    }

    private void copyFile(File file) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        Files.copy(getFileAsStream(file), Paths.get(file.getPath()));
    }

    private InputStream getFileAsStream(File file) {
        return getClass().getClassLoader().getResourceAsStream(file.getPath().replaceAll(File.separator, "/"));
    }

}
