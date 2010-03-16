/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.drools;

import org.openengsb.drools.model.Issue;

public interface DroolsIssuesDomain extends Domain {

    /**
     * Creates an issue in an issue tracking system.
     *
     * @param issue The issue to create.
     * @return ID of created issue
     */
    String createIssue(Issue issue) throws Exception;

    /**
     * Updates the given issue.
     *
     * @param issue The issue to update (the ID of the issue must be set).
     * @throws IssueDomainException
     */
    void updateIssue(Issue issue) throws Exception;

    /**
     * Deletes the issue with the given ID.
     *
     * @param id ID of the issue being deleted.
     * @throws IssueDomainException
     */
    void deleteIssue(String id) throws Exception;

}
