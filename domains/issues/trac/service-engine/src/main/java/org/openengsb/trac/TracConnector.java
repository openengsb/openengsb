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

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.openengsb.drools.IssuesDomain;
import org.openengsb.drools.model.Issue;
import org.openengsb.trac.constants.TracFieldConstants;
import org.openengsb.trac.constants.TracPriorityConstants;
import org.openengsb.trac.constants.TracStatusConstants;
import org.openengsb.trac.xmlrpc.Ticket;

public class TracConnector implements IssuesDomain {
    private Log log = LogFactory.getLog(getClass());

    private Ticket ticket;

    @Override
    public Integer createIssue(Issue issue) {
        Hashtable<String, String> attributes = generateAttributes(issue);
        Integer issueId = -1;

        try {
            issueId = ticket.create(issue.getSummary(), issue.getDescription(), attributes);
            log.info("Successfully created issue " + issue.getSummary() + ", ID is: " + issueId + ".");
        } catch (XmlRpcException e) {
            log.error("Error creating issue " + issue.getSummary() + ". XMLRPC call failed.");
        }

        return issueId;
    }

    @Override
    public void updateIssue(Integer id, String comment, Map<String, Object> changes) {
        Hashtable<String, String> attributes = translateChanges(changes);
        if (comment == null || comment.equals("")) {
            comment = "[No comment added by author]";
        }

        try {
            ticket.update(id, comment, attributes);
            log.info("Successfully updated issue " + id + " with " + changes.size() + " changes.");
        } catch (XmlRpcException e) {
            log.error("Error updating issue " + id + ". XMLRPC call failed.");
        }
    }

    @Override
    public void deleteIssue(Integer id) {
        try {
            ticket.delete(id);
            log.info("Successfully deleted issue " + id + ".");
        } catch (XmlRpcException e) {
            log.error("Error deleting issue " + id + ". XMLRPC call failed.");
        }
    }

    @Override
    public void addComment(Integer id, String comment) {
        try {
            ticket.update(id, comment);
            log.info("Successfully added comment to issue " + id + ".");
        } catch (XmlRpcException e) {
            log.error("Error adding comment to issue " + id + ". XMLRPC call failed.");
        }
    }

    private Hashtable<String, String> generateAttributes(Issue issue) {
        Hashtable<String, String> attributes = new Hashtable<String, String>();

        if (issue.getOwner() != null) {
            attributes.put(TracFieldConstants.FIELD_OWNER, issue.getOwner());
        }
        if (issue.getReporter() != null) {
            attributes.put(TracFieldConstants.FIELD_REPORTER, issue.getReporter());
        }

        addPriority(attributes, issue.getPriority());
        addStatus(attributes, issue.getStatus());

        return attributes;
    }

    private Hashtable<String, String> translateChanges(Map<String, Object> changes) {
        Hashtable<String, String> attributes = new Hashtable<String, String>();

        for (String field : changes.keySet()) {
            try {
                if (field.equals(Issue.fieldDESCRIPTION)) {
                    attributes.put(TracFieldConstants.FIELD_DESCRIPTION, (String) changes.get(field));
                } else if (field.equals(Issue.fieldOWNER)) {
                    attributes.put(TracFieldConstants.FIELD_OWNER, (String) changes.get(field));
                } else if (field.equals(Issue.fieldREPORTER)) {
                    attributes.put(TracFieldConstants.FIELD_REPORTER, (String) changes.get(field));
                } else if (field.equals(Issue.fieldSUMMARY)) {
                    attributes.put(TracFieldConstants.FIELD_SUMMARY, (String) changes.get(field));
                } else if (field.equals(Issue.fieldPRIORITY)) {
                    addPriority(attributes, (String) changes.get(field));
                } else if (field.equals(Issue.fieldSTATUS)) {
                    addStatus(attributes, (String) changes.get(field));
                }
            } catch (ClassCastException e) {
                log.error("Wrong value provided for field " + field + ": " + changes.get(field).getClass().getName());
            }
        }

        return attributes;
    }

    private void addPriority(Hashtable<String, String> attributes, String priority) {
        if (priority != null) {
            if (priority.equals(Issue.priorityHIGH)) {
                attributes.put(TracFieldConstants.FIELD_PRIORITY, TracPriorityConstants.PRIORITY_HIGH);
            } else if (priority.equals(Issue.priorityIMMEDIATE)) {
                attributes.put(TracFieldConstants.FIELD_PRIORITY, TracPriorityConstants.PRIORITY_IMMEDIATE);
            } else if (priority.equals(Issue.priorityLOW)) {
                attributes.put(TracFieldConstants.FIELD_PRIORITY, TracPriorityConstants.PRIORITY_LOW);
            } else if (priority.equals(Issue.priorityNORMAL)) {
                attributes.put(TracFieldConstants.FIELD_PRIORITY, TracPriorityConstants.PRIORITY_NORMAL);
            } else if (priority.equals(Issue.priorityURGENT)) {
                attributes.put(TracFieldConstants.FIELD_PRIORITY, TracPriorityConstants.PRIORITY_URGENT);
            }
        }
    }

    private void addStatus(Hashtable<String, String> attributes, String status) {
        if (status != null) {
            if (status.equals(Issue.statusNEW)) {
                attributes.put(TracFieldConstants.FIELD_STATUS, TracStatusConstants.STATUS_NEW);
            } else if (status.equals(Issue.statusASSIGNED)) {
                attributes.put(TracFieldConstants.FIELD_STATUS, TracStatusConstants.STATUS_ASSIGNED);
            } else if (status.equals(Issue.statusCLOSED)) {
                attributes.put(TracFieldConstants.FIELD_STATUS, TracStatusConstants.STATUS_CLOSED);
            }
        }
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

}