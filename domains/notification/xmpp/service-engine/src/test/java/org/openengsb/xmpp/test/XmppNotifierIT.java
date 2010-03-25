/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.openengsb.xmpp.test;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:testBeans.xml" })
public class XmppNotifierIT extends SpringTestSupport {
    private static ServiceMixClient client;

    private static final String TEST_EVENT = 
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<list xmlns=\"http://org.openengsb/util/serialization\" domainConcept=\"\" format=\"\" name=\"event\">"
            + "    <text domainConcept=\"\" format=\"\" name=\"event\">org.openengsb.core.model.Event</text>"
            + "    <list domainConcept=\"\" format=\"\" name=\"superclasses\">"
            + "        <text domainConcept=\"\" format=\"\" name=\"superclass\">java.lang.Object</text>"
            + "    </list>" 
            + "    <text domainConcept=\"\" format=\"\" name=\"name\">testevent</text>"
            + "    <text domainConcept=\"\" format=\"\" name=\"domain\">drools</text>" 
            + "</list>";

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("testXBean.xml");
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = new DefaultServiceMixClient(this.jbi);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testSendSampleMessage() throws Exception {
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("urn:test", "xmpp"));
        me.getInMessage().setContent(new StringSource(TEST_EVENT));
        me.setOperation(new QName("event"));
        me.getInMessage().setProperty("operation", Boolean.TRUE);
        client.sendSync(me);
        assertNotSame("Exchange was not processed correctly", ExchangeStatus.ERROR, me.getStatus());
    }
}
