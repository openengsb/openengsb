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
package org.openengsb.drools;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.compiler.RuleBaseLoader;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.model.Event;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:testBeans.xml" })
public class DroolsEndpointTest extends SpringTestSupport {
    private static ServiceMixClient client;

    private final String TEST_EVENT = getTestEvent();

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
    public void testSampleDrlFile() throws Exception {

        Resource rbaseResource = ResourceFactory.newClassPathResource("test1.drl");

        RuleBaseLoader loader = RuleBaseLoader.getInstance();
        InputStream is = rbaseResource.getInputStream();

        RuleBase rbase = loader.loadFromReader(new InputStreamReader(is));
        final StatefulSession rsession = rbase.newStatefulSession();

        Event event = new Event("DroolsTestDomain", "hello");

        rsession.insert(event);

        Event event2 = new Event("DroolsTestDomain", "bla");
        rsession.insert(event2);

        System.out.println("fireing all");
        rsession.fireAllRules();
        System.out.println("done");

        rsession.dispose();

    }

    @Test
    public void testHelloEndpoint() throws Exception {
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("urn:test", "drools"));
        me.getInMessage().setContent(new StringSource(TEST_EVENT));
        me.setOperation(new QName("event"));
        me.getInMessage().setProperty("operation", Boolean.TRUE);
        client.sendSync(me);
        assertNotSame("Exchange was not processed correctly", ExchangeStatus.ERROR, me.getStatus());
    }

    private String getTestEvent() {
        try {
            Event event = new Event("domain", "name");
            event.setValue("buz", "42");
            event.setValue("foo", 42);
            event.setValue("bar", "test");
            return Transformer.toXml(event);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

}
