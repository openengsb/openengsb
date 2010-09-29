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

import java.util.List;

import org.openengsb.core.common.l10n.LocalizableString;

/**
 * Provide necessary information about an OpenEngSB domain. Each domain in the OpenEngSB has to create an implementation
 * of this interface and publish it to provide necessary information to the configuration service.
 */
public interface DomainProvider {

    /**
     * Returns the global identifier for this domain. This should be either the package name of this domain, or the full
     * class name of the provided Domain interface.
     */
    String getId();

    /**
     * Returns the localizable name.
     */
    LocalizableString getName();

    /**
     * Returns the localizable description.
     */
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
