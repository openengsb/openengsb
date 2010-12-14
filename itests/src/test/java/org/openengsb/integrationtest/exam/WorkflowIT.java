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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.springframework.security.access.AccessDeniedException;

@RunWith(JUnit4TestRunner.class)
public class WorkflowIT extends AbstractExamTestHelper {

    public static class DummyLogDomain extends AbstractOpenEngSBService implements ExampleDomain {
        private boolean wasCalled = false;

        @Override
        public String doSomething(String message) {
            wasCalled = true;
            return "something";
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public String doSomething(ExampleEnum exampleEnum) {
            wasCalled = true;
            return "something";
        }

        @Override
        public String doSomethingWithLogEvent(LogEvent event) {
            wasCalled = true;
            return "something";
        }

        public boolean isWasCalled() {
            return wasCalled;
        }
    }

    /**
     * Ignored because security manager is commented in the moment.
     */
    @Ignore
    @Test(expected = AccessDeniedException.class)
    public void testUserAccessToRuleManager_shouldThrowException() throws Exception {
        authenticate("user", "password");
    }

}
