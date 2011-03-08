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
 * status converter from OpenEngSB status to Jira status,
 * see http://docs.atlassian.com/jira/latest/constant-values.html
 */
public final class StatusConverter {
    
    private static HashMap<Issue.Status, String> statusMap;

    private StatusConverter() {

    }

    public static String fromIssueStatus(Issue.Status status) {
        if (statusMap == null) {
            initMap();
        }
        return statusMap.get(status);
    }

    private static void initMap() {
        statusMap = new HashMap<Issue.Status, String>();
        statusMap.put(Issue.Status.CLOSED, "6");
        statusMap.put(Issue.Status.NEW, "1");
        statusMap.put(Issue.Status.UNASSIGNED, "2");
    }

}
