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

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.openengsb.drools.IssuesDomain;
import org.openengsb.drools.model.Comment;
import org.openengsb.drools.model.Issue;
import org.openengsb.trac.xmlrpc.Ticket;

public class TracConnector implements IssuesDomain {
    private Logger log = Logger.getLogger(getClass());

    private Ticket ticket;

    @Override
    public int createIssue(Issue issue) {
        Hashtable<String, String> attributes = generateAttributes(issue);
        int id = -1;

        try {
            id = ticket.create(issue.getSummary(), issue.getDescription(), attributes);
        } catch (XmlRpcException e) {
            log.error("Error creating Issue. XMLRPC failed.");
        }

        return id;
    }

    @Override
    public void updateIssue(Issue issue) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteIssue(int id) {
        throw new NotImplementedException();
    }

    @Override
    public void addComment(int id, Comment comment) {
        throw new NotImplementedException();
    }

    private Hashtable<String, String> generateAttributes(Issue issue) {
        Hashtable<String, String> attributes = new Hashtable<String, String>();

        if (issue.getPriority() != null) {
            switch (issue.getPriority()) {
            case HIGH:
                attributes.put("priority", "major");
                break;
            case IMMEDIATE:
                attributes.put("priority", "blocker");
                break;
            case LOW:
                attributes.put("priority", "trivial");
                break;
            case NONE:
                attributes.put("priority", "none");
                break;
            case NORMAL:
                attributes.put("priority", "minor");
                break;
            case URGENT:
                attributes.put("priority", "critical");
                break;
            }
        }
        if (issue.getType() != null) {
            switch (issue.getType()) {
            case BUG:
                attributes.put("type", "defect");
                break;
            case IMPROVEMENT:
                attributes.put("type", "enhancement");
                break;
            case NEW_FEATURE:
                attributes.put("type", "feature");
                break;
            case TASK:
                attributes.put("type", "ntaskone");
                break;
            }
        }
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

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}