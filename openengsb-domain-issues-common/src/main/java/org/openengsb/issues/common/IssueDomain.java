/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.issues.common;

import org.openengsb.issues.common.exceptions.IssueDomainException;
import org.openengsb.issues.common.model.Issue;

/**
 * Interface describing a generic issue domain.
 */
public interface IssueDomain {

    /**
     * Creates an issue in an issue tracking system.
     * 
     * @param summary A short summary of the issue.
     * @param description The detailed description of the issue.
     * @param reporter Name of the reporter of the issue.
     * @param type Issue type
     * @param priority Issue priority
     * @return Id of created issue
     */
    String createIssue(Issue issue) throws IssueDomainException;

}
