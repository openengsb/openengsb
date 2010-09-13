package org.openengsb.ui.web.ruleeditor;

import org.openengsb.core.workflow.RuleManager;

public interface RuleManagerProvider {

    public abstract RuleManager getRuleManager();

}