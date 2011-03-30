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

package org.openengsb.connector.maven.internal;

import java.util.Collections;
import java.util.Map;

import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.api.validation.MultipleAttributeValidationResult;
import org.openengsb.core.api.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.test.TestDomainEvents;

public class MavenServiceInstanceFactory implements ServiceInstanceFactory<MavenDomain, MavenServiceImpl> {

    private BuildDomainEvents buildEvents;

    private TestDomainEvents testEvents;

    private DeployDomainEvents deployEvents;

    private ContextCurrentService contextService;

    public MavenServiceInstanceFactory() {
    }

    @Override
    public void updateServiceInstance(MavenServiceImpl instance, Map<String, String> attributes) {
        if (attributes.containsKey("projectPath")) {
            instance.setProjectPath(attributes.get("projectPath"));
        }
        if (attributes.containsKey("command")) {
            instance.setCommand(attributes.get("command"));
        }
    }

    @Override
    public MavenServiceImpl createServiceInstance(String id, Map<String, String> attributes) {
        MavenServiceImpl service = new MavenServiceImpl(id);
        service.setBuildEvents(buildEvents);
        service.setTestEvents(testEvents);
        service.setDeployEvents(deployEvents);
        service.setContextService(contextService);
        updateServiceInstance(service, attributes);
        return service;
    }

    @Override
    public ServiceDescriptor getDescriptor(Builder builder) {
        builder.name("service.name").description("service.description");
        builder.attribute(builder.newAttribute().id("projectPath").name("service.projectPath.name")
            .description("service.projectPath.description").required().build());
        builder.attribute(builder.newAttribute().id("command").name("service.command.name")
            .description("service.command.description").required().build());
        return builder.build();
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(MavenServiceImpl instance, Map<String, String> attr) {
        Map<String, String> emptyMap = Collections.emptyMap();
        return new MultipleAttributeValidationResultImpl(true, emptyMap);
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        Map<String, String> emptyMap = Collections.emptyMap();
        return new MultipleAttributeValidationResultImpl(true, emptyMap);
    }

    public void setBuildEvents(BuildDomainEvents buildEvents) {
        this.buildEvents = buildEvents;
    }

    public void setTestEvents(TestDomainEvents testEvents) {
        this.testEvents = testEvents;
    }

    public void setDeployEvents(DeployDomainEvents deployEvents) {
        this.deployEvents = deployEvents;
    }

    public void setContextService(ContextCurrentService contextService) {
        this.contextService = contextService;
    }
}
