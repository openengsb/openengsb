package org.openengsb.core.api;

/**
 * Container for the internal OSGi constants used in the OpenEngSB. This is a lookup as well as when direct coding or
 * filter creating against those constants is required.
 */
public final class Constants {
    /**
     * ID used to identfy backend storages. Backend ID's are used in configuration files as well as on services
     * implementing the ConfigBackendService for a specific purpose.
     */
    public static final String BACKEND_ID = "backend.id";

    /**
     * The {@link #CONFIGURATION_ID} is used to map specific areas like RULES or CONNECTORS to {@link #BACKEND_ID}
     * backends. Retrieving a configuration service using this ID will provide the required persistence service.
     */
    public static final String CONFIGURATION_ID = "configuration.id";

    /**
     * The connector constants gives a connector a unique identification. The exacty semantic value is identified by the
     * conenctor itself. Possible values are git, trac, jira, ...
     */
    public static final String CONNECTOR = "connector";

    private Constants() {
        // this class should not be instanciated.
    }

}
