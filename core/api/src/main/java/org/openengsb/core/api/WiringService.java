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


/**
 * provides utility-methods for retrieving domain-services
 */
public interface WiringService {

    /**
     * returns the domain-service for the corresponding location in the current context. If no service with that
     * location is found in the current context, the root-context is tried.
     */
    <T extends Domain> T getDomainEndpoint(Class<T> domainType, String location);

    /**
     * returns domain-services for all domains registered at the given location in the current context and the
     * root-context. If no service is found an empty list is returned.
     */
    <T extends Domain> List<T> getDomainEndpoints(Class<T> domainType, String location);

    /**
     * returns the domain-service for the corresponding location in the given context. If no service with that location
     * is found in the given context, the root-context is tried.
     */
    <T extends Domain> T getDomainEndpoint(Class<T> domainType, String location, String context);

    /**
     * returns domain-services for all domains registered at the given location in the given context and the
     * root-context. If no service is found an empty list is returned.
     */
    <T extends Domain> List<T> getDomainEndpoints(Class<T> domainType, String location, String context);

    /**
     * returns true a connector for the specified domain type exists, otherwise false
     */
    boolean isConnectorCurrentlyPresent(Class<? extends Domain> domainType);
}
