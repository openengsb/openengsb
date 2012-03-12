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

/**
 * Container for the internal OSGi constants used in the OpenEngSB. This is a lookup as well as when direct coding or
 * filter creating against those constants is required.
 */
public final class Constants {
    /**
     * ID used to identfy backend storages. Backend ID's are used in configuration files as well as on services
     * implementing the ConfigBackendService for a specific purpose.
     */
    public static final String BACKEND_ID = "backend.id";

    /**
     * The {@link #CONFIGURATION_ID} is used to map specific areas like RULES or CONNECTORS to {@link #BACKEND_ID}
     * backends. Retrieving a configuration service using this ID will provide the required persistence service.
     */
    public static final String CONFIGURATION_ID = "configuration.id";

    /**
     * The connector constants gives a connector a unique identification. The exacty semantic value is identified by the
     * conenctor itself. Possible values are git, trac, jira, ...
     */
    public static final String CONFIG_CONNECTOR = "CONNECTOR";

    /**
     * represents the property-key for type of connector in osgi-service-properties
     */
    public static final String CONNECTOR_KEY = "connector";

    /**
     * represents the property-key for type of domain in osgi-service-properties
     */
    public static final String DOMAIN_KEY = "domain";

    /**
     * represents the property-key for a connector's instanceid in osgi-service-properties
     */
    public static final String ID_KEY = "id";

    /**
     * connectors of this type require special handling. They represent proxies to connector-services running outside
     * the OpenEngSB and can represent any domain.
     */
    public static final String EXTERNAL_CONNECTOR_PROXY = "external-connector-proxy";

    /**
     * Header element in MANIFEST that states which classes may be loaded using Classloader delegation
     */
    public static final String PROVIDED_CLASSES = "Provided-Classes";

    /**
     * Header element in MANIFEST that states which super-classes are used for registring and categorizing Classloader
     * delegation
     */
    public static final String PROVIDED_CLASSES_PARENTS = "Provided-ClassesParents";

    /**
     * Element in service properties of Classloader-delegation-services that states which classes are supported for
     * loading
     */
    public static final String PROVIDED_CLASSES_KEY = "providedClass";

    /**
     * Element in service properties of Classloader-delegation-services that states which category (by supertype) of
     * classes this service allows to load
     */
    public static final String PROVIDED_CLASSES_PARENTS_KEY = "providedClassParent";

    private Constants() {
        // this class should not be instanciated.
    }

}
