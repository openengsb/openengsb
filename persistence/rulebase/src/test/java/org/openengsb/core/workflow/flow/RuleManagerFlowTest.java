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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.drools.KnowledgeBase;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.model.TestObject;
import org.openengsb.core.workflow.persistence.util.PersistenceTestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleManagerFlowTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleManagerFlowTest.class);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private RuleManager source;
    private KnowledgeBase rulebase;
    private StatefulKnowledgeSession session;

    @Before
    public void setUp() throws Exception {
        source = getRuleBaseSource();
        rulebase = source.getRulebase();
    }

    @After
    public void tearDown() throws Exception {
        if (session != null) {
            session.dispose();
        }
    }

    @After
    @Before
    public void deleteData() throws Exception {
        File ruleDir = new File("data");
        while (ruleDir.exists()) {
            FileUtils.deleteQuietly(ruleDir);
        }
    }

    protected RuleManager getRuleBaseSource() throws Exception {
        RuleManager source = PersistenceTestUtil.getRuleManager(folder);
        return source;
    }

    @Test
    public void testAddFlow() throws Exception {
        source.addImport(TestObject.class.getName());
        URL systemResource = ClassLoader.getSystemResource("flowtest.rf");
        File flowFile = FileUtils.toFile(systemResource);
        String flowString = FileUtils.readFileToString(flowFile);
        source.add(new RuleBaseElementId(RuleBaseElementType.Process, "flowtest"), flowString);
        assertThat(source.get(new RuleBaseElementId(RuleBaseElementType.Process, "flowtest")), notNullValue());
    }

    @Test(timeout = 3000)
    public void testRunFlow() throws Exception {
        source.addImport(TestObject.class.getName());
        URL systemResource = ClassLoader.getSystemResource("flowtest.rf");
        File flowFile = FileUtils.toFile(systemResource);
        String flowString = FileUtils.readFileToString(flowFile);
        source.add(new RuleBaseElementId(RuleBaseElementType.Process, "flowtest"), flowString);
        session = rulebase.newStatefulKnowledgeSession();
        session.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                session.halt();
            }
        });
        Thread t = new Thread() {
            @Override
            public void run() {
                session.fireUntilHalt();
            };
        };
        t.start();
        Thread.sleep(200);
        session.startProcess("flowtest");
        t.join();
        session.dispose();
    }

    @Test(timeout = 10000)
    public void testRunFlowWithEvents() throws Exception {
        source.addImport(TestObject.class.getName());
        source.addGlobal(Logger.class.getName(), "log");

        URL systemResource = ClassLoader.getSystemResource("floweventtest.rf");
        File flowFile = FileUtils.toFile(systemResource);
        String flowString = FileUtils.readFileToString(flowFile);
        source.add(new RuleBaseElementId(RuleBaseElementType.Process, "flowtest"), flowString);
        session = rulebase.newStatefulKnowledgeSession();
        session.setGlobal("log", LOGGER);
        session.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                session.halt();
            }
        });
        Thread t = new Thread() {
            @Override
            public void run() {
                session.fireAllRules();
            };
        };
        t.start();
        ProcessInstance startProcess = session.startProcess("flowtest");
        startProcess.signalEvent("TestObject", new TestObject());
        /*
         * invoking: session.signalEvent("TestObject", new TestObject()); does not work
         */
        startProcess.signalEvent("TestObject2", new TestObject());
        t.join();
        session.dispose();
    }
}
