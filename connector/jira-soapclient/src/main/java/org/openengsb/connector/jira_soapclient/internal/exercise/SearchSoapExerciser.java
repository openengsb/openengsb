package org.openengsb.connector.jira_soapclient.internal.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import org.openengsb.connector.jira_soapclient.internal.SOAPSession;


/**
 * @since v4.0
 */
public class SearchSoapExerciser extends AbstractSoapExerciser
{
    public SearchSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public RemoteIssue[] testJqlSearch(final String jql) throws Exception
    {
        return testJqlSearch(jql, 10);
    }

    public RemoteIssue[] testJqlSearch(final String jql, final int limit) throws Exception
    {
        return getJiraSoapService().getIssuesFromJqlSearch(getToken(), jql, limit);
    }

    public RemoteIssue[] testGetIssuesFromTextSearch(final String searchTerms) throws Exception
    {
        return getJiraSoapService().getIssuesFromTextSearch(getToken(), searchTerms);
    }

    public RemoteIssue[] testGetIssuesFromTextSearchWithProject(String[] projectKeys, String searchTerms, int maxNumResults) throws Exception
    {
        return getJiraSoapService().getIssuesFromTextSearchWithProject(getToken(), projectKeys, searchTerms, maxNumResults);
    }
}
