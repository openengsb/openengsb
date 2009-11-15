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
package org.openengsb.issues.common.endpoints;

import java.io.StringReader;
import java.io.StringWriter;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.issues.common.IssueDomain;
import org.openengsb.issues.common.exceptions.IssueDomainException;
import org.openengsb.issues.common.messages.CreateIssueMessage;
import org.openengsb.issues.common.messages.CreateIssueResponseMessage;
import org.openengsb.issues.common.messages.CreateIssueStatus;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;
import org.openengsb.util.serialization.Serializer;

/**
 * @org.apache.xbean.XBean element="create-issue"
 */
public abstract class AbstractCreateIssueEndpoint extends ProviderEndpoint {

    private Logger log = Logger.getLogger(AbstractCreateIssueEndpoint.class);

    private Serializer serializer;

    protected abstract IssueDomain createIssueDomain() throws IssueDomainException;

    public AbstractCreateIssueEndpoint() {
        // set defaults
        this.serializer = new JibxXmlSerializer();
    }

    public void validate() throws DeploymentException {
    }

    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        CreateIssueResponseMessage responseMessage = new CreateIssueResponseMessage();

        try {
            IssueDomain domain = createIssueDomain();

            // transform message to string
            Transformer messageTransformer = TransformerFactory.newInstance().newTransformer();
            StringWriter stringWriter = new StringWriter();
            messageTransformer.transform(in.getContent(), new StreamResult(stringWriter));

            String issueId = domain.createIssue(serializer.deserialize(CreateIssueMessage.class,
                    new StringReader(stringWriter.toString())).getIssue());
            responseMessage.setCreatedIssueId(issueId);
            responseMessage.setStatus(CreateIssueStatus.SUCCESS);
            responseMessage.setStatusMessage("Issue created successfully.");
        } catch (SerializationException e) {
            log.error("Error deserializing incoming message.", e);
            responseMessage.setStatus(CreateIssueStatus.ERROR);
            responseMessage.setStatusMessage(e.getMessage());
        } catch (IssueDomainException e) {
            log.error("Error creating issue.", e);
            responseMessage.setStatus(CreateIssueStatus.ERROR);
            responseMessage.setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred.", e);
            responseMessage.setStatus(CreateIssueStatus.ERROR);
            responseMessage.setStatusMessage(e.getMessage());
        } finally {
            StringWriter sw = new StringWriter();
            serializer.serialize(responseMessage, sw);
            out.setContent(new StringSource(sw.toString()));
            getChannel().send(exchange);
        }
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

}
