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
 * type converter from OpenEngSB type to Jira type,
 * see http://docs.atlassian.com/jira/latest/constant-values.html
 */
public final class TypeConverter {

    private static HashMap<Issue.Type, String> typeMap;
    private static HashMap<String, String> codeMap;

    private TypeConverter() {

    }

    public static String fromIssueType(Issue.Type type) {
        if (typeMap == null) {
            initMap();
        }
        return typeMap.get(type);
    }

    private static void initMap() {
        typeMap = new HashMap<Issue.Type, String>();
        typeMap.put(Issue.Type.BUG, "1");
        typeMap.put(Issue.Type.NEW_FEATURE, "2");
        typeMap.put(Issue.Type.TASK, "3");
        typeMap.put(Issue.Type.IMPROVEMENT, "4");
    }


    public static String fromCode(String code) {
        if (codeMap == null) {
            initCodeMap();
        }
        return codeMap.get(code);
    }

    private static void initCodeMap() {
        codeMap = new HashMap<String, String>();
        codeMap.put("1", "Bug");
        codeMap.put("2", "New Feature");
        codeMap.put("3", "Task");
        codeMap.put("4", "Improvement");
    }
}
