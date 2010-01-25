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
package org.openengsb.core;

import javax.jbi.messaging.NormalizedMessage;

public class MessageProperties {

    private final String contextId;

    private final String correlationId;

    private final String workflowId;

    private final String workflowInstanceId;

    public MessageProperties(String contextId, String correlationId) {
        this(contextId, correlationId, null);
    }

    public MessageProperties(String contextId, String correlationId, String workflowId) {
        this(contextId, correlationId, workflowId, null);
    }

    public MessageProperties(String contextId, String correlationId, String workflowId, String workflowInstanceId) {
        this.contextId = contextId;
        this.correlationId = correlationId;
        this.workflowId = workflowId;
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getContextId() {
        return contextId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void applyToMessage(NormalizedMessage message) {
        message.setProperty("contextId", contextId);
        message.setProperty("correlationId", correlationId);
        if (workflowId != null) {
            message.setProperty("workflowId", workflowId);
        }
        if (workflowInstanceId != null) {
            message.setProperty("workflowInstaceId", workflowInstanceId);
        }
    }

}
