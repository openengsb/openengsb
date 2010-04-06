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
package org.openengsb.drools.tool;

import java.util.HashMap;
import java.util.Properties;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.drools.RuleBase;
import org.drools.agent.RuleAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.drools.DroolsComponent;
import org.openengsb.drools.DroolsEndpoint;

/*
 * NOTE: Guvnor must be running and, holding the correctly setup rulebase, for these tests to run
 * Further, the jboss-server Guvnor is running in, must listen to port 8081 instead of 8080
 * (because servicemix occupies 8080)
 */
@Ignore("Requires a running guvnor-server with the correct setup")
public class GuvnorTest {

    private JBIContainer jbi;
    private DroolsComponent drools;
    private ServiceMixClient client;
    private static final String URL = "http://localhost:8081/drools-guvnor/org.drools.guvnor.Guvnor/package/org.openengsb/LATEST";

    @Before
    public void setUp() throws Exception {
        jbi = new JBIContainer();
        jbi.setEmbedded(true);
        jbi.init();
        client = new DefaultServiceMixClient(jbi);

        drools = new DroolsComponent();
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(), new QName("drools"), "endpoint");
        Properties config = new Properties();
        config.put("url", URL);
        RuleAgent agent = RuleAgent.newRuleAgent(config);
        RuleBase ruleBase = agent.getRuleBase();

        endpoint.setRuleBase(ruleBase);
        endpoint.setGlobals(new HashMap<String, Object>());
        drools.addCustomEndpoint(endpoint);
        jbi.activateComponent(drools, "openengsb-drools");

        jbi.start();
    }

    @After
    public void tearDown() throws Exception {
        jbi.shutDown();
    }

    @Test
    public void testGuvnorConnect() throws Exception {

        InOut me = client.createInOutExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<event><name>hello</name></event>"));
        me.getInMessage().setProperty("prop", Boolean.TRUE);
        client.sendSync(me, 1000);

        // Source answer = client.receive(1000).getMessage("out").getContent();
        // System.out.println("answer" + answer);
    }

    @Test
    public void testGuvnorAction() throws Exception {
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource("<event><name>checkout</name></event>"));
        me.getInMessage().setProperty("prop", Boolean.TRUE);
        client.sendSync(me, 1000);
    }

    @Test
    public void testBuildBreak() throws Exception {
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage()
                .setContent(new StringSource("<event><name>buildbreak</name><contextId>5</contextId></event>"));
        me.getInMessage().setProperty("prop", Boolean.TRUE);
        client.sendSync(me, 1000);
    }

}
