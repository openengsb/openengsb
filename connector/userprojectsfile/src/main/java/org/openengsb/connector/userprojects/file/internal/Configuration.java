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
package org.openengsb.connector.userprojects.file.internal;

import java.io.File;

public final class Configuration {

    private static final Configuration SELF = new Configuration();
    
    private File baseDir = new File("data/userprojectsfile");
    private File usersFile = new File(baseDir, "users");
    private File projectsFile = new File(baseDir, "projects");
    private File rolesFile = new File(baseDir, "roles");
    private File assignmentsFile = new File(baseDir, "assignments");
    
    private String associationSeparator = ":-:";
    private String valueSeparator = ",-,";
    
    private Configuration() {
    }
    
    public static Configuration get() {
        return SELF;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = new File(baseDir);
    }

    public File getUsersFile() {
        return usersFile;
    }

    public void setUsersFile(File usersFile) {
        this.usersFile = usersFile;
    }

    public void setUsersFile(String fileName) {
        usersFile = new File(getBaseDir(), fileName);
    }

    public File getProjectsFile() {
        return projectsFile;
    }

    public void setProjectsFile(File projectsFile) {
        this.projectsFile = projectsFile;
    }

    public void setProjectsFile(String fileName) {
        projectsFile = new File(getBaseDir(), fileName);
    }

    public File getRolesFile() {
        return rolesFile;
    }

    public void setRolesFile(File rolesFile) {
        this.rolesFile = rolesFile;
    }

    public void setRolesFile(String fileName) {
        rolesFile = new File(getBaseDir(), fileName);
    }

    public File getAssignmentsFile() {
        return assignmentsFile;
    }

    public void setAssignmentsFile(File assignmentsFile) {
        this.assignmentsFile = assignmentsFile;
    }

    public void setAssignmentsFile(String fileName) {
        assignmentsFile = new File(getBaseDir(), fileName);
    }

    public String getAssociationSeparator() {
        return associationSeparator;
    }

    public void setAssociationSeparator(String associationSeparator) {
        this.associationSeparator = associationSeparator;
    }

    public String getValueSeparator() {
        return valueSeparator;
    }

    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }
}
