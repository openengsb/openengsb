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
package org.openengsb.issues.trac;

import java.util.List;

import org.openengsb.drools.DroolsIssuesDomain;
import org.openengsb.issues.common.exceptions.IssueDomainException;
import org.openengsb.issues.common.model.Comment;
import org.openengsb.issues.common.model.Issue;
import org.openengsb.issues.common.model.IssuePriority;
import org.openengsb.issues.common.model.IssueType;

public class TracIssuesDomain implements DroolsIssuesDomain {

    private final TracConnector connector;

    public TracIssuesDomain(TracConnector connector) {
        this.connector = connector;
    }

    @Override
    public boolean addComment(String issueId, String comment) {
        // TODO
        if (true) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        Comment c = new Comment();
        c.setText(comment);

        Issue issue = new Issue();
        List<Comment> comments = issue.getComments();
        comments.add(c);

        issue.setComments(comments);

        return false;
    }

    @Override
    public synchronized String createIssue(String name) {
        Issue issue = new Issue();
        issue.setSummary(name + " (summary)");
        issue.setDescription(name + " (description)");
        issue.setPriority(IssuePriority.HIGH);
        issue.setType(IssueType.BUG);
        issue.setOwner("user");
        issue.setReporter("user");
        issue.setAffectedVersion("1.0");
        
        try {
            return connector.createIssue(issue);
        } catch (IssueDomainException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteIssue(String issueId) {
        try {
            connector.deleteIssue(issueId);
        } catch (IssueDomainException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
