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

import org.openengsb.core.api.l10n.LocalizableString;
import org.openengsb.core.api.security.Public;

/**
 * Provide necessary information about an OpenEngSB domain. Each domain in the OpenEngSB has to create an implementation
 * of this interface and publish it to provide necessary information to the configuration service.
 */
public interface DomainProvider {

    /**
     * Returns the global identifier for this domain. This should be a short and unique human-readable id (e.g. "scm")
     */
    @Public
    String getId();

    /**
     * Returns the localizable name.
     */
    @Public
    LocalizableString getName();

    /**
     * Returns the localizable description.
     */
    @Public
    LocalizableString getDescription();

    /**
     * Returns the domain-specific interface.
     */
    Class<? extends Domain> getDomainInterface();

    /**
     * Returns the domain-specific event related interface.
     */
    Class<? extends DomainEvents> getDomainEventInterface();

    /**
     * Returns a list of domain-specific events, as they are defined in the event interface
     */
    List<Class<? extends Event>> getEvents();
}
