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

import java.util.Map;

import org.openengsb.core.api.remote.ProxyConnectorRegistry;

import com.google.common.collect.Maps;

public class ProxyConnectorRegistryImpl implements ProxyConnectorRegistry {

    private Map<String, ProxyRegistration> registrations = Maps.newConcurrentMap();

    @Override
    public synchronized void registerConnector(String uuid, String portId, String destination) {
        if (!registrations.containsKey(uuid)) {
            throw new IllegalArgumentException("Unknown connector " + uuid);
        }
        ProxyRegistration proxyRegistration = registrations.get(uuid);
        proxyRegistration.setPortId(portId);
        proxyRegistration.setDestination(destination);
        return;
    }

    @Override
    public synchronized void unregisterConnector(String uuid) {
        if (!registrations.containsKey(uuid)) {
            throw new IllegalArgumentException("Unknown connector " + uuid);
        }
        ProxyRegistration proxyRegistration = registrations.get(uuid);
        proxyRegistration.setDestination(null);
        proxyRegistration.setPortId(null);
    }

    public ProxyRegistration create(String id) {
        ProxyRegistration proxyRegistration = new ProxyRegistration(id);
        registrations.put(id, proxyRegistration);
        return proxyRegistration;
    }

}
