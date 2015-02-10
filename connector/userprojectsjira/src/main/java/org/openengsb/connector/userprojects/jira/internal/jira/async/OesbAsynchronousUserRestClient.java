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
package org.openengsb.connector.userprojects.jira.internal.jira.async;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringUtils;
import org.openengsb.connector.userprojects.jira.internal.jira.json.UsersJsonParser;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousUserRestClient;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.collect.Iterables;

public class OesbAsynchronousUserRestClient extends AsynchronousUserRestClient {

    protected static final String USER_URI_PREFIX = "user";
    protected static final String ASSIGNABLE_URI_PREFIX = "assignable";
    protected static final String SEARCH_URI_PREFIX = "search";

    protected static final String PROJECT_KEY_PARAMETER_NAME = "project";

    protected final URI baseUri;

    private final UsersJsonParser usersJsonParser = new UsersJsonParser();

    public OesbAsynchronousUserRestClient(URI baseUri, HttpClient client) {
        super(baseUri, client);
        this.baseUri = baseUri;
    }

    @SuppressWarnings("unchecked")
    public Promise<Iterable<User>> getAssignableUsers(Iterable<BasicProject> projects) {
        List<BasicProject> validProjects = new ArrayList<>();
        if (projects != null) {
            for (BasicProject project : projects) {
                if (project == null) {
                    continue;
                }
                if (StringUtils.isNotBlank(project.getKey())) {
                    validProjects.add(project);
                }
            }
        }
        Set<User> allAssignableUsers = new HashSet<>();
        for (BasicProject project : validProjects) {
            Iterables.addAll(allAssignableUsers, getAssignableUsers(project).claim());
        }
        return Promises.promise(Iterables.concat(allAssignableUsers));
    }

    private Promise<Iterable<User>> getAssignableUsers(BasicProject project) {
        final UriBuilder uriBuilder =
            UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX).path(ASSIGNABLE_URI_PREFIX).path(SEARCH_URI_PREFIX);
        URI uri = uriBuilder.queryParam(PROJECT_KEY_PARAMETER_NAME, project.getKey()).build();
        return getAndParse(uri, usersJsonParser);
    }

}
