/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.openengsb.issues.common.model;

import java.net.URI;
import java.util.List;

public class Project {

    private String id;
    private String name;
    private ProjectStatus status;
    private String description;
    private URI uri;
    private List<Issue> issues;

    /**
     * Supplies the identifier of the project
     *
     * @return id - identifier of the project
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the project to identify it.
     *
     * @param id - the id identifies the project
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Supplies the name of the project
     *
     * @return name - short description of the project
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the project in the form of a short
     *
     * @param name - short description
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Supplies the topically status of the project
     *
     * @return status - the topically status of the project
     * @see ProjectStatus
     */
    public ProjectStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the project
     *
     * @param projectStatus - use the enumaration values for setting
     * @see ProjectStatus
     */
    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    /**
     * Supplies the detailed description of the project
     *
     * @return description - in detailed form
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the project
     *
     * @param description - a detailed description of the project
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Supplies the URI of the project
     *
     * @return uri - place on the file system
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI of the project
     *
     * @param uri - place on the file system(remote in most cases)
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the issues of the project
     *
     * @return issues - reported issues of the project
     * @see Issue
     */
    public List<Issue> getIssues() {
        return issues;
    }

    /**
     * Sets the issues for this project
     *
     * @param issues - a list of issues
     * @see Issue
     */
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

}