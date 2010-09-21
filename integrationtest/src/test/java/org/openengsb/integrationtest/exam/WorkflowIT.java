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

package org.openengsb.integrationtest.exam;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.WorkflowService;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.notification.NotificationDomain;
import org.openengsb.domains.notification.model.Notification;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class WorkflowIT extends AbstractExamTestHelper {

    public static class DummyNotificationDomain implements NotificationDomain {

        private Notification notification;

        @Override
        public void notify(Notification notification) {
            this.notification = notification;
        }
    }

    public static class DummyLogDomain implements ExampleDomain {
        @Override
        public void doSomething(String message) {
        }
    }

    @Test
    public void testHasHelloRule() throws Exception {
        RuleManager ruleManager = retrieveService(getBundleContext(), RuleManager.class);
        Collection<RuleBaseElementId> list = ruleManager.list(RuleBaseElementType.Rule);
        Assert.assertTrue(list.contains(new RuleBaseElementId(RuleBaseElementType.Rule, "hello1")));
    }

    @Test
    public void testSendEvent() throws Exception {
        ContextCurrentService contextService = retrieveService(getBundleContext(), ContextCurrentService.class);
        contextService.createContext("42");
        contextService.setThreadLocalContext("42");
        contextService.putValue("domains/NotificationDomain/defaultConnector/id", "dummyConnector");
        contextService.putValue("domains/ExampleDomain/defaultConnector/id", "dummyLog");

        DummyNotificationDomain dummy = new DummyNotificationDomain();
        String[] clazzes = new String[] { Domain.class.getName(), NotificationDomain.class.getName() };
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("id", "dummyConnector");

        getBundleContext().registerService(clazzes, dummy, properties);

        clazzes = new String[] { Domain.class.getName(), ExampleDomain.class.getName() };
        properties.put("id", "dummyLog");

        getBundleContext().registerService(clazzes, new DummyLogDomain(), properties);

        WorkflowService workflowService = retrieveService(getBundleContext(), WorkflowService.class);
        Event e = new Event("42");
        workflowService.processEvent(e);

        Assert.assertNotNull(dummy.notification);
    }
}