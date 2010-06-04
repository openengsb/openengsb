/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
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
