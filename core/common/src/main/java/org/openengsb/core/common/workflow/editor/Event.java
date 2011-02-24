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
