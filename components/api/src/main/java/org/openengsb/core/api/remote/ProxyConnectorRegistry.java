package org.openengsb.core.api.remote;

/**
 * manages registrations of remote-connectors
 */
public interface ProxyConnectorRegistry {

    /**
     * registers the given portId and destination for the given connector identified with the given uuid. The given
     * portId and destination are used for future calls on the proxy-connector.
     */
    void registerConnector(String uuid, String portId, String destination);

    /**
     * unregisters a remote-connector. After that the ProxyConnector knows that the remote destination is no longer
     * reachable.
     */
    void unregisterConnector(String uuid);

}
