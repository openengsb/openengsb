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
import org.drools.runtime.rule.ConsequenceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.internal.dirsource.DirectoryRuleSource;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.notification.NotificationDomain;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WorkflowServiceDynamicTest {

    private WorkflowServiceImpl service;
    private RuleManager manager;
    private ServiceEvent exampleServiceEvent;
    private ExampleDomain example;
    private ServiceEvent notificationServiceEvent;
    private NotificationDomain notification;

    @Before
    public void setUp() throws Exception {
        setupWorkflowService();

        ServiceReference exampleReference = setupServiceReferenceMock("domains.log");
        ServiceReference notificationReference = setupServiceReferenceMock("domains.notification");

        exampleServiceEvent = setupServiceEventMock(exampleReference);
        notificationServiceEvent = setupServiceEventMock(notificationReference);

        BundleContext context = mock(BundleContext.class);
        example = mock(ExampleDomain.class);
        when(context.getService(exampleReference)).thenReturn(example);

        notification = mock(NotificationDomain.class);
        when(context.getService(notificationReference)).thenReturn(notification);

        service.setBundleContext(context);
    }

    private ServiceEvent setupServiceEventMock(ServiceReference reference) {
        ServiceEvent result = mock(ServiceEvent.class);
        when(result.getType()).thenReturn(ServiceEvent.REGISTERED);
        when(result.getServiceReference()).thenReturn(reference);
        return result;
    }

    @After
    public void tearDown() throws Exception {
        File ruleDir = new File("data");
        while (ruleDir.exists()) {
            FileUtils.deleteQuietly(ruleDir);
        }
    }

    @Test(expected = ConsequenceException.class)
    public void processEventBeforeServiceStarted_shouldThrowException() throws Exception {
        service.processEvent(new Event("42"));
    }

    @Test
    public void processEventAfterServiceEvents_shouldExecuteServiceMethod() throws Exception {
        service.serviceChanged(exampleServiceEvent);
        service.serviceChanged(notificationServiceEvent);
        service.processEvent(new Event("42"));
        verify(example).doSomething(anyString());
    }

    private ServiceReference setupServiceReferenceMock(String id) {
        ServiceReference reference = mock(ServiceReference.class);
        when(reference.getProperty("openengsb.service.type")).thenReturn("domain");
        when(reference.getProperty("id")).thenReturn(id);
        return reference;
    }

    private void setupWorkflowService() throws RuleBaseException {
        service = new WorkflowServiceImpl();
        setupRulemanager();
        service.setRulemanager(manager);
        service.setCurrentContextService(mock(ContextCurrentService.class));
    }

    private void setupRulemanager() throws RuleBaseException {
        manager = new DirectoryRuleSource("data/rulebase");
        ((DirectoryRuleSource) manager).init();
    }

}
