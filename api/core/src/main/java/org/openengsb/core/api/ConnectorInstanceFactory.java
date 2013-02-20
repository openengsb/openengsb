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

public interface ConnectorInstanceFactory {

    /**
     * validates the attribute combination. This is used for validating attributes before creating a new service.
     * Therefore the supplied map contains all attributes
     *
     * returns a Collection of error-messages. should return an empty map if there are no errors
     */
    Map<String, String> getValidationErrors(Map<String, String> attributes);

    /**
     * validates the attribute combination. This is used for validating attributes before updating new service.
     * Therefore the supplied map contains only the attributes that should be changed. Other attributes must be
     * retrieved from the instance directly.
     *
     * returns a Collection of error-messages. should return an empty map if there are no errors
     */
    Map<String, String> getValidationErrors(Connector instance, Map<String, String> attributes);

    /**
     * creates a new instance with the given service-id. The serviceId should then be the the same as returned by
     * {@link OpenEngSBService#getInstanceId()}
     *
     * The created instance only contains default-values that are changed later.
     */
    Connector createNewInstance(String id);

    /**
     * This method is used for filling in the attributes of a service. It can be assumed that the attributes have been
     * validated before.
     *
     * returns the reconfigured connector instance. This does not have to be the same instance that was given as
     * parameter to the method
     */
    Connector applyAttributes(Connector instance, Map<String, String> attributes);
}
