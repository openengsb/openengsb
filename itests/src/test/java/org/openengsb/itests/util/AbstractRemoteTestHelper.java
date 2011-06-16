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

package org.openengsb.itests.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;

/**
 * Abstracts the general concepts required for remote tests
 */
public class AbstractRemoteTestHelper extends AbstractExamTestHelper {

    protected RuleManager ruleManager;

    @Before
    public void setUp() throws Exception {
        ruleManager = getOsgiService(RuleManager.class);
    }

    protected String getRequest(String messageId) {
        return ""
                + "{"
                + "    \"callId\": \"" + messageId + "\","
                + "    \"answer\": true,"
                + "    \"classes\": ["
                + "        \"java.lang.String\","
                + "        \"org.openengsb.core.api.workflow.model.ProcessBag\""
                + "    ],"
                + "    \"methodName\": \"executeWorkflow\","
                + "    \"metaData\": {"
                + "        \"serviceId\": \"workflowService\","
                + "        \"contextId\": \"foo\""
                + "    },"
                + "    \"args\": ["
                + "        \"simpleFlow\","
                + "        {"
                + "        }"
                + "    ]"
                + "}";
    }

    protected String getAuditingRequest(String messageId) {
        return ""
                + "{"
                + "    \"callId\": \"" + messageId + "\","
                + "    \"answer\": true,"
                + "    \"classes\": ["
                + "        \"java.lang.String\""
                + "    ],"
                + "    \"methodName\": \"audit\","
                + "    \"metaData\": {"
                + "        \"serviceId\": \"auditing+memoryauditing+auditing-root\","
                + "        \"contextId\": \"foo\""
                + "    },"
                + "    \"args\": ["
                + "        \"testMessage\""
                + "    ]"
                + "}";
    }

    protected void addWorkflow(String workflow) throws IOException, RuleBaseException {
        if (ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Process, workflow)) == null) {
            InputStream is =
                getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + workflow + ".rf");
            String testWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, workflow);
            ruleManager.add(id, testWorkflow);
            IOUtils.closeQuietly(is);
        }
    }

}
