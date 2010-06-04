package org.openengsb.drools.dir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openengsb.drools.DirectoryRuleSource;
import org.openengsb.drools.RuleBaseException;

public class DirectoryRuleHandler extends ResourceHandler<DirectoryRuleSource> {

    // do not use .drl because we don't create valid drls
    public static final String EXTENSION = ".rule";

    public DirectoryRuleHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public void create(String name, String code) throws RuleBaseException {
        String filename = name + EXTENSION;
        File ruleFile = new File(source.getPath() + File.separator + filename);
        if (ruleFile.exists()) {
            throw new RuleBaseException("File already exists");
        }
        FileWriter fw;
        try {
            fw = new FileWriter(ruleFile);
            fw.append(code);
            fw.close();
        } catch (IOException e) {
            // ruleFile.delete();
            throw new RuleBaseException("could not write the rule to the filesystem", e);
        }
        source.readRuleBase();
    }

    @Override
    public void delete(String name) throws RuleBaseException {
        String filename = name + EXTENSION;
        File ruleFile = new File(source.getPath() + File.separator + filename);
        if (!ruleFile.exists()) {
            // fail silently if the rule does not exist
            return;
        }
        ruleFile.delete();
        source.getRulebase().removeRule("org.openengsb", name);
    }

    @Override
    public String get(String name) {
        // TODO Auto-generated method stub
        return null;
    }

}
