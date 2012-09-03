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

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.UUID;

import org.junit.Before;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.persistence.internal.DefaultPersistenceManager;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.core.workflow.api.TaskboxService;
import org.openengsb.core.workflow.api.TaskboxServiceInternal;
import org.openengsb.core.workflow.api.WorkflowService;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.core.workflow.drools.internal.DroolsRuleManager;
import org.openengsb.core.workflow.drools.internal.TaskboxServiceImpl;
import org.openengsb.core.workflow.drools.internal.TaskboxServiceInternalImpl;
import org.openengsb.core.workflow.drools.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.drools.util.RuleUtil;
import org.openengsb.domain.auditing.AuditingDomain;

public abstract class AbstractWorkflowServiceTest extends AbstractOsgiMockServiceTest {

    protected WorkflowServiceImpl service;
    protected DroolsRuleManager manager;
    protected DummyService myservice;
    protected HashMap<String, Domain> domains;
    protected TaskboxService taskbox;
    protected TaskboxServiceInternal taskboxInternal;
    protected AuditingDomain auditingMock;

    @Before
    public void setUp() throws Exception {
        OsgiHelper.setUtilsService(new DefaultOsgiUtilsService(bundleContext));
        setupRulemanager();

        auditingMock = mock(AuditingDomain.class);
        registerServiceAtLocation(auditingMock, "auditing-root", AuditingDomain.class);

        service = new WorkflowServiceImpl();
        service.setAuditingConnectors(makeServiceList(AuditingDomain.class));

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
        manager = RuleUtil.getRuleManager();
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
        registerDummyConnector(DummyTest.class, "test");
    }

    @SuppressWarnings("unchecked")
    protected <T extends Domain> T registerDummyConnector(Class<T> domainClass, String name) throws Exception {
        Domain mock2 = mock(domainClass);
        registerServiceAtLocation(mock2, name, Domain.class, domainClass);
        domains.put(name, mock2);
        return (T) mock2;
    }
}
