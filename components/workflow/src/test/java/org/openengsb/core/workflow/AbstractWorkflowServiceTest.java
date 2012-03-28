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

import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.TaskboxServiceInternal;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.persistence.internal.DefaultPersistenceManager;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.workflow.internal.TaskboxServiceImpl;
import org.openengsb.core.workflow.internal.TaskboxServiceInternalImpl;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.persistence.PersistenceTestUtil;

public abstract class AbstractWorkflowServiceTest extends AbstractOsgiMockServiceTest {

    protected WorkflowServiceImpl service;
    protected RuleManager manager;
    protected DummyService myservice;
    protected HashMap<String, Domain> domains;
    protected TaskboxService taskbox;
    protected TaskboxServiceInternal taskboxInternal;

    @BeforeClass
    public static void setUpClass() throws Exception {
        cleanup();
    }

    @Before
    public void setUp() throws Exception {
        OsgiHelper.setUtilsService(new DefaultOsgiUtilsService(bundleContext));
        setupRulemanager();
        service = new WorkflowServiceImpl();
        setupTaskbox();
        service.setRulemanager(manager);
        service.setTaskbox(taskbox);
        ContextHolder.get().setCurrentContextId("42");
        service.setBundleContext(bundleContext);
        registerServiceViaId(service, "workflowService", WorkflowService.class);
        setupDomainsAndOtherServices();
    }

    private void setupTaskbox() {
        DefaultPersistenceManager persistenceManager = new DefaultPersistenceManager();
        persistenceManager.setPersistenceRootDir("target/" + UUID.randomUUID().toString());
        TaskboxServiceImpl taskboxServiceImpl = new TaskboxServiceImpl();
        taskboxServiceImpl.setPersistenceManager(persistenceManager);
        taskboxServiceImpl.setBundleContext(bundleContext);
        taskboxServiceImpl.init();
        taskboxServiceImpl.setWorkflowService(service);
        TaskboxServiceInternalImpl taskboxInternalImpl = new TaskboxServiceInternalImpl();
        taskboxInternalImpl.setBundleContext(bundleContext);
        taskboxInternalImpl.setPersistenceManager(persistenceManager);
        taskboxInternalImpl.init();
        taskbox = taskboxServiceImpl;
        taskboxInternal = taskboxInternalImpl;
    }

    private void setupRulemanager() throws Exception {
        manager = PersistenceTestUtil.getRuleManager();
        RuleUtil.addImportsAndGlobals(manager);
        RuleUtil.addHello1Rule(manager);
        RuleUtil.addTestFlows(manager);
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "logtest"),
            "when\n Event ( name == \"test-context\")\n then \n example.doSomething(\"42\");");
    }

    private void setupDomainsAndOtherServices() throws Exception {
        createDomainMocks();
        myservice = mock(DummyService.class);
        registerServiceAtLocation(myservice, "myservice", DummyService.class);
    }

    private void createDomainMocks() throws Exception {
        domains = new HashMap<String, Domain>();
        registerDummyConnector(DummyExampleDomain.class, "example");
        registerDummyConnector(DummyNotificationDomain.class, "notification");
        registerDummyConnector(DummyBuild.class, "build");
        registerDummyConnector(DummyDeploy.class, "deploy");
        registerDummyConnector(DummyReport.class, "report");
        registerDummyConnector(DummyIssue.class, "issue");
        registerDummyConnector(DummyTest.class, "test");
    }

    @SuppressWarnings("unchecked")
    protected <T extends Domain> T registerDummyConnector(Class<T> domainClass, String name) throws Exception {
        Domain mock2 = mock(domainClass);
        registerServiceAtLocation(mock2, name, Domain.class, domainClass);
        domains.put(name, mock2);
        return (T) mock2;
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
