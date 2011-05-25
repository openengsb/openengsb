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

import org.openengsb.core.api.Domain;

@SuppressWarnings("serial")
public class ActionRepresentation implements Serializable, NodeRepresentation {
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "event")
    private final List<ActionRepresentation> actions = new ArrayList<ActionRepresentation>();
    @XmlElementWrapper(name = "events")
    @XmlElement(name = "event")
    private final List<EventRepresentation> events = new ArrayList<EventRepresentation>();

    private EndRepresentation end;

    private Class<? extends Domain> domain;
    private String methodName;

    private String code;

    private List<Class<?>> methodParameters = new ArrayList<Class<?>>();
    private String location;

    public final List<ActionRepresentation> getActions() {
        return actions;
    }

    public final List<EventRepresentation> getEvents() {
        return events;
    }

    @Override
    public final void addAction(ActionRepresentation action) {
        actions.add(action);
    }

    public final void addEvent(EventRepresentation event) {
        events.add(event);
    }

    public final Class<? extends Domain> getDomain() {
        return domain;
    }

    public final void setDomain(Class<? extends Domain> domain) {
        this.domain = domain;
    }

    public final String getLocation() {
        return location;
    }

    public final void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getDescription() {
        if (domain != null && location != null && methodName != null) {
            return domain.getSimpleName() + ":" + methodName + "@" + location;
        } else {
            return "Root";
        }
    }

    public final String getMethodName() {
        return methodName;
    }

    public final void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public final List<Class<?>> getMethodParameters() {
        return methodParameters;
    }

    public void setMethodParameters(List<Class<?>> methodParameters) {
        this.methodParameters = methodParameters;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public EndRepresentation getEnd() {
        if (isLeaf()) {
            return this.end;
        } else {
            return null;
        }
    }

    public boolean hasSharedEnd() {
        return end != null;
    }

    public void setEnd(EndRepresentation end) {
        this.end = end;
    }

    public boolean isLeaf() {
        return actions.isEmpty() && events.isEmpty();
    }

}
