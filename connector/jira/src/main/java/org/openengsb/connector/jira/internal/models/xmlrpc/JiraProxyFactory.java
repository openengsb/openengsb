/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.jira.internal.models.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

public class JiraProxyFactory {

    private static final String RPC_PATH = "/rpc/xmlrpc";
    private static final String RPC_INSTANCE_NAME = "jira1";

    private String jiraURI;

    public JiraProxyFactory(String jiraURI) {
        this.jiraURI = jiraURI;
    }

    public String getJiraURI() {
        return this.jiraURI;
    }

    public void setJiraURI(String jiraURI) {
        this.jiraURI = jiraURI;
    }

    public JiraDynamicProxy createInstance() throws MalformedURLException {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(jiraURI + RPC_PATH));
        XmlRpcClient rpcClient = new XmlRpcClient();
        rpcClient.setConfig(config);
        ClientFactory factory = new ClientFactory(rpcClient);
        XmlRpcService rpcService = (XmlRpcService) factory.newInstance(this.getClass().getClassLoader(),
                XmlRpcService.class, RPC_INSTANCE_NAME);

        return new JiraDynamicProxy(rpcService);
    }

}
