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

public class JiraServiceUT {
    public static final String JIRA_URI = "http://localhost:8080";
    public static final String RPC_PATH = "/rpc/xmlrpc";
    public static final String JIRA_USER = "admin";
    public static final String JIRA_PASSWORD = "admin";

    @Test
    public void testAll() throws Exception {
        JiraProxyFactory proxyFactory = new JiraProxyFactory(JIRA_URI);
        JiraRpcConverter rpcConverter = new JiraRpcConverter("PP");
        JiraService issueSystem = new JiraService("someId", proxyFactory, rpcConverter);
        issueSystem.setJiraUser(JIRA_USER);
        issueSystem.setJiraPassword(JIRA_PASSWORD);

        Issue issue = new Issue();
        issue.setDescription("issue.description " + new Date());
        issue.setOwner("cka");
        issue.setReporter(JIRA_USER);
        issue.setPriority(Priority.IMMEDIATE);
        issue.setStatus(Status.NEW);
        issue.setSummary("issue.summary " + new Date());
        issue.setId(issueSystem.createIssue(issue));

        issueSystem.addComment(issue.getId(), timestamped("New comment on"));

        HashMap<IssueAttribute, String> changes = new HashMap<IssueAttribute, String>();
        changes.put(Issue.Field.OWNER, "admin");
        //changes.put(Issue.Field.PRIORITY, Priority.URGEND.toString());
        issueSystem.updateIssue(issue.getId(), timestamped("comment for issue update"), changes);

        System.out.println("JiraConnector test app stopped");
    }

    private static String timestamped(String message) {
        return message + " " + new Date();
    }
}
