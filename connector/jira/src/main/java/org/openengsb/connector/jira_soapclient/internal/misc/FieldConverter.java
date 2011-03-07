package org.openengsb.connector.jira_soapclient.internal.misc;

import org.openengsb.domain.issue.models.Issue;

/**
 * field converter from OpenEngSB field to Jira field,
 * see http://docs.atlassian.com/jira/latest/constant-values.html
 */
public class FieldConverter {

    public static String fromIssueField(Issue.Field issueField) {
        switch (issueField) {
            case SUMMARY:
                return "summary";
            case DESCRIPTION:
                return "description";
            case OWNER:
                return "assignee";
            case REPORTER:
                return "reporter";
            case PRIORITY:
                return "priority";
            case STATUS:
                return "status";
            default:
                return null;
        }
    }
}
