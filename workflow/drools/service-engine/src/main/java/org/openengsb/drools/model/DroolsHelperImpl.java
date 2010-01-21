package org.openengsb.drools.model;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.openengsb.drools.DroolsHelper;

public class DroolsHelperImpl implements DroolsHelper {

    RuleBase ruleBase;

    public DroolsHelperImpl(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    @Override
    public void runFlow(String flowId) {
        StatefulSession ksession = ruleBase.newStatefulSession();
        ksession.startProcess(flowId);
    }

}
