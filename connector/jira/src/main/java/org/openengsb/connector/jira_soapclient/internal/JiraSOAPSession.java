package org.openengsb.connector.jira_soapclient.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;

/**
 * This represents a SOAP session with JIRA including that state of being logged in or not
 */
public class JiraSOAPSession {

    private static Log log = LogFactory.getLog(JiraSOAPSession.class);
    private JiraSoapServiceService jiraSoapServiceLocator;
    private JiraSoapService jiraSoapService;
    private String token;

    public JiraSOAPSession(String url) {
        setUp(url);
    }

    private void setUp(String url) {
    try {
            URL webServicePort = new URL(url);
            jiraSoapServiceLocator = new JiraSoapServiceServiceLocator();
            if (webServicePort == null) {
                jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2();
            } else {
                jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2(webServicePort);
                log.info("SOAP Session service endpoint at " + webServicePort.toExternalForm());
            }
        } catch (ServiceException e) {
            throw new RuntimeException("ServiceException during JiraService contruction", e);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void connect(String userName, String password) throws RemoteException {
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

    public void updateJiraURI(String url) {
        setUp(url);
    }
}
