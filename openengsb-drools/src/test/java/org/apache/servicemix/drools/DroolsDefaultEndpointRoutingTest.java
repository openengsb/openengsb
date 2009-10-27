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

import javax.jbi.messaging.InOnly;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.ReceiverComponent;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

/**
 * Tests for {@link DroolsEndpoint} configured with a default service
 */
public class DroolsDefaultEndpointRoutingTest extends TestCase {
    
    private JBIContainer jbi;
    private DroolsComponent drools;
    private ServiceMixClient client;
    
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
    
    public void testRouteToDefaultService() throws Exception {
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(), new QName("smx", "drools"), "endpoint");
        endpoint.setRuleBaseResource(new ClassPathResource("router.drl"));
        endpoint.setDefaultTargetService(new QName("smx", "default-service"));

        drools.setEndpoints(new DroolsEndpoint[] {endpoint});
        jbi.activateComponent(drools, "servicemix-drools");
        
        ReceiverComponent target = new ReceiverComponent();
        target.setService(new QName("smx", "default-service"));
        target.setEndpoint("endpoint");
        
        jbi.activateComponent(target, "target");

        jbi.start();

        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("smx", "drools"));
        String inMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test/>";
        me.getInMessage().setContent(new StringSource(inMessage));
        if (client.sendSync(me,10000)) {
            // check if the message ended up in the default service endpoint
            assertEquals(1, target.getMessageList().getMessageCount());
        } else {
            fail ("No response from drools in time...");
        }

        Thread.sleep(50);
    }
}
