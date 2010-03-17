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
package org.openengsb.trac;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.openengsb.drools.IssuesDomain;
import org.openengsb.drools.model.Comment;
import org.openengsb.drools.model.Issue;
import org.openengsb.drools.model.Issue.IssuePriority;
import org.openengsb.drools.model.Issue.IssueResolution;
import org.openengsb.drools.model.Issue.IssueStatus;
import org.openengsb.drools.model.Issue.IssueType;
import org.openengsb.trac.xmlrpc.Ticket;

public class TracConnector implements IssuesDomain {
    private Logger log = Logger.getLogger(getClass());

    private Ticket ticket;

    @Override
    public Integer createIssue(Issue issue) {
        Hashtable<String, String> attributes = generateAttributes(issue);
        Integer issueId = -1;

        try {
            issueId = ticket.create(issue.getSummary(), issue.getDescription(), attributes);
        } catch (XmlRpcException e) {
            log.error("Error creating Issue. XMLRPC failed.");
        }

        return issueId;
    }

    @Override
    public void updateIssue(Issue issue) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteIssue(Integer id) {
        ticket.delete(id);
    }

    @Override
    public void addComment(Integer id, Comment comment) {
        ticket.update(id, comment.getText());
    }
    
    /**
     * just for testing to see which fields are available, see TracTest
     */
    public Vector<HashMap<?, ?>> getFields() {
        return ticket.getTicketFields();
    }

    private Hashtable<String, String> generateAttributes(Issue issue) {
        Hashtable<String, String> attributes = new Hashtable<String, String>();

        addPriority(attributes, issue.getPriority());
        addType(attributes, issue.getType());
        addStatus(attributes, issue.getStatus());
        addResolution(attributes, issue.getResolution());
        
        if (issue.getOwner() != null) {
            attributes.put("owner", issue.getOwner());
        }
        if (issue.getReporter() != null) {
            attributes.put("reporter", issue.getReporter());
        }
        if (issue.getAffectedVersion() != null) {
            attributes.put("version", issue.getAffectedVersion());
        }
        return attributes;
    }
    
    private void addPriority(Hashtable<String, String> attributes, IssuePriority priority) {
        if (priority != null) {
            switch (priority) {
            case HIGH:
                attributes.put("priority", "major");
                break;
            case IMMEDIATE:
                attributes.put("priority", "blocker");
                break;
            case LOW:
                attributes.put("priority", "trivial");
                break;
            case NORMAL:
                attributes.put("priority", "minor");
                break;
            case URGENT:
                attributes.put("priority", "critical");
                break;
            }
        }
    }
    
    private void addType(Hashtable<String, String> attributes, IssueType type) {
        if (type != null) {
            switch (type) {
            case BUG:
                attributes.put("type", "defect");
                break;
            case IMPROVEMENT:
                attributes.put("type", "enhancement");
                break;
//            case NEW_FEATURE:
//                attributes.put("type", "feature");
//                break;
            case TASK:
                attributes.put("type", "task");
                break;
            }
        }
    }
    
    private void addStatus(Hashtable<String, String> attributes, IssueStatus status) {
        if (status != null) {
            switch (status) {
            case ASSIGNED:
                attributes.put("status", "assigned");
                break;
            case NEW:
                attributes.put("status", "new");
                break;
            case CLOSED:
                attributes.put("status", "closed");
                break;
            }
        }
    }
    
    private void addResolution(Hashtable<String, String> attributes, IssueResolution resolution) {
        if (resolution != null) {
            switch (resolution) {
            case FIXED:
                attributes.put("resolution", "fixed");
                break;
            case INVALID:
                attributes.put("resolution", "invalid");
                break;
            case NOTFIXABLE:
                attributes.put("resolution", "wontfix");
                break;
            case WONTFIX:
                attributes.put("resolution", "wontfix");
                break;
            case DUPLICATE:
                attributes.put("resolution", "duplicate");
                break;
            case WORKSFORME:
                attributes.put("resolution", "worksforme");
                break;
            case UNABLETOPRODUCE:
                attributes.put("resolution", "worksforme");
                break;
            }
        }
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}