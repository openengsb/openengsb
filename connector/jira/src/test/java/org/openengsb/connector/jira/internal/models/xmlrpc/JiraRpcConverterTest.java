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

package org.openengsb.connector.jira.internal.models.xmlrpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;

import org.junit.Test;
import org.openengsb.connector.jira.internal.models.constants.JiraIssueField;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.Issue.Priority;
import org.openengsb.domain.issue.models.Issue.Status;

public class JiraRpcConverterTest {

    @Test
    public void projectIsInitializedCorrectly() {
        JiraRpcConverter converter = new JiraRpcConverter("aProject");

        assertEquals("aProject", converter.getJiraProject());
    }

    @Test
    public void projectIsSetCorrectly() {
        JiraRpcConverter converter = new JiraRpcConverter("aProject");

        converter.setJiraProject("anotherProject");

        assertEquals("anotherProject", converter.getJiraProject());
    }

    @Test
    public void issueFields_AreMappedCorrectly() {
        JiraRpcConverter converter = new JiraRpcConverter("aProject");
        Issue testIssue = mock(Issue.class);
        when(testIssue.getSummary()).thenReturn("theSummary");
        when(testIssue.getDescription()).thenReturn("theDescription");
        when(testIssue.getOwner()).thenReturn("anOwner");
        when(testIssue.getPriority()).thenReturn(Priority.URGEND);
        when(testIssue.getStatus()).thenReturn(Status.ASSIGNED);

        Hashtable<String, Object> convertedIssueTable = converter.convertIssueForCreation(testIssue);

        assertEquals("theSummary", convertedIssueTable.get(JiraIssueField.SUMMARY.toString()));
        assertEquals("theDescription", convertedIssueTable.get(JiraIssueField.DESCRIPTION.toString()));
        assertEquals("aProject", convertedIssueTable.get(JiraIssueField.PROJECT.toString()));
        assertEquals("1", convertedIssueTable.get(JiraIssueField.TYPE.toString()));
        assertEquals("anOwner", convertedIssueTable.get(JiraIssueField.ASSIGNEE.toString()));
    }

}
