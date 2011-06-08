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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Represents an Event in the OpenEngSB Workflow model. It describes an Event that can take place during Runtime and
 * which Actions to call or for which other Events to wait before doing any further steps. An EventRepresentation
 * can contain ActionRepresentations as well as further EventRepresentations.
 */
@SuppressWarnings("serial")
public class EventRepresentation implements NodeRepresentation, Serializable {
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "event")
    private final List<ActionRepresentation> actions = new ArrayList<ActionRepresentation>();
    @XmlElementWrapper(name = "events")
    @XmlElement(name = "event")
    private final List<EventRepresentation> events = new ArrayList<EventRepresentation>();

    private Class<? extends org.openengsb.core.api.Event> event;

    @Override
    public final void addAction(ActionRepresentation action) {
        actions.add(action);
    }

    public final List<ActionRepresentation> getActions() {
        return actions;
    }

    @Override
    public String getDescription() {
        return event.getSimpleName();
    }

    public final Class<? extends org.openengsb.core.api.Event> getEvent() {
        return event;
    }

    public final void setEvent(Class<? extends org.openengsb.core.api.Event> event) {
        this.event = event;
    }

    public final void addEvent(EventRepresentation event) {
        this.events.add(event);
    }

    public List<EventRepresentation> getEvents() {
        return events;
    }

}
