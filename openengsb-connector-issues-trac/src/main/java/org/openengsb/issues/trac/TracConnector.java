/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.issues.trac;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.openengsb.issues.trac.model.Ticket;
import org.openengsb.issues.trac.xmlrpc.TrackerDynamicProxy;

public class TracConnector implements IssueDomain {
    private final Ticket ticket;

    public TracConnector(String url, String username, String password) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException();
        }
        config.setBasicUserName(username);
        config.setBasicPassword(password);
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        TrackerDynamicProxy proxy = new TrackerDynamicProxy(client);

        ticket = (Ticket) proxy.newInstance(Ticket.class);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, String> getTicket(int id) {
        Vector<?> v = ticket.get(id);
        return (HashMap<String, String>) v.get(3);
    }

    public void updateTicketStatus(int id, String status, String comment) {
        Hashtable<String, String> h = new Hashtable<String, String>();
        h.put("status", status);
        ticket.update(id, comment, h);
    }

    public String createIssue(String summary, String description, String reporter, String owner, String type,
            String priority) {
        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put("type", type);
        attributes.put("owner", owner);
        attributes.put("reporter", reporter);
        attributes.put("priority", priority);

        return ticket.create(summary, description, attributes).toString();
    }

    @Override
    public String createIssue(Issue issue) {
        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put("type", issue.getType());
        attributes.put("owner", issue.getOwner());
        attributes.put("reporter", issue.getReporter());
        attributes.put("priority", issue.getPriority());

        return ticket.create(issue.getSummary(), issue.getDescription(), attributes).toString();
    }

}