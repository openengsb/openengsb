package org.openengsb.connector.jira_soapclient.internal.misc;

import org.openengsb.domain.issue.models.Issue;

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
