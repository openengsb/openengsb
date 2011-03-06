package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.*;
import com.atlassian.jira_soapclient.SOAPSession;

import java.rmi.RemoteException;

/**
 * This class exercises the <b>JIRA Administration</b> functions of the JIRA SOAP API
 */
public class JiraAdminSoapExerciser extends AbstractSoapExerciser
{
    public JiraAdminSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public RemoteServerInfo testGetRemoteServerInfo() throws RemoteException
    {
        System.out.println("Testing getRemoteServerInfo ...");
        RemoteServerInfo serverInfo = getJiraSoapService().getServerInfo(getToken());
        System.out.println("Returned Server Info:");
        System.out.println("Current server timezone: " + serverInfo.getServerTime().getTimeZoneId());
        System.out.println("Current server time: " + serverInfo.getServerTime().getServerTime());
        return serverInfo;
    }

    public RemoteConfiguration testGetRemoteConfiguration() throws RemoteException
    {
        System.out.println("Testing getRemoteConfiguration ...");
        RemoteConfiguration config = getJiraSoapService().getConfiguration(getToken());
        System.out.println("Returned Configuration: \nAllow Attachments: " + config.isAllowAttachments() + "\nAllow Issue Linking: " + config.isAllowIssueLinking());
        return config;
    }

    public RemotePermission[] testGetAllPermissions() throws java.rmi.RemoteException
    {
        RemotePermission[] allPermissions = getJiraSoapService().getAllPermissions(getToken());
        for (int i = 0; i < allPermissions.length; i++)
        {
            RemotePermission allPermission = allPermissions[i];
            System.out.println("allPermission.getName(): " + allPermission.getName());
        }
        return allPermissions;
    }

    public RemoteIssueType[] getIssueTypes(String userName, String password) throws RemoteException
    {
        String currentToken = getTokenForUser(userName, password);
        return getJiraSoapService().getIssueTypes(currentToken);
    }

    public RemoteIssueType[] getSubTaskIssueTypes(String userName, String password) throws RemoteException
    {
        String currentToken = getTokenForUser(userName, password);
        return getJiraSoapService().getSubTaskIssueTypes(currentToken);
    }

    public RemotePriority[] getPriorities(String userName, String password) throws RemoteException
    {
        String currentToken = getTokenForUser(userName, password);
        return getJiraSoapService().getPriorities(currentToken);
    }

    public RemoteStatus[] getStatuses(String userName, String password) throws RemoteException
    {
        String currentToken = getTokenForUser(userName, password);
        return getJiraSoapService().getStatuses(currentToken);
    }

    public RemoteResolution[] getResolutions(String userName, String password) throws RemoteException
    {
        String currentToken = getTokenForUser(userName, password);
        return getJiraSoapService().getResolutions(currentToken);
    }

}