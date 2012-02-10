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

package org.openengsb.core.services;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.apache.commons.lang.reflect.MethodUtils;
import org.junit.Test;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.workflow.WorkflowService;

public class MethodFinderTest {

    public interface TestEvent extends Event {

    }

    @Test
    public void testFindMethodWithSubclass_shouldReturnSameMethod() throws Exception {
        Method matchingAccessibleMethod =
            MethodUtils.getMatchingAccessibleMethod(WorkflowService.class, "processEvent",
                new Class[]{ TestEvent.class });
        assertThat(matchingAccessibleMethod, is(WorkflowService.class.getMethod("processEvent", Event.class)));
    }
}
