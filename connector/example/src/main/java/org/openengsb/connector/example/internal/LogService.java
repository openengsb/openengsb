/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.example.internal;


import org.openengsb.core.api.AliveState;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.ExampleDomainEvents;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogService extends AbstractOpenEngSBConnectorService implements ExampleDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogService.class);

    private String outputMode;
    private String prefix;
    private AliveState aliveState = AliveState.OFFLINE;
    private final ExampleDomainEvents domainEventInterface;

    public LogService(String instanceId, ExampleDomainEvents domainEventInterface) {
        super(instanceId);
        this.domainEventInterface = domainEventInterface;
        aliveState = AliveState.CONNECTING;
    }

    @Override
    public String doSomethingWithMessage(String message) {
        message = prefix + ": " + message;
        String level = "INFO";
        if (outputMode != null) {
            level = outputMode;
        }
        raiseEvent(message, level);
        return "LogServiceCalled with: " + message;
    }

    private void raiseEvent(String message, String level) {
        LogEvent event = new LogEvent();
        event.setMessage(message);
        event.setLevel(level);
        domainEventInterface.raiseEvent(event);
    }

    public void setOutputMode(String outputMode) {
        this.outputMode = outputMode;
        aliveState = AliveState.ONLINE;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public AliveState getAliveState() {
        return aliveState;
    }

    @Override
    public String doSomethingWithLogEvent(LogEvent event) {
        return "Called: " + event.getMessage() + " " + event.getLevel();
    }

    @Override
    public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) {
        LOGGER.info("received model with the id \"{}\" and name \"{}\"", model.getId(), model.getName());
        ExampleResponseModel response = new ExampleResponseModel();
        return response;
    }

}
