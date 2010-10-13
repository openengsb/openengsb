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

package org.openengsb.domains.scm.git.internal;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domains.scm.ScmDomain;

public class GitServiceInstanceFactory implements ServiceInstanceFactory<ScmDomain, GitServiceImpl> {

    public GitServiceInstanceFactory() {
    }

    @Override
    public void updateServiceInstance(GitServiceImpl instance, Map<String, String> attributes) {
        if (attributes.containsKey("repository")) {
            instance.setRemoteLocation(attributes.get("repository"));
        }
        if (attributes.containsKey("workspace")) {
            instance.setLocalWorkspace(attributes.get("workspace"));
        }
        if (attributes.containsKey("branch")) {
            instance.setWatchBranch(attributes.get("branch"));
        }
    }

    @Override
    public GitServiceImpl createServiceInstance(String id, Map<String, String> attributes) {
        GitServiceImpl service = new GitServiceImpl();
        updateServiceInstance(service, attributes);
        return service;
    }

    @Override
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder) {
        builder.name("service.name").description("service.description");
        builder.attribute(builder.newAttribute().id("repository").name("service.repository.name")
            .description("service.repository.description").build());
        builder.attribute(builder.newAttribute().id("workspace").name("service.workspace.name")
            .description("service.workspace.description").build());
        builder.attribute(builder.newAttribute().id("branch").name("service.branch.name")
            .description("service.branch.description").build());
        return builder.build();
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(
            GitServiceImpl instance, Map<String, String> attributes) {
        updateServiceInstance(instance, attributes);
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id,
            Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }
}
