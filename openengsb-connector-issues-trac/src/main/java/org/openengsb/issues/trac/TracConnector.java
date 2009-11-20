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
import java.util.Hashtable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.openengsb.issues.common.IssueDomain;
import org.openengsb.issues.common.exceptions.IssueDomainException;
import org.openengsb.issues.common.model.Issue;
import org.openengsb.issues.trac.model.Converter;
import org.openengsb.issues.trac.model.TracIssue;
import org.openengsb.issues.trac.services.Ticket;
import org.openengsb.issues.trac.xmlrpc.TrackerDynamicProxy;

public class TracConnector implements IssueDomain {

    private Logger log = Logger.getLogger(TracConnector.class);

    private final Ticket ticket;

    private String url;
    private String username;
    private String password;

    private Converter converter = new Converter();

    public TracConnector(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;

        log.info(String.format("Instantiating TracConnector to %s with user %s ...", url, username));

        TrackerDynamicProxy proxy = createProxyGenerator();

        ticket = (Ticket) proxy.newInstance(Ticket.class);

        log.info("Instantiation done");
    }

    @Override
    public String createIssue(Issue issue) throws IssueDomainException {
        TracIssue tracIssue = converter.convertGenericIssueToSpecificIssue(issue);

        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put("type", tracIssue.getType());
        attributes.put("owner", tracIssue.getOwner());
        attributes.put("reporter", tracIssue.getReporter());
        attributes.put("priority", tracIssue.getPriority());
        attributes.put("version", tracIssue.getVersion());

        String issueId;
        try {
            issueId = ticket.create(tracIssue.getSummary(), tracIssue.getDescription(), attributes).toString();
        } catch (Exception e) {
            throw new IssueDomainException(e.getMessage(), e);
        }

        return issueId;
    }

    @Override
    public void deleteIssue(String arg0) throws IssueDomainException {
        throw new NotImplementedException();
    }

    @Override
    public void updateIssue(Issue arg0) throws IssueDomainException {
        throw new NotImplementedException();
    }

    private TrackerDynamicProxy createProxyGenerator() {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL(this.url));
        } catch (MalformedURLException e) {
            throw new RuntimeException();
        }
        config.setBasicUserName(this.username);
        config.setBasicPassword(this.password);
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        TrackerDynamicProxy proxy = new TrackerDynamicProxy(client);
        return proxy;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}