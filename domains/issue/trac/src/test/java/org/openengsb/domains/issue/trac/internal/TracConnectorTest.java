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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.domains.issue.models.Issue;
import org.openengsb.domains.issue.models.IssueAttribute;
import org.openengsb.domains.issue.trac.internal.models.TicketHandlerFactory;
import org.openengsb.domains.issue.trac.internal.models.constants.TracFieldConstants;
import org.openengsb.domains.issue.trac.internal.models.constants.TracPriorityConstants;
import org.openengsb.domains.issue.trac.internal.models.constants.TracStatusConstants;
import org.openengsb.domains.issue.trac.internal.models.xmlrpc.Ticket;

public class TracConnectorTest {

    Ticket ticketMock;
    TracConnector tracConnector;

    @Before
    public void setUp() {
        ticketMock = mock(Ticket.class);
        TicketHandlerFactory tc = mock(TicketHandlerFactory.class);
        tracConnector = new TracConnector("1", tc);
        when(tc.createTicket()).thenReturn(ticketMock);
    }

    @Test
    public void createNewIssue() throws Exception {
        Issue i = new Issue();
        String s = "test " + new Date();
        i.setSummary(s);
        i.setDescription("testdescription");
        i.setOwner("testowner");
        i.setPriority(Issue.Priority.URGEND);
        i.setReporter("testreporter");
        i.setStatus(Issue.Status.NEW);

        Hashtable<Enum<?>, String> attributes = new Hashtable<Enum<?>, String>();
        attributes.put(TracFieldConstants.OWNER, "testowner");
        attributes.put(TracFieldConstants.REPORTER, "testreporter");
        attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.URGENT.toString());
        attributes.put(TracFieldConstants.STATUS, TracStatusConstants.NEW.toString());

        tracConnector.createIssue(i);
        verify(ticketMock, times(1))
            .create(eq(s), eq("testdescription"), eq(attributes));
    }

    @Test
    public void testToDeleteIssue() throws Exception {
        tracConnector.deleteIssue(-1);
        verify(ticketMock, times(1)).delete(eq(-1));
    }

    @Test
    public void testToAddComment() throws Exception {
        tracConnector.addComment(5, "testcomment");
        verify(ticketMock, times(1)).update(eq(5), eq("testcomment"));
    }

    @Test
    public void testUpdateIssue() throws Exception {
        HashMap<IssueAttribute, String> changes = new HashMap<IssueAttribute, String>();
        changes.put(Issue.Field.STATUS, Issue.Status.CLOSED.toString());

        Hashtable<IssueAttribute, String> result = new Hashtable<IssueAttribute, String>();
        result.put(TracFieldConstants.STATUS, TracStatusConstants.CLOSED.toString());

        tracConnector.updateIssue(3, null, changes);
        verify(ticketMock, times(1))
            .update(eq(3), eq("[No comment added by author]"), eq(result));
    }

    @Test
    public void testCreateOnNotExistingTicket_ShouldPrintErrorMessageAndDonotThrowException() throws Exception {
        when(ticketMock.create(anyString(), anyString(), any(Hashtable.class)))
            .thenThrow(new XmlRpcException("test"));
        tracConnector.createIssue(new Issue());
    }

    @Test
    public void testUpdateANotExistingTicket_ShouldPrintErrorMessageAndDonotThrowException() throws Exception {
        when(ticketMock.update(anyInt(), anyString(), any(Hashtable.class)))
            .thenThrow(new XmlRpcException("test"));
        tracConnector.updateIssue(0, "test", new HashMap<IssueAttribute, String>());
    }

    @Test
    public void testCommentOnNotExistingTicket_ShouldPrintErrorMessageAndDonotThrowException() throws Exception {
        when(ticketMock.update(anyInt(), anyString())).thenThrow(new XmlRpcException("test"));
        tracConnector.addComment(0, "test");
    }

    @Test
    public void testDeleteANotExistingTicket_ShouldPrintErrorMessageAndDonotThrowException() throws Exception {
        when(ticketMock.delete(anyInt())).thenThrow(new XmlRpcException("test"));
        tracConnector.deleteIssue(0);
    }

}
