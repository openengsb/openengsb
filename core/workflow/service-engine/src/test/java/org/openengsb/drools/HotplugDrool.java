/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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

import java.util.HashMap;

import javax.jbi.messaging.InOnly;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.model.Event;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class HotplugDrool {

    private JBIContainer jbi;
    private DroolsComponent drools;
    private ServiceMixClient client;

    @Before
    public void setUp() throws Exception {
        jbi = new JBIContainer();
        jbi.setEmbedded(true);
        jbi.init();
        client = new DefaultServiceMixClient(jbi);

        drools = new DroolsComponent();
        DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(), new QName("drools"), "endpoint");
        endpoint.setGlobals(new HashMap<String, Object>());
        drools.setEndpoints(new DroolsEndpoint[] { endpoint });
        jbi.activateComponent(drools, "openengsb-drools");

        jbi.start();

    }

    @After
    public void tearDown() throws Exception {
        jbi.shutDown();
    }

    @Test
    public void testGuvnorConnect() throws Exception {
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource(getEvent()));
        client.sendSync(me);
    }

    private String getEvent() {
        try {
            Event event = new Event("domain", "name");
            event.setValue("foo", 42);
            return Transformer.toXml(event);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGuvnorAction() throws Exception {
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("drools"));
        me.getInMessage().setContent(new StringSource(getEvent()));
        client.sendSync(me);
    }

}
