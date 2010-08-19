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
package org.openengsb.core.workflow.internal.dirsource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.drools.rule.Package;
import org.openengsb.core.workflow.internal.ResourceHandler;
import org.openengsb.core.workflow.internal.RuleBaseException;
import org.openengsb.core.workflow.model.RuleBaseElementId;

public abstract class MultiFileResourceHandler extends ResourceHandler<DirectoryRuleSource> {

    public MultiFileResourceHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public void create(RuleBaseElementId name, String code) throws RuleBaseException {
        File resFile = source.getFilePath(name);
        if (resFile.exists()) {
            throw new RuleBaseException(String.format("File already exists \"%s\".", resFile));
        }
        if (!resFile.getParentFile().exists()) {
            resFile.getParentFile().mkdirs();
        }
        FileWriter fw;
        try {
            fw = new FileWriter(resFile);
            fw.append(code);
            fw.close();
        } catch (IOException e) {
            throw new RuleBaseException(String.format("could not write the %s to the filesystem", name.getType()), e);
        }
        source.readPackage(name.getPackageName());
    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        File resFile = source.getFilePath(name);
        if (!resFile.exists()) {
            // fail silently if the element does not exist
            return;
        }
        resFile.delete();
        removeFromRuleBase(name);
    }

    protected abstract void removeFromRuleBase(RuleBaseElementId name) throws RuleBaseException;

    @Override
    public String get(RuleBaseElementId name) {
        File ruleFile = source.getFilePath(name);
        try {
            return IOUtils.toString(new FileReader(ruleFile));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Collection<RuleBaseElementId> list() throws RuleBaseException {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (Package p : source.getRulebase().getPackages()) {
            result.addAll(listElementsInPackage(p));
        }
        return result;
    }

    protected abstract Collection<RuleBaseElementId> listElementsInPackage(Package p);

    @Override
    public Collection<RuleBaseElementId> list(String packageName) throws RuleBaseException {
        return listElementsInPackage(source.getRulebase().getPackage(packageName));
    }
}
