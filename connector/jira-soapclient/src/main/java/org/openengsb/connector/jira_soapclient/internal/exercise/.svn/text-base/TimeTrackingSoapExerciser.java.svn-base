package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteWorklog;
import com.atlassian.jira_soapclient.SOAPSession;

import java.rmi.RemoteException;

/**
 * This class exercises the <b>Time Tracking</b> functions of the JIRA SOAP API
 */
public class TimeTrackingSoapExerciser extends AbstractSoapExerciser
{
    public TimeTrackingSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public RemoteWorklog[] testGetWorklogs(String issueKey) throws RemoteException
    {
        return getJiraSoapService().getWorklogs(getToken(), issueKey);
    }

    public RemoteWorklog testAddWorklogWithNewRemainingEstimate(String issueKey, RemoteWorklog remoteWorklog, String newRemainingEstimate)
            throws RemoteException
    {
        return getJiraSoapService().addWorklogWithNewRemainingEstimate(getToken(), issueKey, remoteWorklog, newRemainingEstimate);
    }

    public RemoteWorklog testAddWorklogAndRetainRemainingEstimate(String issueKey, RemoteWorklog remoteWorklog)
            throws RemoteException
    {
        return getJiraSoapService().addWorklogAndRetainRemainingEstimate(getToken(), issueKey, remoteWorklog);
    }

    public RemoteWorklog testAddWorklogAndAutoAdjustRemainingEstimate(String issueKey, RemoteWorklog remoteWorklog)
            throws RemoteException
    {
        return getJiraSoapService().addWorklogAndAutoAdjustRemainingEstimate(getToken(), issueKey, remoteWorklog);
    }
}