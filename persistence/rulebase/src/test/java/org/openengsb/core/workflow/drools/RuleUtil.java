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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openengsb.core.workflow.api.RuleBaseException;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;

public final class RuleUtil {

    private RuleUtil() {

    }

    public static void addHello1Rule(RuleManager manager) throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "hello1");
        String rule = readRule();
        manager.add(id, rule);
    }

    private static String readRule() throws IOException {
        InputStream helloWorldRule = null;
        try {
            helloWorldRule = RuleUtil.class.getClassLoader().getResourceAsStream("rulebase/org/openengsb/hello1.rule");
            return IOUtils.toString(helloWorldRule);
        } finally {
            IOUtils.closeQuietly(helloWorldRule);
        }
    }

    public static void addTestFlows(RuleManager manager) throws Exception {
        addFlow(manager, "flowtest");
        addFlow(manager, "ci");
        addFlow(manager, "flowStartedEvent");
        addFlow(manager, "floweventtest");
        addFlow(manager, "propertybagtest");
        addFlow(manager, "blockingFlowtest");
        addFlow(manager, "subFlowtest");
        addFlow(manager, "simpleFlow");
        addFlow(manager, "backgroundFlow");
    }

    private static void addFlow(RuleManager manager, String flow) throws IOException, RuleBaseException {
        RuleBaseElementId testFlowId = new RuleBaseElementId(RuleBaseElementType.Process, flow);
        String code = readFlow(flow);
        manager.add(testFlowId, code);
    }

    private static String readFlow(String string) throws IOException {
        InputStream flowStream =
            RuleUtil.class.getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + string + ".rf");
        return IOUtils.toString(flowStream);
    }

    public static void addImportsAndGlobals(RuleManager manager) throws IOException {
        InputStream inputStream = RuleUtil.class.getClassLoader().getResourceAsStream("rulebase/imports");
        List<String> importLines = IOUtils.readLines(inputStream);
        for (String s : importLines) {
            String importLine = s.trim();
            if (importLine.isEmpty() || importLine.startsWith("#")) {
                continue;
            }
            manager.addImport(importLine);
        }

        inputStream = RuleUtil.class.getClassLoader().getResourceAsStream("rulebase/globals");
        List<String> globalLines = IOUtils.readLines(inputStream);
        for (String s : globalLines) {
            String globalLine = s.trim();
            if (globalLine.isEmpty() || globalLine.startsWith("#")) {
                continue;
            }
            String[] parts = globalLine.split(" ");
            if (parts.length != 2) {
                continue;
            }
            manager.addGlobalIfNotPresent(parts[0], parts[1]);
        }

    }

}
