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

package org.openengsb.connector.jira.internal.misc;

import org.openengsb.domain.issue.models.Issue;

/**
 * type converter from OpenEngSB type to Jira type,
 * see http://docs.atlassian.com/jira/latest/constant-values.html
 */
public final class TypeConverter {

    private TypeConverter() {

    }

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
