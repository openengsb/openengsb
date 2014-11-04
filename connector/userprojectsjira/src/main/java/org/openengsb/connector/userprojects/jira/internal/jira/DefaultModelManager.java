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
package org.openengsb.connector.userprojects.jira.internal.jira;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Attribute;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.User;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.google.common.collect.Lists;

public final class DefaultModelManager implements ModelManager {

    private JiraClient jiraClient;

    public DefaultModelManager(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openengsb.connector.userprojects.jira.internal.jira.ModelManager#findUsers()
     */
    @Override
    public List<User> findUsers() {
        return createUsers(jiraClient.findUsers(jiraClient.findProjects()));
    }

    private List<User> createUsers(Iterable<com.atlassian.jira.rest.client.api.domain.User> jiraUsers) {
        List<User> users = Lists.newArrayList();
        for (com.atlassian.jira.rest.client.api.domain.User jiraUser : jiraUsers) {
            User user = new User();
            user.setUsername(jiraUser.getName());
            user.setAttributes(createAttributes(createUserAttributeMap(jiraUser), jiraUser.getName()));
            users.add(user);
        }
        return users;
    }

    private Map<String, String> createUserAttributeMap(com.atlassian.jira.rest.client.api.domain.User jiraUser) {
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("displayName", jiraUser.getDisplayName());
        attributeMap.put("emailAddress", jiraUser.getEmailAddress());
        return attributeMap;
    }

    private List<Attribute> createAttributes(Map<String, String> attributeMap, String uuidGenerationKey) {
        List<Attribute> attributes = Lists.newArrayList();
        for (Entry<String, String> attributeEntry : attributeMap.entrySet()) {
            Attribute attribute = new Attribute();
            attribute.setAttributeName(attributeEntry.getKey());
            attribute.getValues().add(attributeEntry.getValue());
            attribute.generateUuid(uuidGenerationKey);
            attributes.add(attribute);
        }
        return attributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openengsb.connector.userprojects.jira.internal.jira.ModelManager#findProjects()
     */
    @Override
    public List<Project> findProjects() {
        return createProjects(jiraClient.findProjects());
    }

    private List<Project> createProjects(Iterable<BasicProject> jiraProjects) {
        List<Project> projects = Lists.newArrayList();
        for (BasicProject jiraProject : jiraProjects) {
            projects.add(createProject(jiraProject));
        }
        return projects;
    }

    private Project createProject(BasicProject jiraProject) {
        Project project = new Project();
        project.setName(jiraProject.getName());
        project.setAttributes(createAttributes(createProjectAttributeMap(jiraProject), jiraProject.getName()));
        return project;
    }

    private Map<String, String> createProjectAttributeMap(BasicProject jiraProject) {
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("key", jiraProject.getKey());
        return attributeMap;
    }

    @Override
    public List<Assignment> findAssignments() {
        List<Assignment> assignments = Lists.newArrayList();
        for (BasicProject jiraProject : jiraClient.findProjects()) {
            for (com.atlassian.jira.rest.client.api.domain.User user : jiraClient.findUsers(Lists
                    .newArrayList(jiraProject))) {
                Assignment assignment = new Assignment();
                assignment.setUserName(user.getName());
                assignment.setProjectName(jiraProject.getName());
                assignment.generateUuid();
                assignments.add(assignment);
            }
        }

        return assignments;
    }

}
