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

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.transaction.NotSupportedException;

import org.apache.camel.converter.jaxp.StringSource;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.openengsb.issues.common.IssueDomain;
import org.openengsb.issues.common.model.Issue;

/**
 * @org.apache.xbean.XBean element="create-issue"
 */
public abstract class AbstractCreateIssueEndpoint extends ProviderEndpoint {

    protected abstract IssueDomain createIssueDomain();

    protected Serializer getSerializer() {
        throw new NotSupportedException();
    }

    public void validate() throws DeploymentException {
    }

    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        Serializer
        
        IssueDomain domain = createIssueDomain();
        String issueId = domain.createIssue(getSerializer().deserialize(Issue.class,
                new StringReader(in.getContent().toString())));

        // TODO do not create xml response this way (use serializer instead)
        out.setContent(new StringSource(String.format("<createdIssueId>%s</createdIssueId>", issueId)));
        getChannel().send(exchange);
    }
}
