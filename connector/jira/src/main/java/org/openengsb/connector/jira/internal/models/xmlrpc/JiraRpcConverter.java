/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.connector.jira.internal.models.xmlrpc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openengsb.connector.jira.internal.models.constants.JiraIssueField;
import org.openengsb.connector.jira.internal.models.constants.JiraIssuePriority;
import org.openengsb.connector.jira.internal.models.constants.JiraIssueStatus;
import org.openengsb.connector.jira.internal.models.constants.JiraIssueType;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.Issue.Field;
import org.openengsb.domain.issue.models.Issue.Priority;
import org.openengsb.domain.issue.models.Issue.Status;
import org.openengsb.domain.issue.models.IssueAttribute;

public class JiraRpcConverter {

    private String jiraProject;

    public JiraRpcConverter(String jiraProject) {
        this.jiraProject = jiraProject;
    }

    public Hashtable<String, Object> convertChanges(HashMap<IssueAttribute, String> changes) {
        Set<IssueAttribute> changedAttributes = new HashSet<IssueAttribute>(changes.keySet());
        Hashtable<String, Object> jiraChanges = new Hashtable<String, Object>();

        if (changedAttributes.contains(Field.STATUS)) {
            Status status = Issue.Status.valueOf(changes.get(Field.STATUS));
            put(jiraChanges, JiraIssueField.STATUS, JiraIssueStatus.fromStatus(status).toString());
            changedAttributes.remove(Issue.Field.STATUS);
        }

        if (changedAttributes.contains(Field.PRIORITY)) {
            Priority priority = Issue.Priority.valueOf(changes.get(Field.PRIORITY));
            put(jiraChanges, JiraIssueField.PRIORITY, JiraIssuePriority.fromPriority(priority).toString());
            changedAttributes.remove(Issue.Field.PRIORITY);
        }

        for (IssueAttribute attribute : changedAttributes) {
            JiraIssueField targetField = JiraIssueField.fromIssueField((Issue.Field) attribute);
            if (targetField != null) {
                jiraChanges.put(targetField.toString(), Arrays.asList(changes.get(attribute)));
            }
        }

        return jiraChanges;
    }

    public Hashtable<String, Object> convertIssueForCreation(Issue issue) {
        Hashtable<String, Object> struct = new Hashtable<String, Object>();

        put(struct, JiraIssueField.SUMMARY, issue.getSummary());
        put(struct, JiraIssueField.DESCRIPTION, issue.getDescription());
        put(struct, JiraIssueField.ASSIGNEE, issue.getOwner());
        put(struct, JiraIssueField.PRIORITY, JiraIssuePriority.fromPriority(issue.getPriority()).toString());
        put(struct, JiraIssueField.STATUS, JiraIssueStatus.fromStatus(issue.getStatus()).toString());
        put(struct, JiraIssueField.PROJECT, this.jiraProject);
        put(struct, JiraIssueField.TYPE, JiraIssueType.BUG.getId());

        return struct;
    }

    private <T, S> void put(Map<String, S> map, T key, S value) {
        map.put(key.toString(), value);
    }

    public String getJiraProject() {
        return this.jiraProject;
    }

    public void setJiraProject(String jiraProject) {
        this.jiraProject = jiraProject;
    }

}
