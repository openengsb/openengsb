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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.drools.RuleBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.model.Event;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.drools.helper.XmlHelper;
import org.openengsb.drools.message.ManageRequest;
import org.openengsb.drools.message.RuleBaseElement;
import org.openengsb.util.IO;
import org.openengsb.util.serialization.SerializationException;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:testBeans.xml" })
public class DroolsEndpointDirTest extends SpringTestSupport {
    private static ServiceMixClient client;

    private final String TEST_EVENT = getTestEvent();

    private RuleBase ruleBase;

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("testXBeanDir.xml");
    }

    @Override
    @Before
    public void setUp() throws Exception {
        File rbDir = new File("data/rulebase");
        rbDir.mkdirs();
        copyAllFiles(new File("src/test/resources/rulebase"), "data/rulebase/");
        super.setUp();

        client = new DefaultServiceMixClient(this.jbi);
        DroolsComponent comp = (DroolsComponent) jbi.getActivationSpecs()[0].getComponent();
        DroolsEndpoint ep = (DroolsEndpoint) comp.getServiceUnit().getEndpoints().iterator().next();
        ruleBase = ep.getRuleBase();

    }

    private void copyAllFiles(File srcDir, String dest) throws IOException {
        for (File f : srcDir.listFiles()) {
            IO.copyFile(f, new File(dest + "/" + f.getName()));
        }
    }

    @After
    @Override
    public void tearDown() throws Exception {
        IO.deleteStructure(new File("data"));
        super.tearDown();
    }

    @Test
    public void testCreateMessage() throws Exception {
        assertNull(ruleBase.getPackage("org.openengsb").getRule("test"));
        InOnly inout = sendInOnlyMessage(new QName("create"), getCreateContent());
        assertNotSame("Exchange was not processed correctly", ExchangeStatus.ERROR, inout.getStatus());
        assertNotNull(ruleBase.getPackage("org.openengsb").getRule("test"));
    }

    private InOnly sendInOnlyMessage(QName operation, Source content) throws MessagingException, JAXBException {
        InOnly inonly = client.createInOnlyExchange();
        inonly.setService(new QName("urn:test", "drools"));
        inonly.setOperation(operation);
        inonly.getInMessage().setContent(content);
        client.sendSync(inonly);
        return inonly;
    }

    private InOut sendInOutMessage(QName operation, Source content) throws MessagingException, JAXBException {
        InOut inout = client.createInOutExchange();
        inout.setService(new QName("urn:test", "drools"));
        inout.setOperation(operation);
        inout.getInMessage().setContent(content);
        client.sendSync(inout);
        return inout;
    }

    @Test
    public void testList() throws Exception {
        ManageRequest req = new ManageRequest();
        req.setElementType(RuleBaseElement.Rule);
        StringWriter sw = new StringWriter();
        XmlHelper.marshal(req, sw);
        Source content = new StringSource(sw.toString());
        InOut inout = sendInOutMessage(new QName("list"), content);
        assertNotNull(inout.getOutMessage().getContent());
    }

    private Source getCreateContent() throws JAXBException {
        ManageRequest req = new ManageRequest();
        req.setElementType(RuleBaseElement.Rule);
        req.setName("test");
        req.setCode("when\n then\n System.out.println(\"bla\");");

        StringWriter sw = new StringWriter();
        XmlHelper.marshal(req, sw);
        return new StringSource(sw.toString());
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
