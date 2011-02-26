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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.RuleBaseException;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.persistence.PersistenceTestUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class WorkflowServiceDynamicTest {

    private Event sampleEvent = new Event("42");

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
    private BundleContext bundleContext;
    private ServiceReference exampleReference;
    private ServiceReference notificationReference;
    private ServiceReference myserviceReference;

    @Before
    public void setUp() throws Exception {
        bundleContext = mock(BundleContext.class);

        exampleReference = setupServiceReferenceMock("example");
        notificationReference = setupServiceReferenceMock("notification");
        myserviceReference = setupOtherServiceReferenceMock("myservice");

        example = mock(DummyExampleDomain.class);
        when(bundleContext.getService(exampleReference)).thenReturn(example);

        notification = mock(DummyNotificationDomain.class);
        when(bundleContext.getService(notificationReference)).thenReturn(notification);

        myservice = mock(DummyService.class);
        when(bundleContext.getService(myserviceReference)).thenReturn(myservice);

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
        simulateServiceStart(exampleReference);
        simulateServiceStart(notificationReference);
        simulateOtherServiceStart(myserviceReference);
        workflowService.processEvent(new Event("42"));

        verify(example).doSomething(anyString());
    }

    @Test(expected = WorkflowException.class)
    public void processEventBetweenServiceEvents_shouldThrowWorkflowException() throws Exception {
        setupWorkflowService();
        simulateServiceStart(exampleReference);
        workflowService.processEvent(new Event("42"));
    }

    @Test
    public void processEventShortlyBeforeServiceEvent_shouldProcessEventDelayed() throws Exception {
        setupWorkflowService();
        simulateServiceStart(exampleReference);
        simulateOtherServiceStart(myserviceReference);
        ProcessEventThread processEventThread = new ProcessEventThread();
        processEventThread.start();
        processEventThread.setPriority(Thread.MIN_PRIORITY);
        Thread.sleep(100);

        simulateServiceStart(notificationReference);
        processEventThread.join(5000);
        if (processEventThread.resultException != null) {
            throw processEventThread.resultException;
        }
        verify(example).doSomething(anyString());
    }

    @Test
    public void lookupAtStartup_shouldPickupServicesStartedBeforeWorkflow() throws Exception {
        simulateServiceStart(exampleReference);
        simulateServiceStart(notificationReference);
        simulateOtherServiceStart(myserviceReference);
        setupWorkflowService();
        workflowService.processEvent(new Event("42"));

        verify(example).doSomething(anyString());
    }

    private ServiceEvent setupServiceEventMock(ServiceReference reference) {
        ServiceEvent result = mock(ServiceEvent.class);
        when(result.getType()).thenReturn(ServiceEvent.REGISTERED);
        when(result.getServiceReference()).thenReturn(reference);
        return result;
    }

    private ServiceReference setupServiceReferenceMock(String id) throws InvalidSyntaxException {
        ServiceReference reference = mock(ServiceReference.class);
        when(reference.getProperty("openengsb.service.type")).thenReturn("domain");
        when(reference.getProperty("id")).thenReturn("domain." + id);
        return reference;
    }

    private ServiceReference setupOtherServiceReferenceMock(String id) throws InvalidSyntaxException {
        ServiceReference reference = mock(ServiceReference.class);
        when(reference.getProperty("openengsb.service.type")).thenReturn("workflow-service");
        when(reference.getProperty("openengsb.workflow.globalid")).thenReturn(id);
        return reference;
    }

    private void simulateServiceStart(ServiceReference reference) throws InvalidSyntaxException {
        String id = (String) reference.getProperty("id");
        String filter = String.format("(&(openengsb.service.type=domain)(id=%s))", id);
        when(bundleContext.getAllServiceReferences(Domain.class.getName(), filter)).thenReturn(
            new ServiceReference[]{ reference });
        if (workflowService != null) {
            // workflowService.serviceChanged(setupServiceEventMock(reference));
        }
    }

    private void simulateOtherServiceStart(ServiceReference reference) throws InvalidSyntaxException {
        String id = (String) reference.getProperty("openengsb.workflow.globalid");
        String filter =
            String.format("(&(openengsb.service.type=workflow-service)(openengsb.workflow.globalid=%s))", id);
        when(bundleContext.getAllServiceReferences(Mockito.any(String.class), Mockito.eq(filter))).thenReturn(
            new ServiceReference[]{ reference });
        if (workflowService != null) {
            // workflowService.serviceChanged(setupServiceEventMock(reference));
        }
    }

    private void setupWorkflowService() throws Exception {
        workflowService = new WorkflowServiceImpl();
        setupRulemanager();
        workflowService.setRulemanager(manager);
        ContextCurrentService currentContext = mock(ContextCurrentService.class);
        when(currentContext.getThreadLocalContext()).thenReturn("42");
        // workflowService.setCurrentContextService(currentContext);
        workflowService.setBundleContext(bundleContext);
        OsgiHelper osgiHelper = new OsgiHelper();
        Map<String, Object> services = new HashMap<String, Object>();
        services.put("osgiHelper", osgiHelper);
        // workflowService.setServices(services);

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

}
