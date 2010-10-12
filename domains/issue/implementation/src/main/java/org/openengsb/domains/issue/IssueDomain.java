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

package org.openengsb.domains.issue;

import java.util.HashMap;

import org.openengsb.core.common.Domain;
import org.openengsb.domains.issue.models.Issue;
import org.openengsb.domains.issue.models.IssueAttribute;

public interface IssueDomain extends Domain {

    /**
     * creates an issue on the server and returned the generated id
     */
    String createIssue(Issue issue);

    /**
     * delete an issue, specified by his id
     */
    void deleteIssue(Integer id);

    /**
     * add a comment to an issue, specified by his id
     */
    void addComment(Integer id, String comment);

    /**
     * update an issue, specified by his id, the comment param can be null, changes: key of map is what field has to be
     * changed,
     */
    void updateIssue(Integer id, String comment, HashMap<IssueAttribute, String> changes);

}
