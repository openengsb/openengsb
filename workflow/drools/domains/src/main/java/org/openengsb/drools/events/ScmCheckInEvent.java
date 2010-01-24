package org.openengsb.drools.events;

import org.openengsb.core.model.Event;

public class ScmCheckInEvent extends Event {

    public ScmCheckInEvent() {
        super("scm", "scmCheckInEvent");
    }

}
