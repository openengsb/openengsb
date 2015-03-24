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
import org.openengsb.connector.userprojects.file.internal.file.ProjectFileAccessObject;
import org.openengsb.domain.userprojects.model.Project;

@RunWith(MockitoJUnitRunner.class)
public class ProjectFileAccessObjectTest {

    private static final String RESOURCES_DIR = "src/test/resources";
    private static final File PROJECTS_FILE = new File(RESOURCES_DIR, "projects");
    
    private List<Project> projects = new ArrayList<>();

    @Before
    public void setup() {
        Configuration.get().setProjectsFile(PROJECTS_FILE);
        setupProjects();
    }

    private void setupProjects() {
        Project project = new Project("project1");
        projects.add(project);
        
        project = new Project("project2");
        projects.add(project);
    }

    @Test
    public void testFindAllProjects_shouldFindNonBlankProjects() {
        ProjectFileAccessObject fao = new ProjectFileAccessObject();
        assertEquals(projects, fao.findAllProjects());
    }
}
