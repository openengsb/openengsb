package org.openengsb.drools.source.dir;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.openengsb.drools.source.DirectoryRuleSource;
import org.openengsb.drools.source.RuleBaseSource;
import org.openengsb.drools.source.RuleSourceTest;

public class DirectoryRuleSourceTest extends RuleSourceTest<DirectoryRuleSource> {
    @Override
    public void setUp() throws Exception {
        File rulebaseReferenceDirectory = new File("src/test/resources/rulebase");
        File rulebaseTestDirectory = new File("data/rulebase");
        FileUtils.copyDirectory(rulebaseReferenceDirectory, rulebaseTestDirectory);
        super.setUp();
    }

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
