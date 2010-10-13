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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;
import org.openengsb.domains.issue.models.Issue;
import org.openengsb.domains.issue.trac.internal.models.TicketHandlerFactory;

public class TracConnectorUT {

    @Test
    public void testCreateIssue() throws XmlRpcException {
        TicketHandlerFactory ticketFactory = new TicketHandlerFactory();
        ticketFactory.setUsername("");
        ticketFactory.setUserPassword("");
        ticketFactory.setServerUrl("http://127.0.0.1:8000/test/rpc");
        TracConnector tracImpl = new TracConnector("1", ticketFactory);
        Issue issue = new Issue();
        issue.setDescription("test Issue");
        issue.setSummary("test summery");
        issue.setId(99);
        issue.setStatus(Issue.Status.NEW);
        issue.setPriority(Issue.Priority.NONE);
        String id = tracImpl.createIssue(issue);

        assertThat(id, not(is("-1")));
    }

}
