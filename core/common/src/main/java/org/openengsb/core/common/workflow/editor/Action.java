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
