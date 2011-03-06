/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.jira_soapclient.internal;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.connector.jira.internal.models.xmlrpc.JiraProxyFactory;
import org.openengsb.connector.jira.internal.models.xmlrpc.JiraRpcConverter;
import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domain.issue.IssueDomain;

public class JiraSoapServiceInstanceFactory implements ServiceInstanceFactory<IssueDomain, SOAPClient> {

    @Override
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder) {
        builder.name("service.name").description("service.description");

        builder.attribute(builder.newAttribute().id("jira.user").name("jira.user.name")
                .description("jira.user.description").build());
        builder.attribute(builder.newAttribute().id("jira.password").name("jira.password.name")
                .description("jira.password.description").defaultValue("").asPassword().build());
        builder.attribute(builder.newAttribute().id("jira.project").name("jira.project.name")
                .description("jira.project.description").defaultValue("").required().build());
        builder.attribute(builder.newAttribute().id("jira.uri").name("jira.uri.name")
                .description("jira.uri.description").defaultValue("").required().build());

        return builder.build();
    }

    @Override
    public void updateServiceInstance(SOAPClient instance, Map<String, String> attributes) {
        instance.setJiraUser(attributes.get("jira.user"));
        instance.setJiraPassword(attributes.get("jira.password"));

        instance.getProxyFactory().setJiraURI(attributes.get("jira.uri"));
        instance.getRpcConverter().setJiraProject(attributes.get("jira.project"));
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(SOAPClient instance, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public SOAPClient createServiceInstance(String id, Map<String, String> attributes) {
        JiraSoapProxyFactory proxyFactory = new JiraSoapProxyFactory(attributes.get("jira.uri"));
        SOAPClient jiraConnector = new SOAPClient(id);

        updateServiceInstance(jiraConnector, attributes);
        return jiraConnector;
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

}
