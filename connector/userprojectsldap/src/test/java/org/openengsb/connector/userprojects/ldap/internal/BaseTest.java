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

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.integ.ServerIntegrationUtils;
import org.junit.After;
import org.junit.Before;
import org.openengsb.connector.userprojects.ldap.internal.ldap.DnFactory;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Credential;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;
import org.openengsb.infrastructure.ldap.LdapDao;

import com.google.common.collect.Lists;

public abstract class BaseTest extends AbstractLdapTestUnit {
    protected UserProjectsLdapServiceImpl ldapService;
    protected LdapDao ldapDao;

    public BaseTest() {
    }

    @Before
    public void beforeTest() throws Exception {
        setupLdapDao();
        ldapService = new UserProjectsLdapServiceImpl(ldapDao);
    }

    private void setupLdapDao() throws Exception {
        LdapConnection ldapConnection = ServerIntegrationUtils.getAdminConnection(getLdapServer());
        ldapDao = new LdapDao(ldapConnection);
    }

    @After
    public void afterTest() throws Exception {
        clearDit();
        ldapDao.disconnect();
    }

    private void clearDit() throws Exception {
        ldapDao.deleteSubtreeExcludingRoot(DnFactory.assignments());
        ldapDao.deleteSubtreeExcludingRoot(DnFactory.permissions());
        ldapDao.deleteSubtreeExcludingRoot(DnFactory.projects());
        ldapDao.deleteSubtreeExcludingRoot(DnFactory.roles());
        ldapDao.deleteSubtreeExcludingRoot(DnFactory.users());
    }

    protected Assignment createTestAssignment() {
        Assignment assignment = new Assignment();
        assignment.setPermissions(Lists.newArrayList("permission1", "permission2"));
        assignment.setProject("project");
        assignment.setRoles(Lists.newArrayList("role1", "role2"));
        assignment.setUser("user");
        return assignment;
    }

    protected Project createTestProject() {
        Project project = new Project();
        project.setName("project");
        project.setAttributes(Lists.newArrayList(createTestAttribute("att", "val1", "val2")));
        return project;
    }

    protected Role createTestRole() {
        Role role = new Role();
        role.setName("role");
        role.setPermissions(Lists.newArrayList("perm1", "perm2"));
        role.setRoles(Lists.newArrayList("subrole"));
        return role;
    }

    protected User createTestUser() {
        String username = "testUser";
        User user = new User(username);

        Credential password = new Credential();
        password.setType("password");
        password.setValue("password");
        user.getCredentials().addAll(Lists.newArrayList(password));

        user.getAttributes().addAll(Lists.newArrayList(createTestAttribute("attribute", "value")));

        return user;
    }

    protected Attribute createTestAttribute(String name, String... values) {
        Attribute attribute = new Attribute();
        attribute.setAttributeName(name);
        List<Object> valueObjects = Lists.newArrayList();
        valueObjects.addAll(Lists.newArrayList(values));
        attribute.setValues(valueObjects);
        return attribute;
    }

}
