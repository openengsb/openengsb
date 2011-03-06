package com.atlassian.jira_soapclient.misc;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemoteWorklog;

import java.util.Calendar;
import java.util.Date;

/**
 * This little test class, is a one off to test loading JIRA with lots of worklog data via RPC
 */
public class LotsOfWorklogs
{

    static JiraSoapServiceService jiraSoapServiceGetter;
    static JiraSoapService soap;
    static String token;

    public static final String FILTER_ID = "14820";

    public static void main(String[] args) throws Exception
    {

        jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
        soap = jiraSoapServiceGetter.getJirasoapserviceV2();
        token = soap.login("testguy", "foobar");

        printOutWorklogsForFilter(FILTER_ID);

        // create a bunch of worklogs for a set of Issues Keys and then retrive them
        String[] issues = new String[] { "TST-8019", "TST-8688", "TST-4629" };

        createWorkLogsForIssues(issues);

        printWorklogsForIssues(issues);

        deleteCreatedWorklogsForIssues(issues);
    }

    private static void deleteCreatedWorklogsForIssues(String[] issues) throws java.rmi.RemoteException
    {
        for (int i = 0; i < issues.length; i++)
        {
            String issueKey = issues[i];
            RemoteWorklog[] remoteWorklogs = soap.getWorklogs(token, issueKey);
            for (int j = 0; j < remoteWorklogs.length; j++)
            {
                RemoteWorklog worklog = remoteWorklogs[j];
                if (worklog.getComment().indexOf("SOAP") != -1)
                {
                    soap.deleteWorklogAndAutoAdjustRemainingEstimate(token, worklog.getId());
                }
            }
        }
    }

    private static void createWorkLogsForIssues(String[] issues) throws java.rmi.RemoteException
    {
        Date startCreate = new Date();
        Date then = new Date();
        long count = 0;
        for (int i = 0; i < issues.length; i++)
        {
            String issueKey = issues[i];
            for (int j = 0; j < 100; j++)
            {
                Date now = new Date();
                RemoteWorklog remoteWorklog = new RemoteWorklog();
                remoteWorklog.setStartDate(Calendar.getInstance());

                long timeSpent = Math.max(now.getTime() - then.getTime(), 1L);
                remoteWorklog.setComment("Created as part of SOAP testing - spent " + timeSpent + " at  " + now);

                remoteWorklog.setTimeSpent(String.valueOf(timeSpent));
                if (j == 0)
                {
                    soap.addWorklogWithNewRemainingEstimate(token, issueKey, remoteWorklog, "1w");
                }
                else
                {
                    soap.addWorklogAndAutoAdjustRemainingEstimate(token, issueKey, remoteWorklog);
                }
                then = now;
                count++;
            }
        }
        long ms = (new Date().getTime() - startCreate.getTime());
        long perSec = (ms * 1000L) / count;
        System.out.println("Created " + count + "worklogs in  " + ms + " ms or " + perSec + " per second");
    }

    private static void printWorklogsForIssues(String[] issues) throws java.rmi.RemoteException
    {
        for (int i = 0; i < issues.length; i++)
        {
            String issueKey = issues[i];
            RemoteWorklog[] remoteWorklogs = soap.getWorklogs(token, issueKey);
            for (int j = 0; j < remoteWorklogs.length; j++)
            {
                RemoteWorklog worklog = remoteWorklogs[j];
                System.out.println(issueKey + "," + remoteWorklogs.length + "," + worklog.getId() + "," + worklog.getAuthor() + "," + worklog.getTimeSpent() + "," + worklog.getStartDate().getTime() + "," + worklog.getComment());
            }
        }
    }

    private static void printOutWorklogsForFilter(String filter)
            throws java.rmi.RemoteException
    {
        RemoteIssue[] issues = soap.getIssuesFromFilter(token, filter);
        for (int i = 0; i < issues.length; i++)
        {
            RemoteIssue issue = issues[i];
            RemoteWorklog[] remoteWorklogs = soap.getWorklogs(token, issue.getKey());
            for (int j = 0; j < remoteWorklogs.length; j++)
            {
                RemoteWorklog worklog = remoteWorklogs[j];
                System.out.println(issue.getKey() + "," + remoteWorklogs.length + "," + worklog.getId() + "," + worklog.getAuthor() + "," + worklog.getTimeSpent() + "," + worklog.getStartDate().getTime() + "," + worklog.getComment());
            }
        }
    }


}
