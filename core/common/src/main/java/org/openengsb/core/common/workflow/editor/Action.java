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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.common.Domain;

public class Action implements Serializable, Node {
    private final List<Action> actions = new ArrayList<Action>();
    private final List<Event> events = new ArrayList<Event>();

    private Class<? extends Domain> domain;
    private String methodName;

    private List<Class<?>> methodParameters = new ArrayList<Class<?>>();
    private String location;

    public final List<Action> getActions() {
        return actions;
    }

    public final List<Event> getEvents() {
        return events;
    }

    public final void addAction(Action action) {
        this.actions.add(action);
    }

    public final void addEvent(Event event) {
        this.events.add(event);
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

}
