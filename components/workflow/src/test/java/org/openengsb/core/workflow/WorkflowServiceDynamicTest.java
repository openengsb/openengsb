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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.persistence.PersistenceTestUtil;
import org.osgi.framework.BundleContext;

public class WorkflowServiceDynamicTest extends AbstractOsgiMockServiceTest {

    private Event sampleEvent = ModelUtils.createEmptyModelObject(Event.class);

    private class ProcessEventThread extends Thread {
        private Exception resultException;

        @Override
        public void run() {
            try {
                workflowService.processEvent(sampleEvent);
            } catch (WorkflowException e) {
                resultException = e;
            }
        }
    }

    private WorkflowServiceImpl workflowService;
    private RuleManager manager;
    private DummyExampleDomain example;
    private DummyNotificationDomain notification;
    private DummyService myservice;
    private Map<String, Object> services = new HashMap<String, Object>();
    private Map<String, Class<?>> domains = new HashMap<String, Class<?>>();

    @Before
    public void setUp() throws Exception {
        sampleEvent.setName("42");
        ContextHolder.get().setCurrentContextId("42");
        domains.put("example", DummyExampleDomain.class);
        example = mock(DummyExampleDomain.class);
        services.put("example", example);
        domains.put("notification", DummyNotificationDomain.class);
        notification = mock(DummyNotificationDomain.class);
        services.put("notification", notification);
        domains.put("myservice", DummyService.class);
        myservice = mock(DummyService.class);
        services.put("myservice", myservice);
    }

    private void mockDomain(String name) throws RuleBaseException {
        manager.removeGlobal(name);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void processEventAfterServiceEvents_shouldExecuteServiceMethod() throws Exception {
        setupWorkflowService();
        simulateServiceStart("example");
        simulateServiceStart("notification");
        simulateServiceStart("myservice");
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("42");
        workflowService.processEvent(event);

        verify(example).doSomething(anyString());
    }

    @Test
    public void processEventShortlyBeforeServiceEvent_shouldProcessEventDelayed() throws Exception {
        setupWorkflowService();
        simulateServiceStart("example");
        simulateServiceStart("myservice");
        ProcessEventThread processEventThread = new ProcessEventThread();
        processEventThread.start();
        processEventThread.setPriority(Thread.MIN_PRIORITY);
        Thread.sleep(100);

        simulateServiceStart("notification");
        processEventThread.join(5000);
        if (processEventThread.resultException != null) {
            throw processEventThread.resultException;
        }
        verify(example).doSomething(anyString());
    }

    @Test
    public void lookupAtStartup_shouldPickupServicesStartedBeforeWorkflow() throws Exception {
        simulateServiceStart("example");
        simulateServiceStart("notification");
        simulateServiceStart("myservice");
        setupWorkflowService();
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("42");
        workflowService.processEvent(event);

        verify(example).doSomething(anyString());
    }

    private void simulateServiceStart(String name) throws Exception {
        Class<?> domainClass = domains.get(name);
        Class<?>[] interfaces;
        if (Domain.class.isAssignableFrom(domainClass)) {
            interfaces = new Class<?>[]{ Domain.class, domainClass, };
        } else {
            interfaces = new Class<?>[]{ domainClass, };
        }
        registerServiceAtLocation(services.get(name), name, interfaces);
    }

    private void setupWorkflowService() throws Exception {
        workflowService = new WorkflowServiceImpl();
        setupRulemanager();
        workflowService.setRulemanager(manager);
        workflowService.setBundleContext(bundleContext);
    }

    private void setupRulemanager() throws Exception {
        manager = PersistenceTestUtil.getRuleManager();
        mockDomain("deploy");
        mockDomain("build");
        mockDomain("test");
        mockDomain("report");
        mockDomain("issue");
        RuleUtil.addHello1Rule(manager);
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
    }

}

