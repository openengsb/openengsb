package org.openengsb.connector.jira_soapclient.internal.misc;

import org.openengsb.domain.issue.models.Issue;

public class TypeConverter {

    public static String fromIssueType(Issue.Type type) {
        switch (type) {
            case BUG:
                return "1";
            case NEW_FEATURE:
                return "2";
            case TASK:
                return "3";
            case IMPROVEMENT:
                return "4";
            default:
                return "1";

        }
    }
}
