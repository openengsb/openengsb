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

public interface ServiceInstanceFactory {

    void validate(Map<String, String> attributes) throws ServiceValidationFailedException;

    void validate(Domain instance, Map<String, String> attributes) throws ServiceValidationFailedException;

    Domain createNewInstance(String id);

    void applyAttributes(Domain instance, Map<String, String> attributes);
    //
    // /**
    // * Updates the attributes of the given service instance. Before the changes are applied they may be validated. The
    // * attributes may only contain changed values and omit previously set attributes.
    // *
    // * @throws ServiceValidationFailedException if the attributes fail validation
    // */
    // void updateServiceInstance
    // (Domain instance, Map<String, String> attributes) throws ServiceValidationFailedException;
    //
    // /**
    // * Updates the attributes of the given service instance. The difference to
    // * {@link #updateServiceInstance(Domain, Map)} method is, that the attribute combination is not validated by the
    // * implementation
    // */
    // void forceUpdateServiceInstance(Domain instance, Map<String, String> attributes);
    //
    // /**
    // * creates a new service instance with the given attributes.
    // *
    // * @throws ServiceValidationFailedException if the attributes fail validation
    // */
    // Domain createServiceInstance(String id, Map<String, String> attributes) throws ServiceValidationFailedException;
    //
    // /**
    // * creates a new service instance without validating the attributes first.
    // *
    // * @param id
    // * @param attributes
    // * @return
    // */
    // Domain forceCreateServiceInstance(String id, Map<String, String> attributes);

}
