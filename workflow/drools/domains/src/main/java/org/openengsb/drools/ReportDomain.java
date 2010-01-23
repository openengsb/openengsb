package org.openengsb.drools;

import org.openengsb.core.model.Event;

public interface ReportDomain extends Domain {

    public static final String contextId = "contextId";
    public static final String correlationId = "correlationId";
    public static final String flowId = "flowId";

    void generateReport(String reportId);

    void generateReport(Event[] events);

    /**
     * Start collecting data for all arriving events with the given id of the
     * given id type
     * 
     * @param idType one of contextId, correlationId or flowId
     * @param id
     * @return the reportId that can later be used to generate the report
     */
    String collectData(String idType, String id);

}
