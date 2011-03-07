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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraDynamicProxy;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraProxyFactory;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraRpcConverter;
import org.openengsb.core.common.AliveState;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;

public class JiraServiceTest {

    private JiraDynamicProxy mockProxy;
    private JiraProxyFactory mockProxyFactory;
    private JiraService jiraService;
    private JiraRpcConverter mockConverter;

    @Before
    public void setUp() throws MalformedURLException {
        mockProxy = mock(JiraDynamicProxy.class);
        mockProxyFactory = mock(JiraProxyFactory.class);
        mockConverter = mock(JiraRpcConverter.class);

        when(mockProxyFactory.createInstance()).thenReturn(mockProxy);

        jiraService = new JiraService("anId", mockProxyFactory, mockConverter);
    }

    @Test
    public void connectorIsDisconnectedAfterCreation() {
        assertEquals(AliveState.DISCONNECTED, jiraService.getAliveState());
    }

    @Test
    public void connectorKeepsId() {
        assertEquals("anId", jiraService.getInstanceId());
    }

    @Test
    public void issuesCanBeCreated() throws Exception {
        Issue issue = mock(Issue.class);
        @SuppressWarnings("unchecked")
        Hashtable<String, Object> mockIssueStruct = mock(Hashtable.class);
        when(mockConverter.convertIssueForCreation(issue)).thenReturn(mockIssueStruct);

        jiraService.createIssue(issue);

        verify(mockProxy).createIssue(mockIssueStruct);
    }

    @Test
    public void issuesCanBeUpdated() {
        HashMap<IssueAttribute, String> changes = new HashMap<IssueAttribute, String>();

        jiraService.updateIssue("issueId", "aComment", changes);
    }

    @Test
    public void commentsCanBeCreated() throws Exception {
        jiraService.addComment("issueId", "aComment");

        verify(mockProxy).addComment("issueId", "aComment");
    }

}
