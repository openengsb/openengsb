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

import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Test;

public class TemporaryTest extends TestCase {

    @Test
    public void testCreateTicket() {
        TracConnector conn = new TracConnector("http://10.0.0.11:8000/trac/login/xmlrpc", "david", "david");
        String ticketId = conn.createIssue("mysummary " + UUID.randomUUID(), "mydescription " + UUID.randomUUID(),
                "reporter1", "owner1", "type_unknown", "priority_unknown");

        System.out.println("Created ticket " + ticketId);
    }

}
