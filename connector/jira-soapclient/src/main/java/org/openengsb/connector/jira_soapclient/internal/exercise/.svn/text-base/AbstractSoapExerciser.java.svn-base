package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira_soapclient.SOAPSession;

import java.rmi.RemoteException;

/**
 * A base class for SOAP exerciser classes
 * <p/>
 * A good design argument would have all this code in the actual funct tests rather than split
 * into the top level project.  But by putting the code here people can see JIRA SOAP API calls in action
 * even if they cant easily run it without all the JIRA func test environment setup.
 */
abstract class AbstractSoapExerciser
{
    final SOAPSession soapSession;

    public AbstractSoapExerciser(final SOAPSession soapSession)
    {
        this.soapSession = soapSession;
    }

    public void soapConnect() throws RemoteException
    {
        soapConnect(ExerciserClientConstants.LOGIN_NAME, ExerciserClientConstants.LOGIN_PASSWORD);
    }

    public void soapConnect(final String userName, final String password)
            throws RemoteException
    {
        soapSession.connect(userName, password);
    }

    public JiraSoapService getJiraSoapService()
    {
        return soapSession.getJiraSoapService();
    }

    public String getToken()
    {
        return soapSession.getAuthenticationToken();
    }

    public String getTokenForUser(String userName, String password) throws RemoteException
    {
        String currentToken;
        if (userName != null && password != null)
        {
            currentToken = getJiraSoapService().login(userName, password);
        }
        else
        {
            currentToken = getToken();
        }
        return currentToken;
    }

}
