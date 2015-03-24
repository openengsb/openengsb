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
package org.openengsb.connector.userprojects.file.internal.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openengsb.connector.userprojects.file.internal.Configuration;
import org.openengsb.domain.userprojects.model.Project;

/**
 * The object providing access to the projects file.
 */
public class ProjectFileAccessObject extends BaseFileAccessObject {

    private final File projectsFile;

    public ProjectFileAccessObject() {
        projectsFile = Configuration.get().getProjectsFile();
    }

    /**
     * Finds all the available projects.
     * 
     * @return the list of available projects
     */
    public List<Project> findAllProjects() {
        List<Project> list = new ArrayList<>();
        List<String> projectNames;
        try {
            projectNames = readLines(projectsFile);
        } catch (IOException e) {
            throw new FileBasedRuntimeException(e);
        }
        for (String projectName : projectNames) {
            if (StringUtils.isNotBlank(projectName)) {
                list.add(new Project(projectName));
            }
        }
        
        return list;
    }
}
