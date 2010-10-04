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
package org.openengsb.core.workflow.flow;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Event;
import org.openengsb.core.workflow.DummyNotificationDomain;
import org.openengsb.core.workflow.EventHelper;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.RuleListener;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.internal.dirsource.DirectoryRuleSource;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RuleFlowWithEventsTest {

    protected RuleManager source;
    protected RuleBase rulebase;
    protected StatefulSession session;
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

    @After
    public void cleanup() {
        File ruleDir = new File("data");
        while (ruleDir.exists()) {
            FileUtils.deleteQuietly(ruleDir);
        }
    }

    protected RuleManager getRuleBaseSource() throws RuleBaseException {
        DirectoryRuleSource source = new DirectoryRuleSource("data/rulebase");
        source.init();
        return source;
    }

    @Test
    public void testEventHelperInRuleBase() throws Exception {
        String ruleString = getStringFromResource("rulebase/org/openengsb/event1.rule");
        source.add(new RuleBaseElementId(RuleBaseElementType.Rule, "eventtest"), ruleString);

        session = rulebase.newStatefulSession();
        EventHelper eventHelper = new EventHelper();
        session.setGlobal("event", eventHelper);

        DummyNotificationDomain notification = mock(org.openengsb.core.workflow.DummyNotificationDomain.class);
        session.setGlobal("notification", notification);
        session.setGlobal("example", mock(org.openengsb.core.workflow.DummyExampleDomain.class));

        session.insert(new Event());
        new Thread() {
            @Override
            public void run() {
                session.fireAllRules();
            };
        }.start();

        Thread.sleep(200);
        eventHelper.insertEvent(new Event("foo"));
        Thread.sleep(200);
        session.dispose();

        verify(notification, times(2)).notify(anyString());
    }

    private String getStringFromResource(String name) throws IOException {
        URL systemResource = ClassLoader.getSystemResource(name);
        File resFile = FileUtils.toFile(systemResource);
        String restring = FileUtils.readFileToString(resFile);
        return restring;
    }
}
