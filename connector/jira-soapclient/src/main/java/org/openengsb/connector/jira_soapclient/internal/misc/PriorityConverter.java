package org.openengsb.connector.jira_soapclient.internal.misc;

import org.openengsb.domain.issue.models.Issue;

/**
 * priority converter from OpenEngSB priority to Jira priority,
 * see http://docs.atlassian.com/jira/latest/constant-values.html
 */
public class PriorityConverter {

    public static String fromIssuePriority(Issue.Priority priority) {
        switch (priority) {
            case IMMEDIATE:
                //Blocker
                return "1";
            case HIGH:
                //Critical
                return "2";
            case URGEND:
                //Major
                return "3";
            case NONE:
                //Minor
                return "4";
            case LOW:
                //Trivial
                return "5";
            default:
                return "4";
        }
    }
}
