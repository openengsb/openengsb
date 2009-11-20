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

package org.openengsb.issues.common;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.equalTo;

import java.io.StringReader;
import java.io.StringWriter;

import javax.annotation.Resource;
import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.issues.common.endpoints.AbstractCreateIssueEndpoint;
import org.openengsb.issues.common.exceptions.IssueDomainException;
import org.openengsb.issues.common.messages.CreateIssueMessage;
import org.openengsb.issues.common.messages.CreateIssueResponseMessage;
import org.openengsb.issues.common.messages.CreateIssueStatus;
import org.openengsb.issues.common.model.Issue;
import org.openengsb.issues.common.model.IssuePriority;
import org.openengsb.issues.common.model.IssueSeverity;
import org.openengsb.issues.common.model.IssueType;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.Serializer;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/integrationtestSpring.xml" })
public class CreateIssueEndpointIntegrationTest extends SpringTestSupport {

    private final String TEST_NAMESPACE = "urn:test";
    private final String XBEAN_XML_NAME = "spring-test-xbean-createissue.xml";
    private final String TEST_SERVICE_NAME = "ticketCreatorService";

    private Serializer serializer;
    private String summary = "Test Summary";
    private String description = "Test Description";
    private String reporter = "Test Reporter";
    private String owner = "Test Owner";
    private IssueType type = IssueType.BUG;
    private IssuePriority priority = IssuePriority.HIGH;
    private IssueSeverity severity = IssueSeverity.BLOCK;
    private String affectedVersion = "1.0";

    private String createdIssueId = "Test Issue ID 1";

    private String exceptionMessage = "Error creating ticket.";

    private IssueDomain mockedIssueDomain;

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
     * Creates and configures a new Message-Object for the Out-Only-MEP
     *
     * @param client The client used to create the empty Message-Object
     * @param service The configured entpoint's name as noted in the xbean.xml
     * @param message The actual message as xml-String
     * @return The new and configured In-Only-Message-Object
     * @throws MessagingException should something go wrong
     */
    private RobustInOnly createRobustInOnlyMessage(DefaultServiceMixClient client, String service, String message)
            throws MessagingException {
        RobustInOnly robustInOnly = client.createRobustInOnlyExchange();
        robustInOnly.setService(new QName(this.TEST_NAMESPACE, service));
        robustInOnly.getInMessage().setContent(new StringSource(message));

        return robustInOnly;
    }

    /* end creators */

    /* setup */

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockedIssueDomain = (IssueDomain) context.getBean("mockedIssueDomain");
        serializer = new JibxXmlSerializer();
    }

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
        Transformer messageTransformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        messageTransformer.transform(msg.getContent(), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    @Test
    public void validInputShouldReturnValidResponse() throws Exception {
        DefaultServiceMixClient client = createClient();

        Issue issueToCreate = new Issue(summary, description, reporter, owner, type, priority, severity,
                affectedVersion);

        CreateIssueMessage inMsg = new CreateIssueMessage(issueToCreate);

        StringWriter requestWriter = new StringWriter();
        serializer.serialize(inMsg, requestWriter);
        InOut inOut = createInOutMessage(client, this.TEST_SERVICE_NAME, requestWriter.toString());

        when(mockedIssueDomain.createIssue(any(Issue.class))).thenReturn(createdIssueId);

        client.sendSync(inOut);

        verify(mockedIssueDomain).createIssue(eq(issueToCreate));

        validateReturnMessageSuccess(inOut);

        CreateIssueResponseMessage outMsg = serializer.deserialize(CreateIssueResponseMessage.class, new StringReader(
                transformMessageToString(inOut.getOutMessage())));

        assertEquals(CreateIssueStatus.SUCCESS, outMsg.getStatus());
        assertEquals("Issue created successfully.", outMsg.getStatusMessage());
        assertEquals(createdIssueId, outMsg.getCreatedIssueId());
    }

    @Test
    public void errorInIssueDomainShouldReturnErrorResponse() throws Exception {
        DefaultServiceMixClient client = createClient();

        Issue issueToCreate = new Issue(summary, description, reporter, owner, type, priority, severity,
                affectedVersion);

        CreateIssueMessage inMsg = new CreateIssueMessage(issueToCreate);

        StringWriter requestWriter = new StringWriter();
        serializer.serialize(inMsg, requestWriter);
        InOut inOut = createInOutMessage(client, this.TEST_SERVICE_NAME, requestWriter.toString());

        when(mockedIssueDomain.createIssue(any(Issue.class))).thenThrow(new IssueDomainException(exceptionMessage));

        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);

        CreateIssueResponseMessage outMsg = serializer.deserialize(CreateIssueResponseMessage.class, new StringReader(
                transformMessageToString(inOut.getOutMessage())));

        assertEquals(CreateIssueStatus.ERROR, outMsg.getStatus());
        assertEquals(exceptionMessage, outMsg.getStatusMessage());
        assertNull(outMsg.getCreatedIssueId());
    }

    @Test
    public void invalidInMessageShouldReturnErrorResponse() throws Exception {
        DefaultServiceMixClient client = createClient();

        InOut inOut = createInOutMessage(client, this.TEST_SERVICE_NAME, "<invalidInMessage />");

        client.sendSync(inOut);

        validateReturnMessageSuccess(inOut);

        CreateIssueResponseMessage outMsg = serializer.deserialize(CreateIssueResponseMessage.class, new StringReader(
                transformMessageToString(inOut.getOutMessage())));

        assertEquals(CreateIssueStatus.ERROR, outMsg.getStatus());
        assertEquals("Error deserializing from reader.", outMsg.getStatusMessage());
        assertNull(outMsg.getCreatedIssueId());
    }

}
