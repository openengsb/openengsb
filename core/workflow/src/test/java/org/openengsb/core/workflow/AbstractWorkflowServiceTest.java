/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.persistence.PersistenceTestUtil;

public abstract class AbstractWorkflowServiceTest extends AbstractOsgiMockServiceTest {

    protected WorkflowServiceImpl service;
    protected RuleManager manager;
    protected DummyExampleDomain logService;
    protected DummyNotificationDomain notification;
    protected DummyBuild build;
    protected DummyDeploy deploy;
    protected DummyReport report;
    protected DummyIssue issue;
    protected DummyTest test;
    protected DummyService myservice;

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
        ContextCurrentService currentContext = mock(ContextCurrentService.class);
        when(currentContext.getThreadLocalContext()).thenReturn("42");
        service.setCurrentContextService(currentContext);
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

    private void setupDomainsAndOtherServices() {
        Map<String, Object> services = new HashMap<String, Object>();
        Map<String, Domain> domains = createDomainMocks();
        services.putAll(domains);
        myservice = mock(DummyService.class);
        services.put("myservice", myservice);
        OsgiHelper osgiHelper = new OsgiHelper();
        osgiHelper.setBundleContext(bundleContext);
        services.put("osgiHelper", osgiHelper);
        service.setServices(services);
    }

    private Map<String, Domain> createDomainMocks() {
        Map<String, Domain> domains = new HashMap<String, Domain>();
        logService = mock(DummyExampleDomain.class);
        domains.put("example", logService);
        notification = mock(DummyNotificationDomain.class);
        domains.put("notification", notification);
        build = mock(DummyBuild.class);
        domains.put("build", build);
        deploy = mock(DummyDeploy.class);
        domains.put("deploy", deploy);
        report = mock(DummyReport.class);
        domains.put("report", report);
        issue = mock(DummyIssue.class);
        domains.put("issue", issue);
        test = mock(DummyTest.class);
        domains.put("test", test);
        return domains;
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
