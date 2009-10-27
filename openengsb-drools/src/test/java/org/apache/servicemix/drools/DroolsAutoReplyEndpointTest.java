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

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.common.JbiConstants;
import org.apache.servicemix.drools.model.Exchange;
import org.apache.servicemix.expression.JAXPStringXPathExpression;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.ReceiverComponent;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

/**
 * Tests for {@link DroolsEndpoint} configured with auto-reply set to <code>true</code>
 */
public class DroolsAutoReplyEndpointTest extends TestCase {
    
    private JBIContainer jbi;
    private DroolsComponent drools;
    private ServiceMixClient client;
    private final SourceTransformer transformer = new SourceTransformer();
    
    protected void setUp() throws Exception {
        super.setUp();
        jbi = new JBIContainer();
        jbi.setEmbedded(true);
        jbi.init();
        client = new DefaultServiceMixClient(jbi);
        drools = new DroolsComponent();
    }
    
    protected void tearDown() throws Exception {
        jbi.shutDown();
    }
    
    public void testAutoReply() throws Exception {
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
                                                                new QName("smx", "drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("AutoReply.drl"));
        endpoint.setAutoReply(true);

        drools.setEndpoints(new DroolsEndpoint[] {endpoint});
        jbi.activateComponent(drools, "servicemix-drools");

        jbi.start();

        InOut me = client.createInOutExchange();
        me.setService(new QName("smx", "drools"));
        me.setOperation(new QName("smx", "process"));
        String inMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test/>";
        String outMessage = "Never Initialized";
        me.getInMessage().setContent(new StringSource(inMessage));
        if (client.sendSync(me,10000)) {
            outMessage = transformer.toString(me.getOutMessage().getContent());
            assertEquals(inMessage, outMessage);
        } else {
            fail ("No response from drools in time...");
        }

        client.done(me);

        Thread.sleep(50);
    }

    public void testModifyXMLWithAValue() throws Exception {
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
                                                                new QName("smx", "drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("XPathAttrMod-StringValue.drl"));
        endpoint.setAutoReply(true);

        drools.setEndpoints(new DroolsEndpoint[] {endpoint});
        jbi.activateComponent(drools, "servicemix-drools");

        ReceiverComponent target = new ReceiverComponent();
        target.setService(new QName("smx", "target"));
        target.setEndpoint("endpoint");

        jbi.activateComponent(target, "target");

        jbi.start();

        InOut me = client.createInOutExchange();
        me.setService(new QName("smx", "drools"));
        me.setOperation(new QName("smx", "process"));
        me.getInMessage().setContent(new StringSource("<test id=\"1234\" />"));
        me.setProperty(JbiConstants.CORRELATION_ID, "TEST");
        if (client.sendSync(me, 10000)) {
            assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
            String id = (String)(new JAXPStringXPathExpression("/test/@id")
                    .evaluate(null, me.getMessage(Exchange.OUT_MESSAGE)));
            assertEquals("0",id);
            client.done(me);
        } else {
            fail ("No response from drools in time...");
        }
    }

    public void testModifyXMLWithAnAttribute() throws Exception {
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
                                                                new QName("smx", "drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("XPathAttrMod-XPathAttr.drl"));
        endpoint.setAutoReply(true);

        drools.setEndpoints(new DroolsEndpoint[] {endpoint});
        jbi.activateComponent(drools, "servicemix-drools");

        ReceiverComponent target = new ReceiverComponent();
        target.setService(new QName("smx", "target"));
        target.setEndpoint("endpoint");

        jbi.activateComponent(target, "target");

        jbi.start();

        InOut me = client.createInOutExchange();
        me.setService(new QName("smx", "drools"));
        me.setOperation(new QName("smx", "process"));
        me.getInMessage().setContent(new StringSource("<test id=\"1234\"><child parentId=\"0\"/></test>"));
        me.setProperty(JbiConstants.CORRELATION_ID, "TEST");
        if (client.sendSync(me, 10000)) {
            assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
            String childId = (String)(new JAXPStringXPathExpression("/test/child/@parentId")
                    .evaluate(me, me.getMessage(Exchange.OUT_MESSAGE)));
            assertEquals("1234",childId);
            client.done(me);
        } else {
            fail ("No response from drools in time...");
        }
    }
}
