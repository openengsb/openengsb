package org.openengsb.drools.source.dir;

import java.io.File;
import java.util.TimerTask;

import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.source.DirectoryRuleSource;

public class ReloadChecker extends TimerTask {

    protected File file;
    protected DirectoryRuleSource ruleSource;

    public ReloadChecker(File file, DirectoryRuleSource ruleSource) {
        super();
        this.file = file;
        this.ruleSource = ruleSource;
    }

    @Override
    public void run() {
        if (file.exists()) {
            try {
                ruleSource.readRuleBase();
            } catch (RuleBaseException e) {
                throw new RuntimeException(e);
            }
            file.delete();
        }

    }

}
