/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.drools;

import java.util.HashMap;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.common.JbiConstants;
import org.apache.servicemix.components.util.MockServiceComponent;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.ReceiverComponent;
import org.springframework.core.io.ClassPathResource;

import org.w3c.dom.Element;

import junit.framework.TestCase;

public class DroolsComponentTest extends TestCase {

    private JBIContainer jbi;
    private DroolsComponent drools;
    private ServiceMixClient client;
    
    protected void setUp() throws Exception {
        super.setUp();
        jbi = new JBIContainer();
        jbi.setEmbedded(true);
        jbi.init();
        client = new DefaultServiceMixClient(jbi);
    }
    
    protected void tearDown() throws Exception {
        jbi.shutDown();
    }
    
    public void testChainedRoutingInOnly() throws Exception {
        drools = new DroolsComponent();
        
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
                                                     new QName("smx", "drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("chained.drl"));
        
        drools.setEndpoints(new DroolsEndpoint[] {endpoint});
        jbi.activateComponent(drools, "servicemix-drools");

        ReceiverComponent target = new ReceiverComponent();
        target.setService(new QName("smx", "target"));
        target.setEndpoint("endpoint");
        
        jbi.activateComponent(target, "target");
        
        jbi.start();
        
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("smx", "drools"));
        me.setOperation(new QName("smx", "process"));
        me.getInMessage().setContent(new StringSource("<payload />"));
        me.setProperty(JbiConstants.CORRELATION_ID, "TEST");
        if (client.sendSync(me, 10000)) {
            assertEquals(ExchangeStatus.DONE, me.getStatus());
        } else {
            fail ("No response from drools in time...");
        }
        
        Thread.sleep(50);
    }
    
    public void testRouteInOnly() throws Exception {
        drools = new DroolsComponent();
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
                                                     new QName("drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("router.drl"));
        drools.setEndpoints(new DroolsEndpoint[] {endpoint });
        jbi.activateComponent(drools, "servicemix-drools");
        
        ReceiverComponent r1 = new ReceiverComponent(new QName("target1"), "endpoint");
        ReceiverComponent r2 = new ReceiverComponent(new QName("target2"), "endpoint");
        ReceiverComponent r3 = new ReceiverComponent(new QName("target3"), "endpoint");
        jbi.activateComponent(r1, "receiver1");
        jbi.activateComponent(r2, "receiver2");
        jbi.activateComponent(r3, "receiver3");
        
        jbi.start();
        
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='0' />"));
        client.sendSync(me);
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        
        me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='1' />"));
        client.sendSync(me);

        me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='2' />"));
        client.sendSync(me);
        
        me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='3' />"));
        me.getInMessage().setProperty("prop", Boolean.TRUE);
        client.sendSync(me);
        
        me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='4' />"));
        client.sendSync(me);
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        
        r1.getMessageList().assertMessagesReceived(1);
        r2.getMessageList().assertMessagesReceived(1);
        r3.getMessageList().assertMessagesReceived(1);
        
        Thread.sleep(50);
    }
    
    public void testRouteInOut() throws Exception {
        drools = new DroolsComponent();
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
                                                     new QName("drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("router.drl"));
        drools.setEndpoints(new DroolsEndpoint[] {endpoint });
        jbi.activateComponent(drools, "servicemix-drools");
        
        MockServiceComponent m1 = new MockServiceComponent(new QName("target1"), "endpoint");
        m1.setResponseContent(new StringSource("<target1/>"));
        MockServiceComponent m2 = new MockServiceComponent(new QName("target2"), "endpoint");
        m2.setResponseContent(new StringSource("<target2/>"));
        MockServiceComponent m3 = new MockServiceComponent(new QName("target3"), "endpoint");
        m3.setResponseContent(new StringSource("<target3/>"));
        jbi.activateComponent(m1, "mock1");
        jbi.activateComponent(m2, "mock2");
        jbi.activateComponent(m3, "mock3");
        
        jbi.start();
        
        InOut me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='0' />"));
        client.sendSync(me);
        assertNotNull(me.getFault());
        client.done(me);
        
        me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='1' />"));
        client.sendSync(me);
        Element e = new SourceTransformer().toDOMElement(me.getOutMessage());
        assertEquals("target1", e.getLocalName());
        client.done(me);

        me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='2' />"));
        client.sendSync(me);
        e = new SourceTransformer().toDOMElement(me.getOutMessage());
        assertEquals("target2", e.getLocalName());
        client.done(me);

        me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='3' />"));
        me.getInMessage().setProperty("prop", Boolean.TRUE);
        client.sendSync(me);
        e = new SourceTransformer().toDOMElement(me.getOutMessage());
        assertEquals("target3", e.getLocalName());
        client.done(me);

        me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<test id='4' />"));
        client.sendSync(me);
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        
        Thread.sleep(50);
    }

    public void testFibonacci() throws Exception {
        drools = new DroolsComponent();
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
                                                     new QName("drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("fibonacci.drl"));
        endpoint.setGlobals(new HashMap<String, Object>());
        endpoint.getGlobals().put("max", 100);
        drools.setEndpoints(new DroolsEndpoint[] {endpoint });
        jbi.activateComponent(drools, "servicemix-drools");
        
        jbi.start();
        
        InOut me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<fibonacci>50</fibonacci>"));
        me.getInMessage().setProperty("prop", Boolean.TRUE);
        client.sendSync(me);
        Element e = new SourceTransformer().toDOMElement(me.getOutMessage());
        assertEquals("result", e.getLocalName());
        assertEquals("12586269025", e.getTextContent());
        client.done(me);

        me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<fibonacci>150</fibonacci>"));
        me.getInMessage().setProperty("prop", Boolean.TRUE);
        client.sendSync(me);
        assertNotNull(me.getFault());
        client.done(me);
        
        Thread.sleep(50);
    }   
}
