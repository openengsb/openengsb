package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteField;
import com.atlassian.jira.rpc.soap.client.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira_soapclient.SOAPSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * This class exercises the <b>Issue</b> functions of the JIRA SOAP API
 */
public class IssueSoapExerciser extends AbstractSoapExerciser
{
    public IssueSoapExerciser(SOAPSession soapSession)
    {
        super(soapSession);
    }

    public RemoteCustomFieldValue[] testGetCustomFieldValues(String issueKey) throws RemoteException
    {
        System.out.println("Testing getting Custom Field values for Issue " + issueKey);
        RemoteField[] remoteFields = getJiraSoapService().getCustomFields(getToken());

        // Get an issue with CustomField Values set.
        RemoteIssue issue = getJiraSoapService().getIssue(getToken(), issueKey);
        RemoteCustomFieldValue[] customFieldValues = issue.getCustomFieldValues();
        for (int i = 0; i < customFieldValues.length; i++)
        {
            System.out.println("Custom Field Id: " + customFieldValues[i].getCustomfieldId());
            String[] values = customFieldValues[i].getValues();
            for (int j = 0; j < values.length; j++)
            {
                System.out.println("Custom Field Value: " + values[j]);
            }
        }
        System.out.println("Ending getting Custom Field values for Issue " + issueKey);
        return customFieldValues;
    }

    public RemoteIssue testGetIssueById(String issueId) throws RemoteException
    {
        System.out.println("Testing getIssueById ...");
        RemoteIssue issue = getJiraSoapService().getIssueById(getToken(), issueId);
        System.out.println("Returned an issue id: " + issue.getId() + " key: " + issue.getKey());
        return issue;
    }

    public RemoteIssue[] testFindIssuesWithTerm(String term) throws java.rmi.RemoteException
    {
        long startTime = System.currentTimeMillis();
        RemoteIssue[] issuesFromTextSearch = getJiraSoapService().getIssuesFromTextSearch(getToken(), term);
        System.out.println(issuesFromTextSearch.length + " issues with term \"" + term + "\"");
        for (int i = 0; i < issuesFromTextSearch.length; i++)
        {
            RemoteIssue remoteIssue = issuesFromTextSearch[i];
            System.out.println("\t" + remoteIssue.getKey() + " " + remoteIssue.getSummary());
        }
        System.out.println("Time taken for search: " + (System.currentTimeMillis() - startTime) + "ms");
        return issuesFromTextSearch;
    }

    public void testUpdateIssue(final String issueKey, final String custom_field_key_1, final String custom_field_value_1, final String custom_field_key_2, final String custom_field_value_2)
            throws java.rmi.RemoteException
    {
        testGetFieldsForEdit(issueKey);

        // Update the issue
        RemoteFieldValue[] actionParams = new RemoteFieldValue[] {
                new RemoteFieldValue("summary", new String[] { ExerciserClientConstants.NEW_SUMMARY }),
                new RemoteFieldValue(custom_field_key_1, new String[] { custom_field_value_1 }),
                new RemoteFieldValue(custom_field_key_2, new String[] { custom_field_value_2 }) };

        getJiraSoapService().updateIssue(getToken(), issueKey, actionParams);
    }

    public void testGetFieldsForEdit(final String issueKey)
            throws java.rmi.RemoteException
    {
        // Editing the issue & getting the fields available on edit
        System.out.println("The issue " + issueKey + " has the following editable fields:");
        final RemoteField[] fieldsForEdit = getJiraSoapService().getFieldsForEdit(getToken(), issueKey);
        for (int i = 0; i < fieldsForEdit.length; i++)
        {
            RemoteField remoteField = fieldsForEdit[i];
            System.out.println("\tremoteField: " + remoteField.getId());
        }
    }

    public RemoteIssue testCreateIssue(RemoteIssue issue)
            throws java.rmi.RemoteException
    {
        // Create the issue

        // Run the create issue code
        RemoteIssue returnedIssue = getJiraSoapService().createIssue(getToken(), issue);
        final String issueKey = returnedIssue.getKey();

        System.out.println("Successfully created issue " + issueKey);
        printIssueDetails(returnedIssue);

        return returnedIssue;
    }

    public Calendar testGetResolutionDate(String issueKey) throws RemoteException
    {
        return getJiraSoapService().getResolutionDateByKey(getToken(),  issueKey);
    }

    public Calendar testGetResolutionDate(Long issueId) throws RemoteException
    {
        return getJiraSoapService().getResolutionDateById(getToken(),  issueId);
    }

    public RemoteIssue testCreateIssueWithSecurity(RemoteIssue issue, long securityLevelId)
            throws java.rmi.RemoteException
    {
        RemoteIssue returnedIssue = getJiraSoapService().createIssueWithSecurityLevel(getToken(), issue, securityLevelId);
        final String issueKey = returnedIssue.getKey();

        System.out.println("Successfully created issue " + issueKey);
        printIssueDetails(returnedIssue);

        return returnedIssue;
    }

    private void printIssueDetails(RemoteIssue issue)
    {
        System.out.println("Issue Details");
        Method[] declaredMethods = issue.getClass().getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++)
        {
            Method declaredMethod = declaredMethods[i];
            if (declaredMethod.getName().startsWith("get") && declaredMethod.getParameterTypes().length == 0)
            {
                System.out.print("Issue." + declaredMethod.getName() + "() -> ");
                try
                {
                    Object o = declaredMethod.invoke(issue, new Object[] { });
                    if (o instanceof Object[])
                    {
                        System.out.println(printArray((Object[]) o));
                    }
                    else
                    {
                        System.out.println(o);
                    }
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private String printArray(Object[] o)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < o.length; i++)
        {
            sb.append(o[i]).append(" ");
        }
        return sb.toString();
    }

}

