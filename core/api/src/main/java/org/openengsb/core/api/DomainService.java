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

import java.util.List;

import org.osgi.framework.ServiceReference;

/**
 * Domain service helping to work with domain services
 */
public interface DomainService {

    /**
     * Method to retrieve all {@link DomainProvider} in the entire OpenEngSB
     */
    List<DomainProvider> domains();

    /**
     * Method to retrieve the {@link InternalServiceRegistrationManager} for a specific connector which can be used to
     * create, update or destroy instances of a specific type. E.g. this method can be used to retrieve the jira service
     * manager and add instances or remove them.
     */
    InternalServiceRegistrationManager serviceManagerForConnector(String connectorName);

    /**
     * Retrieves the {@link InternalServiceRegistrationManager} instances for a specific {@link Domain} for all
     * connectors available in the OpenEngSB.
     */
    List<InternalServiceRegistrationManager> serviceManagersForDomain(Class<? extends Domain> domain);

    /**
     * Retrieves the osgi {@link ServiceReference} matching all instances implementing a specific {@link Domain}
     */
    List<ServiceReference> serviceReferencesForDomain(Class<? extends Domain> domain);

    /**
     * Retrieves the {@link ServiceReference} which can be used to retrieve all instances of connectors identified by
     * implementing the base {@link Domain} interface.
     */
    List<? extends ServiceReference> getAllServiceInstances();

    /**
     * Returns the service for {@link ServiceReference}
     */
    Object getService(ServiceReference serviceReference);

    /**
     * Returns the service object for a specific service class and id.
     */
    Object getService(String serviceClass, String serviceId);
}
