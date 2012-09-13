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

package org.openengsb.core.workflow.drools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.drools.runtime.KnowledgeRuntime;
import org.drools.runtime.process.ProcessInstance;
import org.openengsb.core.workflow.api.model.ProcessBag;

public final class WorkflowHelper {

    private WorkflowHelper() {
    }

    public static long startFlow(KnowledgeRuntime session, String processId) {
        return startFlow(session, processId, new HashMap<String, Object>());
    }

    public static long startFlow(KnowledgeRuntime session, String processId, Map<String, Object> params) {
        ProcessBag processBag = getProcessBag(params);
        ProcessInstance processInstance = session.createProcessInstance(processId, params);
        session.insert(processInstance);
        processBag.setProcessId(String.valueOf(processInstance.getId()));
        session.startProcessInstance(processInstance.getId());
        return processInstance.getId();
    }

    private static ProcessBag getProcessBag(Map<String, Object> params) {
        ProcessBag processBag;
        if (!params.containsKey("processBag")) {
            processBag = new ProcessBag();
            params.put("processBag", processBag);
        } else {
            processBag = (ProcessBag) params.get("processBag");
        }
        return processBag;
    }

    public static Callable<Long> getCallable(final KnowledgeRuntime session, final String processId,
            final Map<String, Object> params) {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return startFlow(session, processId, params);
            }
        };
    }

}
