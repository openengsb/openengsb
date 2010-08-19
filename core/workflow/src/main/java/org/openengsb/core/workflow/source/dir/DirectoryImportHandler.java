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
package org.openengsb.core.workflow.source.dir;

import java.util.Collection;
import java.util.HashSet;

import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.source.RuleBaseException;

public class DirectoryImportHandler extends SingleFileResourceHandler {

    public DirectoryImportHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public String getFileName() {
        return DirectoryRuleSource.IMPORTS_FILENAME;
    }

    @Override
    public void create(RuleBaseElementId name, String code) throws RuleBaseException {
        Collection<String> imports = readFile();
        imports.add(name.getName());
        writeFile(imports);
        source.readRuleBase();

    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        Collection<String> imports = readFile();
        imports.remove(name.getName());
        writeFile(imports);
        source.readRuleBase();
    }

    @Override
    public String get(RuleBaseElementId name) throws RuleBaseException {
        String iname = name.getName();
        if (source.getRulebase().getPackages()[0].getImports().containsKey(iname)) {
            return iname;
        }
        return null;
    }

    @Override
    public Collection<RuleBaseElementId> list() throws RuleBaseException {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (String s : source.getRulebase().getPackages()[0].getImports().keySet()) {
            result.add(new RuleBaseElementId(RuleBaseElementType.Import, s));
        }
        return result;
    }

    @Override
    public Collection<RuleBaseElementId> list(String packageName) throws RuleBaseException {
        return list();
    }

}
