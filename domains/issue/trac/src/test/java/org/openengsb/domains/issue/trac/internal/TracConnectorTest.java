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

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.domains.issue.models.Issue;
import org.openengsb.domains.issue.trac.internal.models.TicketHandlerFactory;
import org.openengsb.domains.issue.trac.internal.models.constants.TracFieldConstants;
import org.openengsb.domains.issue.trac.internal.models.constants.TracPriorityConstants;
import org.openengsb.domains.issue.trac.internal.models.constants.TracStatusConstants;
import org.openengsb.domains.issue.trac.internal.models.xmlrpc.Ticket;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;


public class TracConnectorTest {

    Ticket ticketMock;
    TracConnector tracConnector;


    @Before
    public void setUp() {
        ticketMock = Mockito.mock(Ticket.class);
        TicketHandlerFactory tc = Mockito.mock(TicketHandlerFactory.class);
        tracConnector = new TracConnector("1", tc);
        Mockito.when(tc.createTicket()).thenReturn(ticketMock);
    }

    @Test
    public void createNewIssue() throws Exception {
        Issue i = new Issue();
        String s = "test " + new Date();
        i.setSummary(s);
        i.setDescription("testdescription");
        i.setOwner("testowner");
        i.setPriority(Issue.PRIORITYURGENT);
        i.setReporter("testreporter");
        i.setStatus(Issue.STATUSNEW);

        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put(TracFieldConstants.FIELD_OWNER, "testowner");
        attributes.put(TracFieldConstants.FIELD_REPORTER, "testreporter");
        attributes.put(TracFieldConstants.FIELD_PRIORITY, TracPriorityConstants.PRIORITY_URGENT);
        attributes.put(TracFieldConstants.FIELD_STATUS, TracStatusConstants.STATUS_NEW);

        tracConnector.createIssue(i);
        Mockito.verify(ticketMock, Mockito.times(1))
            .create(Mockito.eq(s), Mockito.eq("testdescription"), Mockito.eq(attributes));
    }


    @Test
    public void testToDeleteIssue() throws Exception {
        tracConnector.deleteIssue(-1);
        Mockito.verify(ticketMock, Mockito.times(1)).delete(Mockito.eq(-1));
    }

    @Test
    public void testToAddComment() throws Exception {

        tracConnector.addComment(5, "testcomment");
        Mockito.verify(ticketMock, Mockito.times(1)).update(Mockito.eq(5), Mockito.eq("testcomment"));
    }

    @Test
    public void testUpdateIssue() throws Exception {
        HashMap<String, Object> changes = new HashMap<String, Object>();
        changes.put(Issue.FIELDSTATUS, Issue.STATUSCLOSED);

        Hashtable<String, String> result = new Hashtable<String, String>();
        result.put(TracFieldConstants.FIELD_STATUS, TracStatusConstants.STATUS_CLOSED);

        tracConnector.updateIssue(3, null, changes);
        Mockito.verify(ticketMock, Mockito.times(1))
            .update(Mockito.eq(3), Mockito.eq("[No comment added by author]"), Mockito.eq(result));
    }

    @Test
    public void testCreateOnNotExistingTicket_ShouldPrintErrorMessage() throws Exception {
        Mockito.when(ticketMock.create(Mockito.anyString(), Mockito.anyString(), Mockito.any(Hashtable.class)))
            .thenThrow(new XmlRpcException("test"));
        tracConnector.createIssue(new Issue());
    }

    @Test
    public void testUpdateANotExistingTicket_ShouldPrintErrorMessage() throws Exception {
        Mockito.when(ticketMock.update(Mockito.anyInt(), Mockito.anyString(), Mockito.any(Hashtable.class)))
            .thenThrow(new XmlRpcException("test"));
        tracConnector.updateIssue(0, "test", new HashMap<String, Object>());
    }

    @Test
    public void testCommentOnNotExistingTicket_ShouldPrintErrorMessage() throws Exception {
        Mockito.when(ticketMock.update(Mockito.anyInt(), Mockito.anyString())).thenThrow(new XmlRpcException("test"));
        tracConnector.addComment(0, "test");
    }

    @Test
    public void testDeleteANotExistingTicket_ShouldPrintErrorMessage() throws Exception {
        Mockito.when(ticketMock.delete(Mockito.anyInt())).thenThrow(new XmlRpcException("test"));
        tracConnector.deleteIssue(0);
    }

}
