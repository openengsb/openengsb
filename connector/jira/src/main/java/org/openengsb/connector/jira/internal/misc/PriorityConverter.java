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

import java.util.HashMap;

import org.openengsb.domain.issue.models.Issue;

/**
 * priority converter from OpenEngSB priority to Jira priority,
 * see http://docs.atlassian.com/jira/latest/constant-values.html
 */
public final class PriorityConverter {
    
    private static HashMap<Issue.Priority, String> fieldMap;

    private PriorityConverter() {

    }

    public static String fromIssuePriority(Issue.Priority priority) {
        if (fieldMap == null) {
            initMap();
        }
        return fieldMap.get(priority);
    }


    private static void initMap() {
        fieldMap = new HashMap<Issue.Priority, String>();
        fieldMap.put(Issue.Priority.IMMEDIATE, "1");
        fieldMap.put(Issue.Priority.HIGH, "2");
        fieldMap.put(Issue.Priority.URGEND, "3");
        fieldMap.put(Issue.Priority.NONE, "4");
        fieldMap.put(Issue.Priority.LOW, "5");
    }
}
