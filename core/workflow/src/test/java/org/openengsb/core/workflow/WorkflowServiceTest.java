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
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.config.Domain;
import org.openengsb.core.workflow.internal.RuleBaseException;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.internal.dirsource.DirectoryRuleSource;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.notification.NotificationDomain;

public class WorkflowServiceTest {

    public interface LogDomain extends Domain {
        void log(String string);
    }

    public class LogDomainMock implements LogDomain {
        public StringBuffer log = new StringBuffer();

        @Override
        public void log(String string) {
            log.append(string);
        }
    }

    private WorkflowServiceImpl service;
    private RuleManager manager;
    private RuleListener listener;
    private ExampleDomain logService;

    @Before
    public void setUp() throws Exception {
        service = new WorkflowServiceImpl();
        setupRulemanager();
        service.setRulemanager(manager);
        service.setCurrentContextService(Mockito.mock(ContextCurrentService.class));
        setupDomains();
        listener = new RuleListener();
        service.registerRuleListener(listener);
    }

    private void setupRulemanager() throws RuleBaseException {
        manager = new DirectoryRuleSource("data/rulebase");
        ((DirectoryRuleSource) manager).init();
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "logtest"),
                "when\n Event ( contextId == \"test-context\")\n then \n log.doSomething(\"42\");");
    }

    private void setupDomains() {
        Map<String, Domain> domains = createDomainMocks();
        service.setDomainServices(domains);
    }

    private Map<String, Domain> createDomainMocks() {
        Map<String, Domain> domains = new HashMap<String, Domain>();
        logService = Mockito.mock(ExampleDomain.class);
        domains.put("log", logService);
        NotificationDomain notification = Mockito.mock(NotificationDomain.class);
        domains.put("notification", notification);
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
        Assert.assertTrue(listener.haveRulesFired("hello1"));
    }

    @Test
    public void testUseLog() throws Exception {
        Event event = new Event("test-context");
        service.processEvent(event);
        Assert.assertTrue(listener.haveRulesFired("logtest"));
    }

    @Test
    public void testUseLogContent() throws Exception {
        Event event = new Event("test-context");
        service.processEvent(event);
        Mockito.verify(logService, Mockito.times(2)).doSomething(Mockito.anyString());
    }
}
