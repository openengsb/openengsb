/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.connector.jira.internal.models.constants;

import org.openengsb.domain.issue.models.Issue.Field;

public enum JiraIssueField {

    SUMMARY, DESCRIPTION, ASSIGNEE, REPORTER, PROJECT, PRIORITY, STATUS, TYPE;

    public String toString() {
        return this.name().toLowerCase();
    }

    public static JiraIssueField fromIssueField(Field issueField) {
        switch (issueField) {
            case SUMMARY:
                return JiraIssueField.SUMMARY;
            case DESCRIPTION:
                return JiraIssueField.DESCRIPTION;
            case OWNER:
                return JiraIssueField.ASSIGNEE;
            case REPORTER:
                return JiraIssueField.REPORTER;
            case PRIORITY:
                return JiraIssueField.PRIORITY;
            case STATUS:
                return JiraIssueField.STATUS;
            default:
                return null;
        }
    }

}
