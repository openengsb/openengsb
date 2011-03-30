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

package org.openengsb.connector.jira.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.DomainMethodExecutionException;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;

import com.dolby.jira.net.soap.jira.JiraSoapService;
import com.dolby.jira.net.soap.jira.RemoteComment;
import com.dolby.jira.net.soap.jira.RemoteFieldValue;
import com.dolby.jira.net.soap.jira.RemoteIssue;
import com.dolby.jira.net.soap.jira.RemoteVersion;

public class JiraServiceTest {

    private JiraService jiraClient;
    private JiraSoapService jiraSoapService;
    private String authToken = "authToken";
    private JiraSOAPSession jiraSoapSession;
    private String projectKey = "projectKey";

    @Before
    public void setUp() throws Exception {
        jiraSoapSession = mock(JiraSOAPSession.class);
        jiraSoapService = mock(JiraSoapService.class);
        when(jiraSoapSession.getJiraSoapService()).thenReturn(jiraSoapService);
        when(jiraSoapSession.getAuthenticationToken()).thenReturn(authToken);
        jiraClient = new JiraService("id", jiraSoapSession, projectKey);
        jiraClient.setJiraPassword("pwd");
        jiraClient.setJiraUser("user");
    }

    @Test(expected = DomainMethodExecutionException.class)
    public void testCreateIssueAndLoginWithWrongUserdata_shouldFail() throws RemoteException {
        Issue issue = createIssue("id1");
        jiraSoapSession = mock(JiraSOAPSession.class);
        jiraSoapService = mock(JiraSoapService.class);
        when(jiraSoapSession.getJiraSoapService()).thenReturn(jiraSoapService);
        when(jiraSoapSession.getAuthenticationToken()).thenReturn(authToken);
        doThrow(new RemoteException()).when(jiraSoapSession).connect(anyString(), anyString());
        jiraClient = new JiraService("id", jiraSoapSession, projectKey);
        jiraClient.setJiraPassword("pwd");
        jiraClient.setJiraUser("user");
        jiraClient.createIssue(issue);
    }

    @Test
    public void testCreateIssue() throws Exception {
        Issue issue = createIssue("id1");
        RemoteIssue remoteIssue = mock(RemoteIssue.class);
        when(remoteIssue.getKey()).thenReturn("key");
        when(remoteIssue.getId()).thenReturn("id1");
        when(jiraSoapService.createIssue(anyString(), any(RemoteIssue.class))).thenReturn(remoteIssue);
        String id = jiraClient.createIssue(issue);
        verify(jiraSoapSession).getJiraSoapService();
        verify(jiraSoapSession).getAuthenticationToken();
        verify(jiraSoapSession).connect("user", "pwd");
        verify(jiraSoapService).createIssue(anyString(), Mockito.any(RemoteIssue.class));
        assertThat(id, is("key"));
    }

    @Test
    public void testAddComment() throws Exception {
        RemoteIssue remoteIssue = mock(RemoteIssue.class);
        when(remoteIssue.getKey()).thenReturn("issueKey");
        when(jiraSoapService.getIssue(authToken, "id")).thenReturn(remoteIssue);
        jiraClient.addComment("id", "comment1");
        verify(jiraSoapSession, atLeastOnce()).getJiraSoapService();
        verify(jiraSoapSession, atLeastOnce()).getAuthenticationToken();
        verify(jiraSoapSession, atLeastOnce()).connect("user", "pwd");
        verify(jiraSoapService, times(1)).addComment(anyString(), anyString(), any(RemoteComment.class));
    }

    @Test
    public void testUpdateIssue_shouldSuccess() throws Exception {

        RemoteIssue remoteIssue = mock(RemoteIssue.class);
        when(remoteIssue.getKey()).thenReturn("issueKey");
        when(jiraSoapService.getIssue(authToken, "id1")).thenReturn(remoteIssue);
        HashMap<IssueAttribute, String> changes = new HashMap<IssueAttribute, String>();

        jiraClient.updateIssue("id1", "comment1", changes);
        verify(jiraSoapService, times(1)).updateIssue(anyString(), anyString(), any(RemoteFieldValue[].class));

    }

    @Test
    public void testMoveIssues() throws Exception {
        RemoteVersion[] versions = new RemoteVersion[1];
        RemoteVersion version = mock(RemoteVersion.class);
        when(version.getId()).thenReturn("id2");
        versions[0] = version;
        when(jiraSoapService.getVersions(anyString(), anyString())).thenReturn(versions);
        RemoteIssue[] values = new RemoteIssue[1];
        RemoteIssue issue = mock(RemoteIssue.class);
        values[0] = issue;
        when(jiraSoapService.getIssuesFromJqlSearch(authToken, "fixVersion in (\"id1\") ", 1000)).thenReturn(values);
        jiraClient.moveIssuesFromReleaseToRelease("id1", "id2");
        verify(jiraSoapService, atLeastOnce()).updateIssue(anyString(), anyString(), any(RemoteFieldValue[].class));
    }

    @Test
    public void testCloseRelease() throws java.rmi.RemoteException {
        RemoteVersion[] versions = new RemoteVersion[1];
        RemoteVersion version = mock(RemoteVersion.class);
        when(version.getName()).thenReturn("versionName");
        versions[0] = version;
        when(jiraSoapService.getVersions(anyString(), anyString())).thenReturn(versions);

        jiraClient.closeRelease("versionName");
        verify(jiraSoapService).releaseVersion(authToken, "projectKey", version);
    }

    @Test
    public void testGenerateReleaseReport() throws java.rmi.RemoteException {
        RemoteIssue[] values = new RemoteIssue[2];
        RemoteIssue issue = mock(RemoteIssue.class);
        when(issue.getKey()).thenReturn("issue1Key");
        when(issue.getDescription()).thenReturn("issue1Description");
        when(issue.getType()).thenReturn("1");
        when(issue.getStatus()).thenReturn("6");
        values[0] = issue;
        RemoteIssue issue2 = mock(RemoteIssue.class);
        when(issue2.getKey()).thenReturn("issue2Key");
        when(issue2.getDescription()).thenReturn("issue2Description");
        when(issue2.getType()).thenReturn("2");
        when(issue2.getStatus()).thenReturn("6");
        values[1] = issue2;

        when(jiraSoapService.getIssuesFromJqlSearch(authToken, "fixVersion in (\"versionName\") and status in (6)",
                1000)).thenReturn(values);
        ArrayList<String> report = jiraClient.generateReleaseReport("versionName");
        ArrayList<String> expectedReport = new ArrayList<String>();

        expectedReport.add("** New Feature\n");
        expectedReport.add("\t * [issue2Key] - issue2Description");
        expectedReport.add("\n");
        expectedReport.add("** Bug\n");
        expectedReport.add("\t * [issue1Key] - issue1Description");
        expectedReport.add("\n");
        assertThat(report.toString(), is(expectedReport.toString()));
    }

    @Test(expected = DomainMethodExecutionException.class)
    public void testFailCommitingIssueCausedByRemoteException_shouldThrowDomainMehtodExecutionException()
        throws Exception {
        RemoteIssue remoteIssue = mock(RemoteIssue.class);
        when(remoteIssue.getKey()).thenReturn("issueKey");
        doThrow(new RemoteException()).when(jiraSoapSession).connect(anyString(), anyString());
        jiraClient.addComment("id", "comment1");
    }

    @Test(expected = DomainMethodExecutionException.class)
    public void testFailUpdateIssueCausedByRemoteException_shouldThrowDomainMehtodExecutionException()
        throws Exception {
        RemoteIssue remoteIssue = mock(RemoteIssue.class);
        doThrow(new RemoteException()).when(jiraSoapService).updateIssue(anyString(), anyString(),
            any(RemoteFieldValue[].class));
        when(jiraSoapService.getIssue(authToken, "id1")).thenReturn(remoteIssue);
        HashMap<IssueAttribute, String> changes = new HashMap<IssueAttribute, String>();
        jiraClient.updateIssue("id1", "comment1", changes);
    }

    @Test(expected = DomainMethodExecutionException.class)
    public void tesGeneratingReleaseReportCausedByNonExistingRelease_shouldThrowDomainMehtodExecutionException()
        throws Exception {
        doThrow(new RemoteException()).when(jiraSoapService).getIssuesFromJqlSearch(authToken,
            "fixVersion in (\"versionName\") and status in (6)",
                1000);
        jiraClient.generateReleaseReport("versionName");
    }

    private Issue createIssue(String id) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setSummary("summary");
        issue.setDescription("description");
        issue.setReporter("reporter");
        issue.setOwner("owner");
        issue.setPriority(Issue.Priority.NONE);
        issue.setStatus(Issue.Status.NEW);
        issue.setDueVersion("versionID1");
        issue.setType(Issue.Type.BUG);

        return issue;
    }
}
