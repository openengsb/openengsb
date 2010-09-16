/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.core.workflow;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.internal.dirsource.DirectoryRuleSource;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.notification.NotificationDomain;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ExampleDomain example;
    private NotificationDomain notification;
    private BundleContext bundleContext;
    private ServiceReference exampleReference;
    private ServiceReference notificationReference;

    @Before
    public void setUp() throws Exception {
        bundleContext = mock(BundleContext.class);

        exampleReference = setupServiceReferenceMock("log");
        notificationReference = setupServiceReferenceMock("notification");

        example = mock(ExampleDomain.class);
        when(bundleContext.getService(exampleReference)).thenReturn(example);

        notification = mock(NotificationDomain.class);
        when(bundleContext.getService(notificationReference)).thenReturn(notification);
    }

    @After
    public void tearDown() throws Exception {
        File ruleDir = new File("data");
        while (ruleDir.exists()) {
            FileUtils.deleteQuietly(ruleDir);
        }
    }

    @Test
    public void processEventAfterServiceEvents_shouldExecuteServiceMethod() throws Exception {
        setupWorkflowService();
        simulateServiceStart(exampleReference);
        simulateServiceStart(notificationReference);
        workflowService.processEvent(new Event("42"));

        verify(example).doSomething(anyString());
    }

    @Test(expected = WorkflowException.class)
    public void processEventBetweenServiceEvents_shouldThrowWorkflowException() throws Exception {
        setupWorkflowService();
        workflowService.setTimeout(100);
        simulateServiceStart(exampleReference);
        workflowService.processEvent(new Event("42"));
    }

    @Test
    public void processEventShortlyBeforeServiceEvent_shouldProcessEventDelayed() throws Exception {
        setupWorkflowService();
        simulateServiceStart(exampleReference);
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
        when(reference.getProperty("id")).thenReturn("domains." + id);
        return reference;
    }

    private void simulateServiceStart(ServiceReference reference) throws InvalidSyntaxException {
        String id = (String) reference.getProperty("id");
        String filter = String.format("(&(openengsb.service.type=domain)(id=%s))", id);
        when(bundleContext.getAllServiceReferences(Domain.class.getName(), filter)).thenReturn(
                new ServiceReference[] { reference });
        if (workflowService != null) {
            workflowService.serviceChanged(setupServiceEventMock(reference));
        }
    }

    private void setupWorkflowService() throws RuleBaseException {
        workflowService = new WorkflowServiceImpl();
        setupRulemanager();
        workflowService.setRulemanager(manager);
        workflowService.setCurrentContextService(mock(ContextCurrentService.class));
        workflowService.setBundleContext(bundleContext);
    }

    private void setupRulemanager() throws RuleBaseException {
        manager = new DirectoryRuleSource("data/rulebase");
        ((DirectoryRuleSource) manager).init();
    }

}
