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

package org.openengsb.connector.jira.internal.models.xmlrpc;

import java.util.Hashtable;
import java.util.Map;

public class JiraDynamicProxy {

    private String loginToken;
    private XmlRpcService rpcService;

    public JiraDynamicProxy(XmlRpcService rpcService) {
        this.rpcService = rpcService;
    }

    public void logIn(String userName, String password) throws Exception {
        this.loginToken = rpcService.login(userName, password);
    }

    public void logOut() throws Exception {
        rpcService.logout(loginToken);
    }

    public Map<?, ?> createIssue(Hashtable<?, ?> attributes) throws Exception {
        Hashtable<?, ?> issue = rpcService.createIssue(loginToken, attributes);
        return issue;
    }

    public Map<?, ?> updateIssue(String issueKey, Hashtable<?, ?> attributes) throws Exception {
        Hashtable<?, ?> issue = rpcService.updateIssue(loginToken, issueKey, attributes);
        return issue;
    }

    public boolean addComment(String issueKey, String comment) throws Exception {
        return rpcService.addComment(loginToken, issueKey, comment);
    }

}