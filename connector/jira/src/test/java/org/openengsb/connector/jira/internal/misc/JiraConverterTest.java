package org.openengsb.connector.jira.internal.misc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.openengsb.domain.issue.models.Issue;

/**
 *
 */
public class JiraConverterTest {

    @Test
    public void testConvertAllPriorities() {
        assertThat(JiraValueConverter.convert(Issue.Priority.HIGH),
            is(PriorityConverter.fromIssuePriority(Issue.Priority.HIGH)));
        assertThat(JiraValueConverter.convert(Issue.Priority.IMMEDIATE),
            is(PriorityConverter.fromIssuePriority(Issue.Priority.IMMEDIATE)));
        assertThat(JiraValueConverter.convert(Issue.Priority.LOW),
            is(PriorityConverter.fromIssuePriority(Issue.Priority.LOW)));
        assertThat(JiraValueConverter.convert(Issue.Priority.NONE),
            is(PriorityConverter.fromIssuePriority(Issue.Priority.NONE)));
        assertThat(JiraValueConverter.convert(Issue.Priority.NORMAL),
            is(PriorityConverter.fromIssuePriority(Issue.Priority.NORMAL)));
        assertThat(JiraValueConverter.convert(Issue.Priority.URGEND),
            is(PriorityConverter.fromIssuePriority(Issue.Priority.URGEND)));
    }

    @Test
    public void testConvertAllStates() {
        assertThat(JiraValueConverter.convert(Issue.Status.ASSIGNED),
            is(StatusConverter.fromIssueStatus(Issue.Status.ASSIGNED)));
        assertThat(JiraValueConverter.convert(Issue.Status.CLOSED),
            is(StatusConverter.fromIssueStatus(Issue.Status.CLOSED)));
        assertThat(JiraValueConverter.convert(Issue.Status.NEW), is(StatusConverter.fromIssueStatus(Issue.Status.NEW)));
    }

    @Test
    public void testConvertAllTypes() {
        assertThat(JiraValueConverter.convert(Issue.Type.BUG), is(TypeConverter.fromIssueType(Issue.Type.BUG)));
        assertThat(JiraValueConverter.convert(Issue.Type.TASK), is(TypeConverter.fromIssueType(Issue.Type.TASK)));
        assertThat(JiraValueConverter.convert(Issue.Type.IMPROVEMENT),
            is(TypeConverter.fromIssueType(Issue.Type.IMPROVEMENT)));
        assertThat(JiraValueConverter.convert(Issue.Type.NEW_FEATURE),
            is(TypeConverter.fromIssueType(Issue.Type.NEW_FEATURE)));
    }

    @Test
    public void testConvertAllFields() {
        assertThat(JiraValueConverter.convert(Issue.Field.TYPE), is(FieldConverter.fromIssueField(Issue.Field.TYPE)));
        assertThat(JiraValueConverter.convert(Issue.Field.DESCRIPTION),
            is(FieldConverter.fromIssueField(Issue.Field.DESCRIPTION)));
        assertThat(JiraValueConverter.convert(Issue.Field.OWNER), is(FieldConverter.fromIssueField(Issue.Field.OWNER)));
        assertThat(JiraValueConverter.convert(Issue.Field.PRIORITY),
            is(FieldConverter.fromIssueField(Issue.Field.PRIORITY)));
        assertThat(JiraValueConverter.convert(Issue.Field.REPORTER),
            is(FieldConverter.fromIssueField(Issue.Field.REPORTER)));
        assertThat(JiraValueConverter.convert(Issue.Field.STATUS),
            is(FieldConverter.fromIssueField(Issue.Field.STATUS)));
        assertThat(JiraValueConverter.convert(Issue.Field.SUMMARY),
            is(FieldConverter.fromIssueField(Issue.Field.SUMMARY)));
    }

    @Test
    public void testConvertFromString() {
        assertThat(JiraValueConverter.convert("type"), is(FieldConverter.fromIssueField(Issue.Field.TYPE)));
        assertThat(JiraValueConverter.convert("Bug"), is(TypeConverter.fromIssueType(Issue.Type.BUG)));
        assertThat(JiraValueConverter.convert("NEW"), is(StatusConverter.fromIssueStatus(Issue.Status.NEW)));
        assertThat(JiraValueConverter.convert("hIgH"), is(PriorityConverter.fromIssuePriority(Issue.Priority.HIGH)));
    }
}
