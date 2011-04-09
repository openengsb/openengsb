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

package org.openengsb.core.api;

import java.util.Map;

import org.openengsb.core.api.descriptor.ServiceDescriptor;

public interface ServiceInstanceFactory {

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
    void updateServiceInstance(Domain instance, Map<String, String> attributes);

    /**
     * The {@link AbstractServiceManager} calls this method each time a new service instance has to be started.
     *
     * @param id the unique id this service has been assigned.
     * @param attributes the initial service settings
     */
    Domain createServiceInstance(String id, Map<String, String> attributes);

}
