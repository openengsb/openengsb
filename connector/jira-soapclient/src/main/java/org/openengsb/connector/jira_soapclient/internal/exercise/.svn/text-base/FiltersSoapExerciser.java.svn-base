package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteFilter;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira_soapclient.SOAPSession;

import java.rmi.RemoteException;

/**
 * This class exercises the <b>Fitlers</b> functions of the JIRA SOAP API
 */
public class FiltersSoapExerciser extends AbstractSoapExerciser
{
    public FiltersSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public long testGetIssueCountForFilter(String filterId)
            throws RemoteException
    {
        System.out.println("Testing getIssueCountForFilter ...");
        long issueCount = getJiraSoapService().getIssueCountForFilter(getToken(), filterId);
        System.out.println("Returned an issue count of " + issueCount + " for filter " + filterId);
        return issueCount;
    }

    public RemoteFilter[] testGetFilters() throws java.rmi.RemoteException
    {
        System.out.println("Favourite filters:");
        RemoteFilter[] savedFilters = getJiraSoapService().getFavouriteFilters(getToken());
        for (int i = 0; i < savedFilters.length; i++)
        {
            RemoteFilter filter = savedFilters[i];
            String description = filter.getDescription() != null ? (": " + filter.getDescription()) : "";
            System.out.println("\t" + filter.getName() + description);
        }
        return savedFilters;
    }

    public RemoteIssue[] testGetIssuesFromTextSearch(String text) throws java.rmi.RemoteException
    {
        RemoteIssue[] issues = getJiraSoapService().getIssuesFromTextSearch(getToken(), text);
        for (int i = 0; i < issues.length; i++)
        {
            RemoteIssue issue = issues[i];
            System.out.println("issue.getSummary(): " + issue.getSummary());
        }
        return issues;
    }

    public RemoteIssue[] testGetIssuesFromTextSearchWithLimit(String text, int offSet, int maxResults) throws java.rmi.RemoteException
    {
        RemoteIssue[] issues = getJiraSoapService().getIssuesFromTextSearchWithLimit(getToken(), text, offSet, maxResults);
        for (int i = 0; i < issues.length; i++)
        {
            RemoteIssue issue = issues[i];
            System.out.println("issue.getSummary(): " + issue.getSummary());
        }
        return issues;
    }

    public RemoteIssue[] testGetIssuesFromFilter(String filterId) throws java.rmi.RemoteException
    {
        RemoteIssue[] issues = getJiraSoapService().getIssuesFromFilter(getToken(), filterId);
        for (int i = 0; i < issues.length; i++)
        {
            RemoteIssue issue = issues[i];
            System.out.println("issue.getSummary(): " + issue.getSummary());
        }
        return issues;
    }

    public RemoteIssue[] testGetIssuesFromFilterWithLimit(String filterId, int offSet, int maxResults) throws java.rmi.RemoteException
    {
        RemoteIssue[] issues = getJiraSoapService().getIssuesFromFilterWithLimit(getToken(), filterId, offSet, maxResults);
        for (int i = 0; i < issues.length; i++)
        {
            RemoteIssue issue = issues[i];
            System.out.println("issue.getSummary(): " + issue.getSummary());
        }
        return issues;
    }

    public RemoteIssue[] testGetIssuesFromTextSearchWithProjectWithMax(String [] projectKeys, String text, int maxResults) throws java.rmi.RemoteException
    {
        RemoteIssue[] issues = getJiraSoapService().getIssuesFromTextSearchWithProject(getToken(), projectKeys, text, maxResults);
        for (int i = 0; i < issues.length; i++)
        {
            RemoteIssue issue = issues[i];
            System.out.println("issue.getSummary(): " + issue.getSummary());
        }
        return issues;
    }

}