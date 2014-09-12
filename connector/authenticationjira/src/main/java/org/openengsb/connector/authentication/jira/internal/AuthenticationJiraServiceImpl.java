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

package org.openengsb.connector.authentication.jira.internal;

import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

public class AuthenticationJiraServiceImpl extends AbstractOpenEngSBConnectorService implements AuthenticationDomain {

    public AuthenticationJiraServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public Authentication authenticate(String username, Credentials credentials) throws AuthenticationException {
        JiraRestClientFactory restClientFactory = new AsynchronousJiraRestClientFactory();
        String givenPassword = ((Password) credentials).getValue();
        JiraRestClient restClient =
            restClientFactory.createWithBasicHttpAuthentication(ServerConfig.serverUri, username, givenPassword);
        try {
            restClient.getSessionClient().getCurrentSession().claim();
        } catch (Exception e) {
            // statuscode 401: missing user or wrong pass. 403: forbidden (e.g.captcha required)
            throw new AuthenticationException(e);
        }
        Authentication authentication = new Authentication(username);
        return authentication;

    }

    @Override
    public boolean supports(Credentials credentials) {
        return credentials instanceof Password;
    }

}
