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
