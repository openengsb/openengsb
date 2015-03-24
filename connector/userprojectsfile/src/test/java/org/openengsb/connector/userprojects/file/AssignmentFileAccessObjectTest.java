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
package org.openengsb.connector.userprojects.file;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openengsb.connector.userprojects.file.internal.Configuration;
import org.openengsb.connector.userprojects.file.internal.file.AssignmentFileAccessObject;
import org.openengsb.domain.userprojects.model.Assignment;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentFileAccessObjectTest {

    private static final String RESOURCES_DIR = "src/test/resources";
    private static final File ASSIGNMENTS_FILE = new File(RESOURCES_DIR, "assignments");
    
    private List<Assignment> assignments = new ArrayList<>();

    @Before
    public void setup() {
        Configuration.get().setAssignmentsFile(ASSIGNMENTS_FILE);
        
        setupAssignments();
    }

    private void setupAssignments() {
        Assignment assignment = new Assignment("user1", "project1");
        assignment.setRoles(Lists.newArrayList("role1", "role2"));
        assignments.add(assignment);
        
        assignment = new Assignment("user2", "project2");
        assignments.add(assignment);

        assignment = new Assignment("user1", "project2");
        assignments.add(assignment);
    }

    @Test
    public void testFindAllAssignments_shouldFindOnlyCorrectAssignments() {
        AssignmentFileAccessObject fao = new AssignmentFileAccessObject();
        assertEquals(assignments, fao.findAllAssignments());
    }

}
