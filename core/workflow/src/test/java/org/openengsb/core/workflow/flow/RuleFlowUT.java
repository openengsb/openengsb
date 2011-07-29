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

package org.openengsb.core.workflow.flow;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.junit.Test;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.core.workflow.model.TestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleFlowUT extends AbstractOpenEngSBTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleFlowUT.class);

    @Test
    public void testRunFlow() throws Exception {
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("flowtest.drl", getClass()), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newClassPathResource("flowtest.rf", getClass()), ResourceType.DRF);
        LOGGER.error("{}", kbuilder.getErrors());
        if (kbuilder.hasErrors()) {
            fail(kbuilder.getErrors().toString());
        }

        final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        ksession.startProcess("flowtest");
        ksession.fireAllRules();

        ksession.dispose();
    }

    @Test
    public void testRunFlowWithEvents() throws Exception {
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newClassPathResource("flowtest.drl", getClass()), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newClassPathResource("floweventtest.rf", getClass()), ResourceType.DRF);
        LOGGER.error("{}", kbuilder.getErrors());
        if (kbuilder.hasErrors()) {
            fail(kbuilder.getErrors().toString());
        }

        final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        ProcessEventListener listener = new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                LOGGER.debug("Process complete {}", event.getProcessInstance().getProcessId());
                ksession.halt();
            }
        };
        ksession.addEventListener(listener);
        ksession.setGlobal("log", LOGGER);
        ProcessInstance startProcess = ksession.startProcess("flowtest");
        Thread t = new Thread() {
            @Override
            public void run() {
                ksession.fireUntilHalt();
            }
        };
        t.start();

        // submit them in wrong order -> does not matter
        ksession.signalEvent("TestObject2", new TestObject("foo"), startProcess.getId());
        Thread.sleep(200);
        ksession.signalEvent("TestObject", new TestObject("foo"), startProcess.getId());

        t.join(2000);
        assertThat("Process could not finish", t.getState(), is(Thread.State.TERMINATED));
        ksession.dispose();
    }
}
