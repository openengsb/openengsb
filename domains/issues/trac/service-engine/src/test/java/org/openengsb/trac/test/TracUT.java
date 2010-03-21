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
package org.openengsb.trac.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.annotation.Resource;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.trac.TracConstants;
import org.openengsb.trac.xmlrpc.Ticket;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class TracUT {
    @Resource
    private Ticket ticket;

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void testCreateIssue() throws XmlRpcException {
        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put(TracConstants.FIELD_STATUS, TracConstants.STATUS_NEW);
        attributes.put(TracConstants.FIELD_PRIORITY, TracConstants.PRIORITY_IMMEDIATE);
        attributes.put(TracConstants.FIELD_OWNER, "testowner");
        attributes.put(TracConstants.FIELD_REPORTER, "testreporter");
        String s = "test " + new Date();

        Integer id = ticket.create(s, "testdescription", attributes);

        Vector<?> v = ticket.get(id);
        long l = ((Date) v.get(1)).getTime();

        assertEquals(v.get(0), id);
        assertTrue(l < System.currentTimeMillis() && l + 10000000 > System.currentTimeMillis());
        assertEquals(v.get(1), v.get(2));

        HashMap<String, String> attributes2 = (HashMap<String, String>) v.get(3);
        assertEquals(attributes2.get(TracConstants.FIELD_SUMMARY), s);
        assertEquals(attributes2.get(TracConstants.FIELD_DESCRIPTION), "testdescription");
        assertEquals(attributes2.get(TracConstants.FIELD_STATUS), attributes.get(TracConstants.FIELD_STATUS));
        assertEquals(attributes2.get(TracConstants.FIELD_PRIORITY), attributes2.get(TracConstants.FIELD_PRIORITY));
        assertEquals(attributes2.get(TracConstants.FIELD_OWNER), attributes2.get(TracConstants.FIELD_OWNER));
        assertEquals(attributes2.get(TracConstants.FIELD_REPORTER), attributes2.get(TracConstants.FIELD_REPORTER));
    }

    @Test(expected = XmlRpcException.class)
    @Ignore
    public void testDeleteIssue() throws XmlRpcException {
        Integer id = ticket.create("testsummary", "testdescription");
        assertNotNull(ticket.get(id));

        ticket.delete(id);
        ticket.get(id);
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    public void testUpdateIssue() throws XmlRpcException {
        Integer id = ticket.create("testsummary", "testdescription");
        assertNotNull(ticket.get(id));

        Hashtable<String, String> attributes = new Hashtable<String, String>();
        attributes.put(TracConstants.FIELD_STATUS, TracConstants.STATUS_CLOSED);
        attributes.put(TracConstants.FIELD_OWNER, "testowner");
        attributes.put(TracConstants.FIELD_REPORTER, "testreporter");
        attributes.put(TracConstants.FIELD_PRIORITY, TracConstants.PRIORITY_LOW);
        attributes.put(TracConstants.FIELD_SUMMARY, "testsummary2");
        attributes.put(TracConstants.FIELD_DESCRIPTION, "testdescription2");

        ticket.update(id, "testcomment", attributes);

        Vector<?> v = ticket.get(id);

        HashMap<String, String> attributes2 = (HashMap<String, String>) v.get(3);
        assertEquals(attributes2.get(TracConstants.FIELD_STATUS), attributes.get(TracConstants.FIELD_STATUS));
        assertEquals(attributes2.get(TracConstants.FIELD_PRIORITY), attributes2.get(TracConstants.FIELD_PRIORITY));
        assertEquals(attributes2.get(TracConstants.FIELD_OWNER), attributes2.get(TracConstants.FIELD_OWNER));
        assertEquals(attributes2.get(TracConstants.FIELD_REPORTER), attributes2.get(TracConstants.FIELD_REPORTER));
        assertEquals(attributes2.get(TracConstants.FIELD_SUMMARY), attributes2.get(TracConstants.FIELD_SUMMARY));
        assertEquals(attributes2.get(TracConstants.FIELD_DESCRIPTION), attributes2.get(TracConstants.FIELD_DESCRIPTION));
    }
}