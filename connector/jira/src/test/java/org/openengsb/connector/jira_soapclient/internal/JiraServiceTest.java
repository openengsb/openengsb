package org.openengsb.connector.jira_soapclient.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.client.RemoteComment;
import com.atlassian.jira.rpc.soap.client.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;

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
    public void testUpdateIssue() throws Exception {

        RemoteIssue remoteIssue = mock(RemoteIssue.class);
        when(remoteIssue.getKey()).thenReturn("issueKey");
        when(jiraSoapService.getIssue(authToken, "id1")).thenReturn(remoteIssue);
        HashMap<IssueAttribute, String> changes = new HashMap<IssueAttribute, String>();

        jiraClient.updateIssue("id1", "comment1", changes);
        verify(jiraSoapService, times(1)).updateIssue(anyString(), anyString(), any(RemoteFieldValue[].class));

    }

    @Test
    public void testDelayIssue() throws Exception {
        RemoteVersion[] versions = new RemoteVersion[1];
        RemoteVersion version = mock(RemoteVersion.class);
        when(version.getId()).thenReturn("id2");
        versions[0] = version;
        when(jiraSoapService.getVersions(anyString(), anyString())).thenReturn(versions);
        RemoteIssue[] values = new RemoteIssue[1];
        RemoteIssue issue = mock(RemoteIssue.class);
        values[0] = issue;
        when(jiraSoapService.getIssuesFromJqlSearch(authToken, "fixVersion in (\"Test Version 1\") ", 300))
            .thenReturn(values);
        jiraClient.moveIssuesFromReleaseToRelease("id1", "id2");
        verify(jiraSoapService, atLeastOnce()).updateIssue(anyString(), anyString(), any(RemoteFieldValue[].class));
    }

    @Test
    public void testCloseRelease() throws java.rmi.RemoteException, RemoteAuthenticationException {
        RemoteVersion[] versions = new RemoteVersion[1];
        RemoteVersion version = mock(RemoteVersion.class);
        when(version.getName()).thenReturn("versionName");
        versions[0] = version;
        when(jiraSoapService.getVersions(anyString(), anyString())).thenReturn(versions);

        jiraClient.closeRelease("versionName");
        verify(jiraSoapService).releaseVersion(authToken, "projectKey", version);
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
