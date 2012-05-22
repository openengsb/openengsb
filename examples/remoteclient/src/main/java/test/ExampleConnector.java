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

package test;


import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExampleConnector implements ExampleDomain, Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleConnector.class);

    private String connectorId;
    private String domainId;

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public String getInstanceId() {
        return "example-remote";
    }

    @Override
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }

    @Override
    public String doSomethingWithMessage(String message) {
        LOGGER.info(message);
        return message;
    }

    @Override
    public String doSomethingWithEnum(ExampleEnum exampleEnum) {
        LOGGER.info("ExampleEnum: {}", exampleEnum);
        return exampleEnum.toString();
    }

    @Override
    public String doSomethingWithLogEvent(LogEvent event) {
        LOGGER.info("LogEvent: {}", event);
        return event.toString();
    }

    @Override
    public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) {
        LOGGER.info("RequestModelEvent: {}", model);
        ExampleResponseModel response = ModelUtils.createEmptyModelObject(ExampleResponseModel.class);
        response.setResult("success");
        return response;
    }


}
