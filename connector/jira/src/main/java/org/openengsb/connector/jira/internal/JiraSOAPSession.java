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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dolby.jira.net.soap.jira.JiraSoapService;
import com.dolby.jira.net.soap.jira.JiraSoapServiceService;
import com.dolby.jira.net.soap.jira.JiraSoapServiceServiceLocator;



/**
 * This represents a SOAP session with JIRA including that state of being logged in or not
 */
public class JiraSOAPSession {

    private static Log log = LogFactory.getLog(JiraSOAPSession.class);
    private JiraSoapServiceService jiraSoapServiceLocator;
    private JiraSoapService jiraSoapService;
    private String token;
    private String jiraURI;

    public JiraSOAPSession(String jiraURI) {
        this.jiraURI = jiraURI;
    }

    private void setUp(String url) {
        try {
            URL webServicePort = new URL(url);
            jiraSoapServiceLocator = new JiraSoapServiceServiceLocator();
            jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2(webServicePort);
            log.info("SOAP Session service endpoint at " + webServicePort.toExternalForm());
        } catch (ServiceException e) {
            throw new RuntimeException("ServiceException during JiraService contruction", e);
        } catch (MalformedURLException e) {
            log.error("malformed jiraURI");
        }
    }

    public void connect(String userName, String password) throws RemoteException {
        log.info("\tSetup started");
        setUp(jiraURI);
        log.info("\tConnnecting via SOAP as : " + userName);
        token = getJiraSoapService().login(userName, password);
        log.info("\tConnected");
    }

    public String getAuthenticationToken() {
        return token;
    }

    public JiraSoapService getJiraSoapService() {
        return jiraSoapService;
    }

    public JiraSoapServiceService getJiraSoapServiceLocator() {
        return jiraSoapServiceLocator;
    }

    public void setJiraURI(String url) {
        this.jiraURI = url;
    }
}
