package org.openengsb.connector.jira_soapclient.internal.misc;

import org.openengsb.domain.issue.models.Issue;

/**
 * status converter from OpenEngSB status to Jira status,
 * see http://docs.atlassian.com/jira/latest/constant-values.html
 */
public class StatusConverter {

    public static String fromIssueStatus(Issue.Status status) {
        switch (status) {
            case CLOSED:
                return "6";
            case NEW:
                return "1";
           default:
               return "1";
        }
    }
}
