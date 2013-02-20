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

public class ConnectorProviderImpl implements ConnectorProvider {
    private String id;
    private LocalizableString name;
    private LocalizableString description;
    private ServiceDescriptor descriptor;

    public ConnectorProviderImpl() {
    }

    public ConnectorProviderImpl(String id, LocalizableString name,
            LocalizableString description, ServiceDescriptor descriptor) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.descriptor = descriptor;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public LocalizableString getName() {
        return this.name;
    }

    public void setName(LocalizableString name) {
        this.name = name;
    }

    @Override
    public LocalizableString getDescription() {
        return this.description;
    }

    public void setDescription(LocalizableString description) {
        this.description = description;
    }

    @Override
    public ServiceDescriptor getDescriptor() {
        return this.descriptor;
    }

    public void setDescriptor(ServiceDescriptor descriptor) {
        this.descriptor = descriptor;
    }

}
