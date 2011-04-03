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

package org.openengsb.core.workflow.taskbox.config;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.internal.TaskboxConfigurator;

public class TaskboxConfiguratorTest {
    private TaskboxConfigurator configurator;
    private RuleManager ruleManager;

    @Before
    public void setUp() {
        ruleManager = Mockito.mock(RuleManager.class);
        Mockito.when(ruleManager.listGlobals()).thenReturn(new HashMap<String, String>());

        configurator = new TaskboxConfigurator();
        configurator.setRuleManager(ruleManager);
    }

    @Test
    public void testInit_shouldMakeAllCalls() throws RuleBaseException {
        configurator.init();

        RuleBaseElementId workflowId = new RuleBaseElementId(RuleBaseElementType.Process, "humantask");
        Mockito.verify(ruleManager).add(Mockito.eq(workflowId), Mockito.anyString());
        
        Mockito.verify(ruleManager).addGlobal(Mockito.anyString(), Mockito.eq("taskbox"));
        Mockito.verify(ruleManager).addGlobal(Mockito.anyString(), Mockito.eq("taskboxinternal"));
        
        Mockito.verify(ruleManager, Mockito.times(2)).addImport(Mockito.anyString());
    }
}
