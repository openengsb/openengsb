package org.openengsb.drools.dir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openengsb.drools.DirectoryRuleSource;
import org.openengsb.drools.RuleBaseException;

public class DirectoryFunctionHandler extends ResourceHandler<DirectoryRuleSource> {

    public static final String EXTENSION = ".func";

    public DirectoryFunctionHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public void create(String name, String code) throws RuleBaseException {
        String filename = name + EXTENSION;
        File funcFile = new File(source.getPath() + File.separator + filename);
        if (funcFile.exists()) {
            throw new RuleBaseException("File already exists");
        }
        FileWriter fw;
        try {
            fw = new FileWriter(funcFile);
            fw.append(code);
            fw.close();
        } catch (IOException e) {
            throw new RuleBaseException("could not write the function to the filesystem", e);
        }
        source.readRuleBase();
    }

    @Override
    public void delete(String name) throws RuleBaseException {
        String filename = name + EXTENSION;
        File ruleFile = new File(source.getPath() + File.separator + filename);
        if (!ruleFile.exists()) {
            // fail silently if the function does not exist
            return;
        }
        ruleFile.delete();
        source.getRulebase().removeFunction("org.openengsb", name);
    }

    @Override
    public String get(String name) {
        // TODO Auto-generated method stub
        return null;
    }

}
