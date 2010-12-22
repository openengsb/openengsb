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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.workflow.model.RemoteEvent;
import org.openengsb.core.workflow.model.TestEvent;

public class RegistrationServiceTest extends AbstractWorkflowServiceTest {

    @Override
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testWrapRemoteEvent() throws Exception {
        TestEvent event = new TestEvent(3L);
        event.setTestProperty("bla");
        RemoteEvent wrapEvent = RemoteEventUtil.wrapEvent(event);
        Map<String, String> properties = wrapEvent.getNestedEventProperties();
        assertThat(wrapEvent.getType(), is(TestEvent.class.getName()));
        assertThat(properties.get("processId"), is("3"));
    }
}
