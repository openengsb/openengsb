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

package org.openengsb.connector.trac.internal.models;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.openengsb.connector.trac.internal.models.xmlrpc.Ticket;
import org.openengsb.connector.trac.internal.models.xmlrpc.TrackerDynamicProxy;

public class TicketHandlerFactory {

    String serverUrl = "";
    String username = "";
    String userPassword = "";

    public Ticket createTicket() {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

        URL pURL = null;
        try {
            pURL = new URL(serverUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        config.setServerURL(pURL);
        config.setBasicUserName(username);
        config.setBasicPassword(userPassword);

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        TrackerDynamicProxy proxy = new TrackerDynamicProxy(client);
        Ticket t = (Ticket) proxy.newInstance(Ticket.class);
        return t;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
