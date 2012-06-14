package org.openengsb.core.services.internal.virtual;

public class ProxyRegistration {

    private String uuid;
    private String portId;
    private String destination;

    public ProxyRegistration(String uuid, String portId, String destination) {
        this(uuid);
        this.portId = portId;
        this.destination = destination;
    }

    public ProxyRegistration(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isRegistered() {
        return destination != null;
    }

}
