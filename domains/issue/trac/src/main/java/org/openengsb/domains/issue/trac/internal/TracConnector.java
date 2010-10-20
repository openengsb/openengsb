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

package org.openengsb.domains.issue.trac.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domains.issue.IssueDomain;
import org.openengsb.domains.issue.models.Issue;
import org.openengsb.domains.issue.models.IssueAttribute;
import org.openengsb.domains.issue.trac.internal.models.TicketHandlerFactory;
import org.openengsb.domains.issue.trac.internal.models.constants.TracFieldConstants;
import org.openengsb.domains.issue.trac.internal.models.constants.TracPriorityConstants;
import org.openengsb.domains.issue.trac.internal.models.constants.TracStatusConstants;
import org.openengsb.domains.issue.trac.internal.models.xmlrpc.Ticket;

public class TracConnector implements IssueDomain {

    private static Log log = LogFactory.getLog(TracConnector.class);

    private AliveState state = AliveState.DISCONNECTED;
    private final String id;
    private final TicketHandlerFactory ticketFactory;

    public TracConnector(String id, TicketHandlerFactory ticketFactory) {
        this.id = id;
        this.ticketFactory = ticketFactory;
    }

    @Override
    public String createIssue(Issue issue) {
        Ticket ticket = createTicket();
        Hashtable<IssueAttribute, String> attributes = generateAttributes(issue);
        Integer issueId = -1;

        try {
            issueId = ticket.create(issue.getSummary(), issue.getDescription(), attributes);
            this.state = AliveState.ONLINE;
            log.info("Successfully created issue " + issue.getSummary() + ", ID is: " + issueId + ".");
        } catch (XmlRpcException e) {
            log.error("Error creating issue " + issue.getSummary() + ". XMLRPC call failed.");
            this.state = AliveState.OFFLINE;
        }
        return issueId.toString();
    }

    @Override
    public void deleteIssue(Integer id) {
        try {
            Ticket ticket = createTicket();
            ticket.delete(id);
            log.info("Successfully deleted issue " + id + ".");
        } catch (XmlRpcException e) {
            log.error("Error deleting issue " + id + ". XMLRPC call failed.");
        }
    }

    @Override
    public void addComment(Integer id, String comment) {
        try {
            Ticket ticket = createTicket();
            ticket.update(id, comment);
            log.info("Successfully added comment to issue " + id + ".");
        } catch (XmlRpcException e) {
            log.error("Error adding comment to issue " + id + ". XMLRPC call failed.");
        }
    }

    @Override
    public void updateIssue(Integer id, String comment, HashMap<IssueAttribute, String> changes) {
        Hashtable<IssueAttribute, String> attributes = translateChanges(changes);
        if (comment == null || comment.equals("")) {
            comment = "[No comment added by author]";
        }

        try {
            Ticket ticket = createTicket();
            ticket.update(id, comment, attributes);
            log.info("Successfully updated issue " + id + " with " + changes.size() + " changes.");
        } catch (XmlRpcException e) {
            log.error("Error updating issue " + id + ". XMLRPC call failed.");
        }
    }

    private Ticket createTicket() {
        if (ticketFactory != null) {
            Ticket ticket = ticketFactory.createTicket();
            if (ticket != null) {
                this.state = AliveState.CONNECTING;
            } else {
                this.state = AliveState.DISCONNECTED;
            }
            return ticket;
        }
        throw new RuntimeException("tickethandler not yet set");
    }

    public TicketHandlerFactory getTicketHandlerFactory() {
        return this.ticketFactory;
    }

    public String getId() {
        return this.id;
    }

    private Hashtable<IssueAttribute, String> translateChanges(Map<IssueAttribute, String> changes) {
        Hashtable<IssueAttribute, String> attributes = new Hashtable<IssueAttribute, String>();

        for (Map.Entry<IssueAttribute, String> entry : changes.entrySet()) {
            try {
                if (entry.getKey().equals(Issue.Field.DESCRIPTION)) {
                    attributes.put(TracFieldConstants.DESCRIPTION, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.OWNER)) {
                    attributes.put(TracFieldConstants.OWNER, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.REPORTER)) {
                    attributes.put(TracFieldConstants.SUMMARY, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.SUMMARY)) {
                    attributes.put(TracFieldConstants.SUMMARY, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.PRIORITY)) {
                    addPriority(attributes, Issue.Priority.valueOf(entry.getValue()));
                } else if (entry.getKey().equals(Issue.Field.STATUS)) {
                    addStatus(attributes, Issue.Status.valueOf(entry.getValue()));
                }
            } catch (ClassCastException e) {
                log.error("Wrong value provided for field " + entry.getKey() + ": "
                        + entry.getValue().getClass().getName());
            }
        }

        return attributes;
    }

    private Hashtable<IssueAttribute, String> generateAttributes(Issue issue) {
        Hashtable<IssueAttribute, String> attributes = new Hashtable<IssueAttribute, String>();

        if (issue.getOwner() != null) {
            attributes.put(TracFieldConstants.OWNER, issue.getOwner());
        }
        if (issue.getReporter() != null) {
            attributes.put(TracFieldConstants.REPORTER, issue.getReporter());
        }

        addPriority(attributes, issue.getPriority());
        addStatus(attributes, issue.getStatus());

        return attributes;
    }

    private void addPriority(Hashtable<IssueAttribute, String> attributes, Issue.Priority priority) {
        if (priority != null) {
            if (priority.equals(Issue.Priority.HIGH)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.HIGH.toString());
            } else if (priority.equals(Issue.Priority.IMMEDIATE)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.IMMEDIATE.toString());
            } else if (priority.equals(Issue.Priority.LOW)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.LOW.toString());
            } else if (priority.equals(Issue.Priority.NORMAL)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.NORMAL.toString());
            } else if (priority.equals(Issue.Priority.URGEND)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.URGENT.toString());
            }
        }
    }

    private void addStatus(Hashtable<IssueAttribute, String> attributes, Issue.Status status) {
        if (status != null) {
            if (status.equals(Issue.Status.NEW)) {
                attributes.put(TracFieldConstants.STATUS, TracStatusConstants.NEW.toString());
            } else if (status.equals(Issue.Status.ASSIGNED)) {
                attributes.put(TracFieldConstants.STATUS, TracStatusConstants.ASSIGNED.toString());
            } else if (status.equals(Issue.Status.CLOSED)) {
                attributes.put(TracFieldConstants.STATUS, TracStatusConstants.CLOSED.toString());
            }
        }
    }

    @Override
    public AliveState getAliveState() {
        return state;
    }
}
