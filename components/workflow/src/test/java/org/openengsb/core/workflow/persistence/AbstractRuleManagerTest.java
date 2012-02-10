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

package org.openengsb.core.workflow.persistence;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;

public abstract class AbstractRuleManagerTest {
    protected RuleManager ruleManager;
    protected KnowledgeBase rulebase;
    protected StatefulKnowledgeSession session;
    protected RuleListener listener;

    @Before
    public void setUp() throws Exception {
        ruleManager = PersistenceTestUtil.getRuleManagerWithPersistenceService();
        rulebase = ruleManager.getRulebase();
    }

    @After
    public void tearDown() throws Exception {
        PersistenceTestUtil.cleanup();
        if (session != null) {
            session.dispose();
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        PersistenceTestUtil.createReferencePersistence();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        PersistenceTestUtil.cleanupReferenceData();
    }

    /**
     * create new stateful session from the rulebase and attach a listener to validate testresults
     */
    protected void createSession() {
        if (session != null) {
            session.dispose();
            session = null;
        }
        session = rulebase.newStatefulKnowledgeSession();
        listener = new RuleListener();
        session.addEventListener(listener);
        ExampleDomain exampleService = new ExampleDomain() {

            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public AliveState getAliveState() {
                return null;
            }

            @Override
            public String doSomethingWithLogEvent(LogEvent event) {
                return null;
            }

            @Override
            public String doSomething(ExampleEnum exampleEnum) {
                return null;
            }

            @Override
            public String doSomething(String message) {
                return null;
            }

            @Override
            public ExampleResponseModel doSomething(ExampleRequestModel model) {
                return null;
            }
        };
        session.setGlobal("example2", exampleService);
    }

    /**
     * inserts an Event into the existing session and fires All rules
     */
    protected void executeTestSession() {
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        session.insert(event);
        session.fireAllRules();
    }
}
