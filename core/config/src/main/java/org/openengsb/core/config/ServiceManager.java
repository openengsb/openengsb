/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.core.config;

import java.util.Locale;
import java.util.Map;

import org.openengsb.core.config.descriptor.ServiceDescriptor;

/**
 * Instance provider for a specific service interface.
 */
public interface ServiceManager {

    /**
     * Returns the {@code ServiceDescriptor} describing the managed service.
     * Localizable text is localized using the system locale.
     */
    ServiceDescriptor getDescriptor();

    /**
     * Return the {@code ServiceDescriptor} describing the managed service.
     * Localizable text is localized using the given {@code locale}.
     */
    ServiceDescriptor getDescriptor(Locale locale);

    /**
     * Creates or updates a service instance. If the given id does not exist,
     * this creates a new service instance.
     *
     * @param id identifier for a new or already existing service instance.
     * @param attributes updates to maybe already set attributes.
     */
    void update(String id, Map<String, String> attributes);

    /**
     * Deletes the service instanced with the given {@code id}.
     * 
     * @param id identifier for a service instance provided by this
     *        {@code ServiceManager}.
     * 
     * @throws IllegalArgumentException if no instance exists for the given id.
     */
    void delete(String id);
}
