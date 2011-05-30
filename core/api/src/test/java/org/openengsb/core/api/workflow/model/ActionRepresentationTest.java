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

package org.openengsb.core.api.workflow.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActionRepresentationTest {
    @Test
    public void hasNoActionsOrEvents_ShouldBeLeaf() {
        ActionRepresentation action = new ActionRepresentation();
        assertThat(action.isLeaf(), equalTo(true));
        EndRepresentation end = new EndRepresentation();
        action.setEnd(end);
        assertThat(action.getEnd(), sameInstance(end));
    }

    @Test
    public void hasActions_ShouldNotBeLeaf() {
        ActionRepresentation action = new ActionRepresentation();
        action.addAction(new ActionRepresentation());
        assertThat(action.isLeaf(), equalTo(false));
        assertThat(action.getEnd(), equalTo(null));
    }

    @Test
    public void hasEvent_ShouldNotBeLeaf() {
        ActionRepresentation action = new ActionRepresentation();
        action.addEvent(new EventRepresentation());
        assertThat(action.isLeaf(), equalTo(false));
        assertThat(action.getEnd(), equalTo(null));
    }

    @Test
    public void hasNoEnd_ShoulReturnFalseForHasSharedEnd() {
        assertFalse(new ActionRepresentation().hasSharedEnd());
    }

    @Test
    public void hasSharedEnd_ShoulReturnTrueForHasSharedEnd() {
        ActionRepresentation action = new ActionRepresentation();
        action.setEnd(new EndRepresentation());
        assertTrue(action.hasSharedEnd());
    }
}
