package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteField;
import com.atlassian.jira_soapclient.SOAPSession;

/**
 * This class exercises the <b>Custom Fields</b> functions of the JIRA SOAP API
 */
public class CustomFieldsSoapExerciser extends AbstractSoapExerciser
{
    public CustomFieldsSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public RemoteField[] testGetCustomFields() throws java.rmi.RemoteException
    {
        final RemoteField[] customFields = getJiraSoapService().getCustomFields(getToken());
        for (int i = 0; i < customFields.length; i++)
        {
            RemoteField customField = customFields[i];
            System.out.println("customField.getName(): " + customField.getName());
        }
        return customFields;
    }

}