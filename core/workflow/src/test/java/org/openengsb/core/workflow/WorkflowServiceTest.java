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

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.internal.dirsource.DirectoryRuleSource;
import org.openengsb.core.workflow.model.Event;

public class WorkflowServiceTest {

    @Before
    public void setUp() throws Exception {
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
        WorkflowServiceImpl service = new WorkflowServiceImpl();
        RuleManager manager = new DirectoryRuleSource("data/rulebase");
        service.setRulemanager(manager);
        Event event = new Event("", "hello");
        service.processEvent(event);
    }

    @Test
    public void testProcessEventTriggersHelloWorld() throws Exception {
        WorkflowServiceImpl service = new WorkflowServiceImpl();
        RuleManager manager = new DirectoryRuleSource("data/rulebase");
        service.setRulemanager(manager);
        RuleListener listener = new RuleListener();
        service.registerRuleListener(listener);
        Event event = new Event("", "hello");
        service.processEvent(event);
        Assert.assertTrue(listener.haveRulesFired("hello1"));
    }

    // TriggersHelloWorld

}
