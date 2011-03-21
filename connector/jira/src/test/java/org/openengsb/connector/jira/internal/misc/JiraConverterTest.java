/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        assertThat(JiraValueConverter.convert(Issue.Status.UNASSIGNED),
            is(StatusConverter.fromIssueStatus(Issue.Status.UNASSIGNED)));
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

    @Test
    public void testConvertFromTypeCodeToTypeAsName() {
        assertThat(TypeConverter.fromCode("1"), is("Bug"));
        assertThat(TypeConverter.fromCode("2"), is("New Feature"));
        assertThat(TypeConverter.fromCode("3"), is("Task"));
        assertThat(TypeConverter.fromCode("4"), is("Improvement"));
    }
}
