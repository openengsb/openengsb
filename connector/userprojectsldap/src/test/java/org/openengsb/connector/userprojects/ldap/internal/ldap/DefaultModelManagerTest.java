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

package org.openengsb.connector.userprojects.ldap.internal.ldap;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.connector.userprojects.ldap.internal.BaseTest;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;

@RunWith(FrameworkRunner.class)
@ApplyLdifFiles({ "ldap/openengsbSchema.ldif", "ldap/partitionBasicStructure.ldif" })
@CreateDS(allowAnonAccess = true, name = "default", partitions = { @CreatePartition(name = "openengsb",
        suffix = "dc=openengsb,dc=org") })
@CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP") })
public class DefaultModelManagerTest extends BaseTest {

    private DefaultModelManager modelManager;

    @Override
    @Before
    public void beforeTest() throws Exception {
        super.beforeTest();
        modelManager = new DefaultModelManager(ldapDao);
    }

    @Test
    @Ignore
    public void testFindAssignments() throws InterruptedException {
        Assignment assignment = createTestAssignment();
        List<Assignment> expected = Arrays.asList(assignment);
        ldapService.updateAssignments(expected);
        assertTrue(CollectionUtils.isEqualCollection(modelManager.findAssignments(), expected));
    }
    
    @Test
    @Ignore
    public void testFindProjects() throws InterruptedException {
        Project project = createTestProject();
        List<Project> expected = Arrays.asList(project);
        ldapService.updateProjects(expected);
        assertTrue(CollectionUtils.isEqualCollection(modelManager.findProjects(), expected));
    }
    
    @Test
    @Ignore
    public void testFindRoles() throws InterruptedException {
        Role role = createTestRole();
        List<Role> expected = Arrays.asList(role);
        ldapService.updateRoles(expected);
        assertTrue(CollectionUtils.isEqualCollection(modelManager.findRoles(), expected));
    }
    
    @Test
    @Ignore
    public void testFindUsers() throws InterruptedException {
        User user = createTestUser();
        List<User> expected = Arrays.asList(user);
        ldapService.updateUsers(expected);
        assertTrue(CollectionUtils.isEqualCollection(modelManager.findUsers(), expected));
    }

}
