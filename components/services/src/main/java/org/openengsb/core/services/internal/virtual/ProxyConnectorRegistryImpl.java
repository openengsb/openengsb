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
