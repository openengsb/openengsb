package org.openengsb.drools;

import java.util.HashSet;
import java.util.Set;

import org.drools.WorkingMemory;
import org.drools.event.AfterActivationFiredEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.drools.rule.Rule;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RuleListener extends DefaultAgendaEventListener {
    protected int numFired = 0;
    protected Set<String> rulesFired = new HashSet<String>();

    @Override
    public void afterActivationFired(AfterActivationFiredEvent event, WorkingMemory workingMemory) {
        Rule rule = event.getActivation().getRule();
        rulesFired.add(rule.getName());
        String fqName = rule.getPackageName() + "." + rule.getName();
        rulesFired.add(fqName);
        numFired++;
        super.afterActivationFired(event, workingMemory);
    }

    public boolean haveRulesFired(String... names) {
        return rulesFired.containsAll(Arrays.asList(names));
    }
}
