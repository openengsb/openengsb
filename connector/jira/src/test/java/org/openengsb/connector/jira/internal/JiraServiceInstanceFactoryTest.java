package org.openengsb.connector.jira.internal;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

/**
 *
 */
public class JiraServiceInstanceFactoryTest {

    @Test
    public void testUpdateServiceInstance() throws Exception {
        JiraServiceInstanceFactory jsif = new JiraServiceInstanceFactory();
        Map<String, String> attributes = new HashMap<String, String>();
        JiraService service = jsif.createServiceInstance("id", attributes);
        assertThat(service.getInstanceId(), is("id"));
    }

    @Test
    public void testUpdateValidation() throws Exception {
        JiraServiceInstanceFactory jsif = new JiraServiceInstanceFactory();
        Map<String, String> attributes = new HashMap<String, String>();
        JiraSOAPSession sessionMock = mock(JiraSOAPSession.class);
        attributes.put("jira.project", "projectKey");
        attributes.put("jira.user", "user");
        attributes.put("jira.password", "pwd");
        attributes.put("jira.uri", "uri");
        attributes.put("jira.project", "projectKey");
        JiraService jiraService = new JiraService("id", sessionMock, "projectKeyOld");
        jsif.updateServiceInstance(jiraService, attributes);
        assertThat(jiraService.getProjectKey(), is("projectKey"));
        assertThat(jiraService.getJiraPassword(), is("pwd"));
        assertThat(jiraService.getJiraUser(), is("user"));
        verify(sessionMock).setJiraURI("uri");
    }
}
