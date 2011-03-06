package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteField;
import com.atlassian.jira.rpc.soap.client.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemoteNamedObject;
import com.atlassian.jira_soapclient.SOAPSession;

/**
 * This class exercises the <b>Workflow</b> functions of the JIRA SOAP API
 */
public class WorkflowSoapExerciser extends AbstractSoapExerciser
{
    public WorkflowSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public void testProgressWorkflow(String issueKey)
            throws java.rmi.RemoteException
    {
        System.out.println("Progressing workflow of " + issueKey + "...");
        RemoteNamedObject[] availableActions = getJiraSoapService().getAvailableActions(getToken(), issueKey);
        String actionId = null;
        for (int i = 0; i < availableActions.length; i++)
        {
            RemoteNamedObject availableAction = availableActions[i];
            System.out.println("availableAction: " + availableAction.getId() + " - " + availableAction.getName());
            if (actionId == null)
            {
                actionId = availableAction.getId();
            }
        }

        testProgressWorkflow(issueKey, actionId);
    }

    public void testProgressWorkflow(String issueKey, String actionId) throws java.rmi.RemoteException
    {
        if (actionId != null)
        {
            RemoteFieldValue[] actionParams = new RemoteFieldValue[] {
                    new RemoteFieldValue("assignee", new String[] { ExerciserClientConstants.LOGIN_NAME })
            };

            testProgressWorkflow(issueKey, actionId, actionParams);
        }
    }

    public void testProgressWorkflow(String issueKey, String actionId, RemoteFieldValue[] actionParams) throws java.rmi.RemoteException
    {
        if (actionId != null)
        {
            RemoteField[] fieldsForAction = getJiraSoapService().getFieldsForAction(getToken(), issueKey, actionId);
            for (int i = 0; i < fieldsForAction.length; i++)
            {
                RemoteField remoteField = fieldsForAction[i];
                System.out.println("remoteField: " + remoteField.getId() + " - " + remoteField.getName());
            }

            RemoteIssue remoteIssue = getJiraSoapService().progressWorkflowAction(getToken(), issueKey, actionId, actionParams);
            System.out.println("Progressed workflow of " + remoteIssue.getKey() + " to: " + remoteIssue.getStatus());
        }
    }

    // JRA-16112 - test that if you do not provide the resolution on a transition screen that JIRA claims it is required.
    public void testResolveWithNoResolution(String issueKey)
            throws java.rmi.RemoteException
    {
        System.out.println("Attempting to resolve " + issueKey + "...");
        // Don't a resolution
        RemoteFieldValue[] actionParams = new RemoteFieldValue[] {};
        RemoteIssue remoteIssue = getJiraSoapService().progressWorkflowAction(getToken(), issueKey, "5", actionParams);
        System.out.println("Progressed workflow of " + remoteIssue.getKey() + " to: " + remoteIssue.getStatus());
    }

    public void testResolveAsCannotReproduce(String issueKey)
            throws java.rmi.RemoteException
    {
        System.out.println("Resolving " + issueKey + "...");
        // Add a resolution of Cannot Reproduce
        RemoteFieldValue[] actionParams = new RemoteFieldValue[] {
                new RemoteFieldValue("resolution", new String[] { "5" })
        };
        RemoteIssue remoteIssue = getJiraSoapService().progressWorkflowAction(getToken(), issueKey, "5", actionParams);
        System.out.println("Progressed workflow of " + remoteIssue.getKey() + " to: " + remoteIssue.getStatus());
    }
}