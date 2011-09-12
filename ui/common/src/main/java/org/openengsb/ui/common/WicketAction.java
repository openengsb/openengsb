package org.openengsb.ui.common;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;

public class WicketAction {

    private Component component;
    private Action action;

    public WicketAction() {
    }

    public WicketAction(Component component, Action action) {
        this.component = component;
        this.action = action;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
