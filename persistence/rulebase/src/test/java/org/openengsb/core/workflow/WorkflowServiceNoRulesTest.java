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
package org.openengsb.core.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistence;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.model.GlobalDeclaration;
import org.openengsb.core.workflow.model.ImportDeclaration;
import org.openengsb.core.workflow.model.RuleBaseElement;
import org.openengsb.core.workflow.persistence.util.PersistenceTestUtil;
import org.openengsb.domain.auditing.AuditingDomain;

public class WorkflowServiceNoRulesTest extends AbstractOsgiMockServiceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private static final String FLOW_NAME = "simpleFlow";
    private WorkflowServiceImpl workflowService;
    private RuleManager manager;

    @Before
    public void setUp() throws Exception {
        setupWorkflowService();
    }

    @Test
    public void testInitRulebase_shouldPickupFlows() throws Exception {
        ContextHolder.get().setCurrentContextId("foo");
        workflowService.startFlow(FLOW_NAME);
    }

    private void setupWorkflowService() throws Exception {
        workflowService = new WorkflowServiceImpl();
        setupRulemanager();
        workflowService.setRulemanager(manager);
        workflowService.setBundleContext(bundleContext);
        List<AuditingDomain> emptyList = Collections.emptyList();
        workflowService.setAuditingConnectors(emptyList);
    }

    private void setupRulemanager() throws Exception {
        DummyPersistence persistenceMock = new DummyPersistence();
        InputStream inputStream = RuleUtil.class.getClassLoader().getResourceAsStream("rulebase/imports");
        List<String> importLines = IOUtils.readLines(inputStream);
        for (String s : importLines) {
            String importLine = s.trim();
            if (importLine.isEmpty() || importLine.startsWith("#")) {
                continue;
            }
            ImportDeclaration importDec = new ImportDeclaration(importLine);
            persistenceMock.create(importDec);
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
            persistenceMock.create(new GlobalDeclaration(parts[0], parts[1]));
        }

        RuleBaseElementId testFlowId = new RuleBaseElementId(RuleBaseElementType.Process, FLOW_NAME);
        String code = readFlow(FLOW_NAME);
        persistenceMock.create(new RuleBaseElement(testFlowId, code));
        manager = PersistenceTestUtil.getRuleManager(folder);
        // RuleUtil.addHello1Rule(manager);
    }

    private static String readFlow(String string) throws IOException {
        InputStream flowStream =
            RuleUtil.class.getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + string + ".rf");
        return IOUtils.toString(flowStream);
    }
}
