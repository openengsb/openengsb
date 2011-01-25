/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.jira.internal;

import java.util.Date;
import java.util.HashMap;

import org.junit.Test;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraProxyFactory;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraRpcConverter;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.Issue.Priority;
import org.openengsb.domain.issue.models.Issue.Status;
import org.openengsb.domain.issue.models.IssueAttribute;

/**
 * User test, testing the basic Jira connector service funtionality
 * <p>
 * Prerequisites:
 * <ul>
 * <li>A default local Jira installation in {@value JIRA_URI},
 * <li>a project with key {@value JIRA_PROJECT} (CAUTION: the test will create issues there),
 * <li>a user {@value JIRA_USER} with password {@value JIRA_PASSWORD} and developer rights in the project
 * <li>and a second user {@value JIRA_2ND_USER} with developer rights in the project
 * </ul>
 */
public class JiraServiceUT {
    
    /**
     * The URI of the Jira instance
     */
    public static final String JIRA_URI = "http://localhost:8080";
    
    /**
     * The user used for RPCs (has to have developer rights)
     */
    public static final String JIRA_USER = "admin";
    
    /**
     * The password for the user specified in {@link JiraServiceUT#JIRA_USER}
     */
    public static final String JIRA_PASSWORD = "admin";
    
    /**
     * A second user with developer rights
     */
    public static final String JIRA_2ND_USER = "jirauser";
    
    /**
     * The project key of project used for the test
     */
    public static final String JIRA_PROJECT = "PP";

    @Test
    public void testBasicServiceFuntionality() throws Exception {
        JiraProxyFactory proxyFactory = new JiraProxyFactory(JIRA_URI);
        JiraRpcConverter rpcConverter = new JiraRpcConverter(JIRA_PROJECT);
        JiraService issueSystem = new JiraService("someId", proxyFactory, rpcConverter);
        issueSystem.setJiraUser(JIRA_USER);
        issueSystem.setJiraPassword(JIRA_PASSWORD);

        Issue issue = new Issue();
        issue.setDescription(timestamped("issue.description"));
        issue.setOwner(JIRA_2ND_USER);
        issue.setReporter(JIRA_USER);
        issue.setPriority(Priority.IMMEDIATE);
        issue.setStatus(Status.NEW);
        issue.setSummary(timestamped("issue.summary"));
        issue.setId(issueSystem.createIssue(issue)); 

        issueSystem.addComment(issue.getId(), timestamped("New comment on"));

        HashMap<IssueAttribute, String> changes = new HashMap<IssueAttribute, String>();
        changes.put(Issue.Field.OWNER, JIRA_USER);
        issueSystem.updateIssue(issue.getId(), timestamped("comment for issue update"), changes);
    }

    private static String timestamped(String message) {
        return message + " " + new Date();
    }
}
