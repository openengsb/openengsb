/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.connector.jira.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraDynamicProxy;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraProxyFactory;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraRpcConverter;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.domain.issue.IssueDomain;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;

public class JiraService extends AbstractOpenEngSBService implements IssueDomain {

    private static Log log = LogFactory.getLog(JiraService.class);

    private AliveState state = AliveState.DISCONNECTED;
    private String jiraUser;
    private String jiraPassword;
    private JiraProxyFactory proxyFactory;
    private JiraRpcConverter rpcConverter;

    public JiraService(String id, JiraProxyFactory proxyFactory, JiraRpcConverter rpcConverter) {
        super(id);
        this.proxyFactory = proxyFactory;
        this.rpcConverter = rpcConverter;
    }

    @Override
    public String createIssue(Issue issue) {
        Hashtable<String, Object> rpcIssue = rpcConverter.convertIssueForCreation(issue);

        Map<?, ?> issueMap;
        String key = "";
        try {
            JiraDynamicProxy client = createConnectedJiraProxy();
            issueMap = client.createIssue(rpcIssue);
            key = (String) issueMap.get("key");
            log.info("Successfully created issue " + issue.getSummary() + ", ID is: " + key + ".");
            client.logOut();
        } catch (Exception e) {
            log.error("Error creating issue " + issue.getSummary() + ". XMLRPC call failed.");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
        return key;
    }

    @Override
    public void addComment(String id, String comment) {
        try {
            JiraDynamicProxy client = createConnectedJiraProxy();
            client.addComment(id, comment);
            log.info("Successfully added comment to issue " + id + ".");
            client.logOut();
        } catch (Exception e) {
            log.error("Error adding comment to issue " + id + ". XMLRPC call failed.");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }

    @Override
    public void updateIssue(String id, String comment, HashMap<IssueAttribute, String> changes) {
        Hashtable<String, Object> rpcChanges = this.rpcConverter.convertChanges(changes);

        try {
            JiraDynamicProxy client = createConnectedJiraProxy();
            client.updateIssue(id, rpcChanges);
            log.info("Successfully updated issue " + id + " with " + changes.size() + " changes.");
            if (comment != null) {
                client.addComment(id, comment);
            }
            client.logOut();
        } catch (Exception e) {
            log.error("Error updating issue " + id + ". XMLRPC call failed.");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }

    @Override
    public void delayIssue(String id) {

    }

    @Override
    public void closeRelease(String id) {

    }

    @Override
    public AliveState getAliveState() {
        return state;
    }

    public JiraRpcConverter getRpcConverter() {
        return this.rpcConverter;
    }

    public JiraProxyFactory getProxyFactory() {
        return this.proxyFactory;
    }

    public String getJiraUser() {
        return this.jiraUser;
    }

    public void setJiraUser(String jiraUser) {
        this.jiraUser = jiraUser;
    }

    public void setJiraPassword(String jiraPassword) {
        this.jiraPassword = jiraPassword;
    }

    private JiraDynamicProxy createConnectedJiraProxy() throws Exception {
        JiraDynamicProxy client = this.proxyFactory.createInstance();
        client.logIn(jiraUser, jiraPassword);
        this.state = AliveState.ONLINE;
        return client;
    }
}
