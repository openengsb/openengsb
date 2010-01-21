package org.openengsb.drools.helper;

import org.drools.StatefulSession;
import org.openengsb.drools.DroolsHelper;

public class DroolsHelperImpl implements DroolsHelper {

    private StatefulSession session;

    public DroolsHelperImpl(StatefulSession session) {
        this.session = session;
    }

    @Override
    public void runFlow(String flowId) {
        session.startProcess(flowId);
    }

}
