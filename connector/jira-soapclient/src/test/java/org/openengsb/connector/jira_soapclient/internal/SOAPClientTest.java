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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.domain.issue.models.Issue;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteComment;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;

public class SOAPClientTest {

    private SOAPClient jiraClient;
    private JiraSoapService jiraSoapService;
    private String authToken = "authToken";
    private SOAPSession soapSession;
    private String projectKey = "projectKey";

    @Before
    public void setUp() throws Exception {
        soapSession = mock(SOAPSession.class);
        jiraSoapService = mock(JiraSoapService.class);
        when(soapSession.getJiraSoapService()).thenReturn(jiraSoapService);
        when(soapSession.getAuthenticationToken()).thenReturn(authToken);
        jiraClient = new SOAPClient("id", soapSession, projectKey);
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
        verify(soapSession).getJiraSoapService();
        verify(soapSession).getAuthenticationToken();
        verify(soapSession).connect("user","pwd");
        verify(jiraSoapService).createIssue(anyString(), Mockito.any(RemoteIssue.class));
        assertThat(id, is("id1"));
    }

    @Test
    public void testAddComment() throws Exception {
        RemoteIssue remoteIssue = mock(RemoteIssue.class);
        when(remoteIssue.getKey()).thenReturn("issueKey");
        when(jiraSoapService.getIssue(authToken, "id")).thenReturn(remoteIssue);
        jiraClient.addComment("id", "comment1");
        verify(soapSession, atLeastOnce()).getJiraSoapService();
        verify(soapSession, atLeastOnce()).getAuthenticationToken();
        verify(soapSession, atLeastOnce()).connect("user", "pwd");
        verify(jiraSoapService, times(1)).addComment(anyString(), anyString(), any(RemoteComment.class));
    }


    @Test
    public void testUpdateIssue() throws Exception {
    }

    @Test
    public void testDelayIssue() throws Exception {
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
