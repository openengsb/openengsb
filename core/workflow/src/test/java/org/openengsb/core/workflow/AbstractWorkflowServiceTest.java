/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.core.workflow;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.context.ContextHolder;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.persistence.PersistenceTestUtil;
import org.osgi.framework.InvalidSyntaxException;

public abstract class AbstractWorkflowServiceTest extends AbstractOsgiMockServiceTest {

    protected WorkflowServiceImpl service;
    protected RuleManager manager;
    protected DummyService myservice;
    protected HashMap<String, Domain> domains;

    @BeforeClass
    public static void setUpClass() throws Exception {
        cleanup();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupRulemanager();
        service = new WorkflowServiceImpl();
        service.setRulemanager(manager);
        ContextHolder.get().setCurrentContextId("42");
        service.setBundleContext(bundleContext);
        registerService(service, "workflowService", WorkflowService.class);
        setupDomainsAndOtherServices();
    }

    private void setupRulemanager() throws Exception {
        manager = PersistenceTestUtil.getRuleManager();
        RuleUtil.addHello1Rule(manager);
        RuleUtil.addTestFlows(manager);
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "logtest"),
            "when\n Event ( name == \"test-context\")\n then \n example.doSomething(\"42\");");
    }

    private void setupDomainsAndOtherServices() throws InvalidSyntaxException {
        createDomainMocks();
        myservice = mock(DummyService.class);
        registerService(myservice, DummyService.class, OsgiServiceUtils.getFilterForLocation("myservice", "42")
            .toString());
    }

    private void createDomainMocks() throws InvalidSyntaxException {
        domains = new HashMap<String, Domain>();
        registerDummyConnector(DummyExampleDomain.class, "example");
        registerDummyConnector(DummyNotificationDomain.class, "notification");
        registerDummyConnector(DummyBuild.class, "build");
        registerDummyConnector(DummyDeploy.class, "deploy");
        registerDummyConnector(DummyReport.class, "report");
        registerDummyConnector(DummyIssue.class, "issue");
        registerDummyConnector(DummyTest.class, "test");
    }

    private void registerDummyConnector(Class<? extends Domain> domainClass, String name)
        throws InvalidSyntaxException {
        Domain mock2 = mock(domainClass);
        registerSerivce(mock2, new Class<?>[]{ domainClass, Domain.class },
            OsgiServiceUtils.getFilterForLocation(name, "42").toString());
        domains.put(name, mock2);
    }

    @After
    public void tearDown() throws Exception {
        cleanup();
    }

    private static void cleanup() {
        File ruleDir = new File("data");
        while (ruleDir.exists()) {
            FileUtils.deleteQuietly(ruleDir);
        }
    }

}
