package org.openengsb.drools.source.dir;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.openengsb.drools.source.DirectoryRuleSource;
import org.openengsb.drools.source.RuleBaseSource;
import org.openengsb.drools.source.RuleSourceTest;

public class DirectoryRuleSourceEmtpyTest extends RuleSourceTest<DirectoryRuleSource> {
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(new File("data"));
    }

    @Override
    protected RuleBaseSource getRuleBaseSource() {
        return new DirectoryRuleSource("data/rulebase");
    }
}
