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

import static org.mockito.Mockito.mock;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.workflow.RuleManager;

public abstract class AbstractRuleManagerTest {
    protected RuleManager source;
    protected KnowledgeBase rulebase;
    protected StatefulKnowledgeSession session;
    protected RuleListener listener;

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

    protected abstract RuleManager getRuleBaseSource() throws Exception;

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
        session.setGlobal("example", mock(DummyExampleDomain.class));
    }

    /**
     * inserts an Event into the existing session and fires All rules
     */
    protected void executeTestSession() {
        Event event = new Event();
        session.insert(event);
        session.fireAllRules();
    }
}
