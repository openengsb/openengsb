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

package org.openengsb.core.common;

import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.l10n.BundleStrings;
import org.openengsb.core.api.l10n.LocalizableString;
import org.osgi.framework.BundleContext;

/**
 * Base class for {@code ConnectorProvider} implementations with the following functionality:
 * <ul>
 * <li>id is a unique human readable name of the connector-type (e.g. "git")</li>
 * <li>name is looked up through localized {@code BundleStrings.getString("connector.name")}</li>
 * <li>description is looked up through localized {@code BundleStrings.getString("connector.description")}</li>
 * </ul>
 */
public abstract class AbstractConnectorProvider implements ConnectorProvider {

    protected BundleStrings strings;
    protected String id;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public LocalizableString getName() {
        return strings.getString("connector.name");
    }

    @Override
    public LocalizableString getDescription() {
        return strings.getString("connector.description");
    }

    /**
     * It is important to set the bundlecontext here so that the localized strings are found
     */
    public void setBundleContext(BundleContext bundleContext) {
        this.strings = new BundleStrings(bundleContext.getBundle());
    }

}
