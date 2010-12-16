/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.jira.internal.models.constants;

import org.openengsb.domain.issue.models.Issue.Status;

/**
 * 
 * The statuses of a default Jira installation
 * 
 */
public enum JiraIssueStatus {
    OPEN("1"), IN_PROGRESS("2"), REOPENED("3"), RESOLVED("4"), CLOSED("5");

    private String id;

    private JiraIssueStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }

    public static JiraIssueStatus fromStatus(Status status) {
        switch (status) {
            case NEW:
                return OPEN;
            case ASSIGNED:
                return IN_PROGRESS;
            case CLOSED:
                return CLOSED;
            default:
                return null;
        }
    }
}
