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

package org.openengsb.connector.jira.internal;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.api.validation.MultipleAttributeValidationResult;
import org.openengsb.core.api.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domain.issue.IssueDomain;

public class JiraServiceInstanceFactory implements ServiceInstanceFactory<IssueDomain, JiraService> {

    @Override
    public ServiceDescriptor getDescriptor(Builder builder) {
        builder.name("service.name").description("service.description");

        builder.attribute(
            builder.newAttribute().id("jira.user").name("jira.user.name").description("jira.user.description").build());
        builder.attribute(builder.newAttribute().id("jira.password").name("jira.password.name")
            .description("jira.password.description").defaultValue("").asPassword().build());
        builder.attribute(
            builder.newAttribute().id("jira.project").name("jira.project.name").description("jira.project.description")
                .defaultValue("").required().build());
        builder.attribute(
            builder.newAttribute().id("jira.uri").name("jira.uri.name").description("jira.uri.description")
                .defaultValue("").required().build());

        return builder.build();
    }

    @Override
    public void updateServiceInstance(JiraService instance, Map<String, String> attributes) {
        instance.setJiraUser(attributes.get("jira.user"));
        instance.setJiraPassword(attributes.get("jira.password"));

        instance.getSoapSession().setJiraURI(attributes.get("jira.uri"));
        instance.setProjectKey(attributes.get("jira.project"));
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(JiraService instance, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public JiraService createServiceInstance(String id, Map<String, String> attributes) {
        JiraSOAPSession jiraSoapSession = new JiraSOAPSession(attributes.get("jira.uri"));
        JiraService jiraConnector = new JiraService(id, jiraSoapSession, attributes.get("jira.project"));
        jiraConnector.setJiraUser(attributes.get("jira.user"));
        jiraConnector.setJiraPassword(attributes.get("jira.password"));
        updateServiceInstance(jiraConnector, attributes);
        return jiraConnector;
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

}
