/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.drools;

import java.util.List;

import org.openengsb.core.model.Event;
import org.openengsb.drools.model.Report;

public interface ReportDomain extends Domain {

    public static final String contextId = "contextId";
    public static final String correlationId = "correlationId";
    public static final String workflowId = "workflowId";
    public static final String workflowInstanceId = "workflowInstanceId";

    Report generateReport(String reportId);

    Report generateReport(List<Event> events);

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
