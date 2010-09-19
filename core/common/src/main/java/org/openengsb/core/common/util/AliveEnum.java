package org.openengsb.core.common.util;

public enum AliveEnum {

    /**
     * domain is connecting
     */
    CONNECTING,

    /**
     * domain is online, means it is connected and working
     */
    ONLINE,


    /**
     * domain is offline, means an error occurred and it has to updated
     */
    OFFLINE,


    /**
     * domain is disconnected means, from the view point of a domain everything is ok,
     */
    DISCONNECTED;

}
