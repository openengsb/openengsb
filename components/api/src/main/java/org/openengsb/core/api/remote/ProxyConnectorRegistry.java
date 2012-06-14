package org.openengsb.core.api.remote;


public interface ProxyConnectorRegistry {

    void registerConnector(String uuid, String portId, String destination);

    void unregisterConnector(String uuid);

}
