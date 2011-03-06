package org.openengsb.connector.jira_soapclient.internal.exercise;

import org.openengsb.connector.jira_soapclient.internal.SOAPSession;

import com.atlassian.jira.rpc.soap.client.RemoteField;

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