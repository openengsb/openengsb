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

package org.openengsb.issues.trac.test.unit;

import java.io.StringReader;
import java.io.StringWriter;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.issues.common.messages.CreateIssueMessage;
import org.openengsb.issues.common.messages.CreateIssueResponseMessage;
import org.openengsb.issues.common.model.Issue;
import org.openengsb.issues.common.model.IssuePriority;
import org.openengsb.issues.common.model.IssueSeverity;
import org.openengsb.issues.common.model.IssueType;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.Serializer;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring.xml" })
public class TracEndpointIntegrationTest extends SpringTestSupport {

    private final String TEST_NAMESPACE = "urn:test";
    private final String XBEAN_XML_NAME = "spring-test-xbean-createissue.xml";
    private final String TEST_SERVICE_NAME = "tracTicketCreatorService";

    private Serializer serializer;

    private String summary = "Test Summary";
    private String description = "Test Description";
    private String reporter = "Test Reporter";
    private String owner = "Test Owner";
    private IssueType type = IssueType.BUG;
    private IssuePriority priority = IssuePriority.HIGH;
    private IssueSeverity severity = IssueSeverity.BLOCK;
    private String affectedVersion = "1.0";

    /* creators */

    /**
     * Creates a new ServiceMixClieant
     * 
     * @return The new ServiceMixClient
     */
    private DefaultServiceMixClient createClient() throws JBIException {
        return new DefaultServiceMixClient(this.jbi);
    }

    /**
     * Creates and configures a new Message-Object for the In-Out-MEP
     * 
     * @param client The client used to create the empty Message-Object
     * @param service The configured entpoint's name as noted in the xbean.xml
     * @param message The actual message as xml-String
     * @return The new and configured InOut-Message-Object
     * @throws MessagingException should something go wrong
     */
    private InOut createInOutMessage(DefaultServiceMixClient client, String service, String message)
            throws MessagingException {
        InOut inOut = client.createInOutExchange();
        inOut.setService(new QName(this.TEST_NAMESPACE, service));
        inOut.getInMessage().setContent(new StringSource(message));

        return inOut;
    }

    /**
     * Called before each test. Performs basic JUnit setup Don't get confused by
     * the name, this is actually JUnit 4 ;)
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.serializer = new JibxXmlSerializer();
    }

    /**
     * Called after each test. Don't get confused by the name, this is actually
     * JUnit 4
     */
    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /* end setup */

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(this.XBEAN_XML_NAME);
    }

    /**
     * Checks the message for errors and either fails or throws an Exception
     * 
     * @param message
     * @throws Exception
     */
    private void validateReturnMessageSuccess(MessageExchange message) throws Exception {
        if (message.getStatus() == ExchangeStatus.ERROR) {
            if (message.getError() != null) {
                throw message.getError();
            } else {
                fail("Received ERROR status");
            }
        } else if (message.getFault() != null) {
            fail("Received fault: " + new SourceTransformer().toString(message.getFault().getContent()));
        }
    }

    /**
     * Transforms a NormalizedMessage to its String representation.
     * 
     * @param msg Message to be transformed to a String
     * @return String representation of the given NormalizedMessage
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    private String transformMessageToString(NormalizedMessage msg) throws TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {
        javax.xml.transform.Transformer messageTransformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        messageTransformer.transform(msg.getContent(), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    @Ignore(value = "This test is meant only for manual verification that the endpoint communicates with a running "
            + "trac instance correctly. It requires a running trac instance which can be configured in the file "
            + "spring-test-xbean-createissue.xml")
    @Test
    public void validInputShouldReturnValidResponse() throws Exception {
        DefaultServiceMixClient client = createClient();
        CreateIssueMessage inMsg = new CreateIssueMessage(new Issue(summary, description, reporter, owner, type,
                priority, severity, affectedVersion));

        StringWriter sw = new StringWriter();
        serializer.serialize(inMsg, sw);
        InOut inOut = createInOutMessage(client, this.TEST_SERVICE_NAME, sw.toString());

        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);
        CreateIssueResponseMessage outMsg = serializer.deserialize(CreateIssueResponseMessage.class, new StringReader(
                transformMessageToString(inOut.getOutMessage())));

        System.out.println(String.format("Result: %s, %s, %s", outMsg.getStatus(), outMsg.getStatusMessage(), outMsg
                .getCreatedIssueId()));
    }

    @Test
    @Ignore("Tests synchronous calls to trac. Needs a running trac instance.")
    public void testMethodCall() throws Exception {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    MethodCall call = new MethodCall("createIssue", new Object[] { "test" },
                            new Class<?>[] { String.class });
                    String xml = Transformer.toXml(call);

                    DefaultServiceMixClient client = createClient();

                    InOut inOut = createInOutMessage(client, TEST_SERVICE_NAME, xml);
                    inOut.setOperation(new QName("methodcall"));

                    client.sendSync(inOut);

                    validateReturnMessageSuccess(inOut);

                    String outXml = new SourceTransformer().toString(inOut.getOutMessage().getContent());
                    ReturnValue returnValue = Transformer.toReturnValue(outXml);

                    System.out.println("RETURN VALUE IS: " + returnValue.getValue());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Thread[] t = new Thread[5];

        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(run);
            t[i].start();
        }

        for (int i = 0; i < t.length; i++) {
            t[i].join();
        }
    }

}
