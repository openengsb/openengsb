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

import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.LocalizableString;

/**
 * Provides metadata describing a connector implementation.
 *
 * Each connector-implementation needs to export a service of this type, so it can be found by management services and
 * interface.
 */
public interface ConnectorProvider {

    /**
     * unique short human readable id to identify the connector-type (e.g. "git")
     */
    String getId();

    /**
     * The full name of the connector. This String is localized. It is recommended to use
     * {@link org.openengsb.core.api.l10n.BundleStrings} and bundle-properties to achieve this.
     */
    LocalizableString getName();

    /**
     * A description of the connector implementation. This String is localized. It is recommended to use
     * {@link org.openengsb.core.api.l10n.BundleStrings} and bundle-properties to achieve this.
     */
    LocalizableString getDescription();

    /**
     * A service-descriptor for determining what attributes this connector-implementation uses.
     */
    ServiceDescriptor getDescriptor();

}
