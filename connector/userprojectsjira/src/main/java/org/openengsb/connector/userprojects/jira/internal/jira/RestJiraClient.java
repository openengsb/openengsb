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
package org.openengsb.connector.userprojects.jira.internal.jira;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.openengsb.connector.userprojects.jira.internal.jira.async.OesbAsynchronousUserRestClient;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

public class RestJiraClient implements JiraClient {

    private final JiraRestClient jiraRestClient;
    private final OesbAsynchronousUserRestClient userRestClient;

    public RestJiraClient(URI serverUri, String username, String password) {
        final URI baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
        final HttpClient httpClient =
            new AsynchronousHttpClientFactory().createClient(serverUri, new BasicHttpAuthenticationHandler(username,
                    password));
        jiraRestClient = new AsynchronousJiraRestClientFactory().create(serverUri, httpClient);
        userRestClient = new OesbAsynchronousUserRestClient(baseUri, httpClient);
    }

    @Override
    public Iterable<BasicProject> findProjects() {
        return jiraRestClient.getProjectClient().getAllProjects().claim();
    }

    @Override
    public Iterable<User> findUsers(Iterable<BasicProject> projects) {
        return userRestClient.getAssignableUsers(projects).claim();
    }
}
