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

package org.openengsb.core.common.workflow.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Node, Serializable {
    private final List<Action> actions = new ArrayList<Action>();

    private Class<? extends org.openengsb.core.common.Event> event;

    public final void addAction(Action action) {
        this.actions.add(action);
    }

    public final List<Action> getActions() {
        return actions;
    }

    @Override
    public String getDescription() {
        return event.getSimpleName();
    }

    public final Class<? extends org.openengsb.core.common.Event> getEvent() {
        return event;
    }

    public final void setEvent(Class<? extends org.openengsb.core.common.Event> event) {
        this.event = event;
    }

}
