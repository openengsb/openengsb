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

package org.openengsb.core.common;

import java.util.Map;

import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;

public interface ServiceInstanceFactory<DomainType extends Domain, InstanceType extends DomainType> {

    /**
     * Called when the {@link #ServiceDescriptor} for the provided service is needed.
     *
     * The {@code builder} already has the id, service type and implementation type set to defaults.
     */
    ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder);

    /**
     * Called by the {@link AbstractServiceManager} when updated service attributes for an instance are available. The
     * attributes may only contain changed values and omit previously set attributes.
     *
     * @param instance the instance to update
     * @param attributes the new service settings
     */
    void updateServiceInstance(InstanceType instance, Map<String, String> attributes);

    /**
     * Validates if the service is correct before updating.
     */
    MultipleAttributeValidationResult updateValidation(InstanceType instance, Map<String, String> attributes);

    /**
     * The {@link AbstractServiceManager} calls this method each time a new service instance has to be started.
     *
     * @param id the unique id this service has been assigned.
     * @param attributes the initial service settings
     */
    InstanceType createServiceInstance(String id, Map<String, String> attributes);

    /**
     * Validates if the attributes are correct before creation.
     */
    MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes);

}
