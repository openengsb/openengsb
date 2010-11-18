package org.openengsb.core.workflow.internal;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.openengsb.core.common.workflow.RuleBaseException;
import org.openengsb.core.common.workflow.RuleManager;

public abstract class AbstractRuleManager implements RuleManager {

    protected KnowledgeBase rulebase;
    protected RulebaseBuilder builder;

    public AbstractRuleManager() {
        rulebase = KnowledgeBaseFactory.newKnowledgeBase();
        builder = new RulebaseBuilder(rulebase, this);
    }

    @Override
    public KnowledgeBase getRulebase() {
        return rulebase;
    }

    public void init() throws RuleBaseException {
        builder.reloadRulebase();
    }

}
