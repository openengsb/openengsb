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

package org.openengsb.connector.virtual.filewatcher.internal;

import org.openengsb.connector.virtual.filewatcher.FileWatcherConnectorFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.VirtualConnectorProvider;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.AbstractConnectorProvider;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.util.DefaultOsgiUtilsService;

public class FileWatcherConnectorProvider extends AbstractConnectorProvider implements VirtualConnectorProvider {

    private QueryInterface queryService;

    private PersistInterface persistService;

    public FileWatcherConnectorProvider(PersistInterface persistService, QueryInterface queryService) {
        this.persistService = persistService;
        this.queryService = queryService;
    }

    @Override
    public ServiceDescriptor getDescriptor() {
        Builder builder = ServiceDescriptor.builder(strings);

        builder.id("filewatcher");
        builder.name("composite.name", "filewatcher");
        builder.description("composite.description");

        builder.attribute(builder.newAttribute().id("watchfile").name("filewatcher.watchfile.id")
            .description("composite.queryString.description").build());

        return builder.build();
    }

    @Override
    public FileWatcherConnectorFactory createFactory(DomainProvider provider) {
        return new FileWatcherConnectorFactory(provider, persistService, queryService,
                new DefaultOsgiUtilsService(bundleContext));
    }

}
