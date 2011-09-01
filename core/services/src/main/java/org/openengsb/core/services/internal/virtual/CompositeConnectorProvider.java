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

package org.openengsb.core.services.internal.virtual;

import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.VirtualConnectorProvider;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.AbstractConnectorProvider;

public class CompositeConnectorProvider extends AbstractConnectorProvider implements VirtualConnectorProvider {

    @Override
    public ServiceDescriptor getDescriptor() {
        Builder builder = ServiceDescriptor.builder(strings);

        builder.id("connector-composition");
        builder.name("composite.name", "[proxy-name]");
        builder.description("composite.description");

        builder.attribute(builder.newAttribute().id("queryString").name("composite.queryString.id")
            .description("composite.queryString.description").build());
        builder.attribute(builder.newAttribute().id("compositeStrategy").name("composite.strategy.id")
            .description("composite.strategy.description").build());

        return builder.build();
    }

    @Override
    public ConnectorInstanceFactory createFactory(DomainProvider provider) {
        return new CompositeConnectorFactory(provider);
    }

}
