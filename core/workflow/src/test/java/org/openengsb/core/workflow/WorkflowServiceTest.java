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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.internal.dirsource.DirectoryRuleSource;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.model.TestEvent;

public class WorkflowServiceTest {

    public interface LogDomain extends Domain {
        void log(String string);
    }

    public static class LogDomainMock implements LogDomain {
        private final StringBuffer log = new StringBuffer();

        @Override
        public void log(String string) {
            log.append(string);
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }
    }

    private WorkflowServiceImpl service;
    private RuleManager manager;
    private DummyExampleDomain logService;
    private DummyNotificationDomain notification;
    private DummyBuild build;
    private DummyDeploy deploy;
    private DummyReport report;
    private DummyIssue issue;
    private DummyTest test;

    @Before
    public void setUp() throws Exception {
        File ruleSources = FileUtils.toFile(ClassLoader.getSystemResource("rulebase"));
        File workingDir = new File("data/rulebase");
        workingDir.mkdirs();
        FileUtils.copyDirectory(ruleSources, workingDir);
        service = new WorkflowServiceImpl();
        setupRulemanager();
        service.setRulemanager(manager);
        ContextCurrentService currentContext = mock(ContextCurrentService.class);
        when(currentContext.getCurrentContextId()).thenReturn("42");
        service.setCurrentContextService(currentContext);
        setupDomains();
    }

    private void setupRulemanager() throws RuleBaseException {
        manager = new DirectoryRuleSource("data/rulebase");
        ((DirectoryRuleSource) manager).init();
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "logtest"),
            "when\n Event ( name == \"test-context\")\n then \n example.doSomething(\"42\");");
    }

    private void setupDomains() {
        Map<String, Domain> domains = createDomainMocks();
        service.setDomainServices(domains);
    }

    private Map<String, Domain> createDomainMocks() {
        Map<String, Domain> domains = new HashMap<String, Domain>();
        logService = Mockito.mock(DummyExampleDomain.class);
        domains.put("example", logService);
        notification = Mockito.mock(DummyNotificationDomain.class);
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
        File ruleDir = new File("data");
        while (ruleDir.exists()) {
            FileUtils.deleteQuietly(ruleDir);
        }
    }

    @Test
    public void testProcessEvent() throws Exception {
        Event event = new Event();
        service.processEvent(event);
    }

    @Test
    public void testProcessEventTriggersHelloWorld() throws Exception {
        Event event = new Event();
        service.processEvent(event);
        verify(notification, atLeast(1)).notify("Hello");
        verify(logService, atLeast(1)).doSomething("Hello World");
    }

    @Test
    public void testUseLog() throws Exception {
        Event event = new Event("test-context");
        service.processEvent(event);
        verify(logService).doSomething("42");
    }

    @Test
    public void testUpdateRule() throws Exception {
        manager.update(new RuleBaseElementId(RuleBaseElementType.Rule, "hello1"),
            "when\n Event ( name == \"test-context\")\n then \n example.doSomething(\"21\");");
        Event event = new Event("test-context");
        service.processEvent(event);
        verify(logService).doSomething("21");
    }

    @Test
    public void testUseLogContent() throws Exception {
        Event event = new Event("test-context");
        service.processEvent(event);
        Mockito.verify(logService, Mockito.times(2)).doSomething(Mockito.anyString());
    }

    @Test
    public void addInvalidRule_shouldNotModifyRulebase() throws Exception {
        try {
            manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "hello"), "this*is_invalid");
            fail("expected Exception");
        } catch (RuleBaseException e) {
            // expected
        }
        Event event = new Event("test-context");
        service.processEvent(event);
        Mockito.verify(logService, Mockito.times(2)).doSomething(Mockito.anyString());
    }

    @Test
    public void invalidMofidyRule_shouldNotModifyRulebase() throws Exception {
        try {
            manager.update(new RuleBaseElementId(RuleBaseElementType.Rule, "hello1"), "this*is_invalid");
            fail("expected Exception");
        } catch (RuleBaseException e) {
            assertThat(e.getCause(), nullValue());
        }
        Event event = new Event("test-context");
        service.processEvent(event);
        Mockito.verify(logService, Mockito.times(2)).doSomething(Mockito.anyString());
    }

    @Test
    public void testStartProcess_shouldRunScriptNodes() throws Exception {
        long id = service.startFlow("flowtest");
        service.waitForFlowToFinish(id);
        verify(logService).doSomething("flow42");
    }

    @Test
    public void testStartProcessWithEvents_shouldRunScriptNodes() throws Exception {
        long id = service.startFlow("floweventtest");
        service.processEvent(new Event());
        service.processEvent(new TestEvent());
        service.waitForFlowToFinish(id);
        InOrder inOrder2 = inOrder(logService);
        inOrder2.verify(logService).doSomething("start testflow");
        inOrder2.verify(logService).doSomething("first event received");
    }

    @Test
    public void testCiWorkflow() throws Exception {
        long id = service.startFlow("ci");
        service.processEvent(new Event() {
            @Override
            public String getType() {
                return "BuildSuccess";
            }
        });
        service.processEvent(new Event() {
            @Override
            public String getType() {
                return "TestSuccess";
            }
        });
        service.waitForFlowToFinish(id);
        verify(report, times(1)).collectData();
        verify(notification, atLeast(1)).notify(anyString());
        verify(deploy, times(1)).deployProject();
    }
}
