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
package org.openengsb.drools.source.dir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.drools.rule.Package;
import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.message.RuleBaseElementId;
import org.openengsb.drools.message.RuleBaseElementType;
import org.openengsb.drools.source.DirectoryRuleSource;
import org.openengsb.drools.source.ResourceHandler;

public class DirectoryFunctionHandler extends ResourceHandler<DirectoryRuleSource> {

    public DirectoryFunctionHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public void create(RuleBaseElementId name, String code) throws RuleBaseException {
        File funcFile = source.getFilePath(name);
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
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        File ruleFile = source.getFilePath(name);
        if (!ruleFile.exists()) {
            // fail silently if the function does not exist
            return;
        }
        ruleFile.delete();
        source.getRulebase().removeFunction(name.getPackageName(), name.getName());
    }

    @Override
    public String get(RuleBaseElementId name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<RuleBaseElementId> list() throws RuleBaseException {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (Package p : source.getRulebase().getPackages()) {
            String packageName = p.getName();
            for (String s : p.getFunctions().keySet()) {
                result.add(new RuleBaseElementId(RuleBaseElementType.Function, packageName, s));
            }
        }
        return result;
    }

    @Override
    public Collection<RuleBaseElementId> list(String packageName) throws RuleBaseException {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (String name : source.getRulebase().getPackage(packageName).getFunctions().keySet()) {
            result.add(new RuleBaseElementId(RuleBaseElementType.Function, packageName, name));
        }
        return result;
    }

}
