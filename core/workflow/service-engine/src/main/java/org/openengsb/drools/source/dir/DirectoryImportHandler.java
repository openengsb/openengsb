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

import java.util.Collection;

import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.source.DirectoryRuleSource;

public class DirectoryImportHandler extends SingleFileResourceHandler {

    public DirectoryImportHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public String getFileName() {
        return DirectoryRuleSource.IMPORTS_FILENAME;
    }

    @Override
    public void create(String name, String code) throws RuleBaseException {
        Collection<String> imports = readFile();
        imports.add(name);
        writeFile(imports);
        System.err.println("reread rulebase");
        source.readRuleBase();

    }

    @Override
    public void delete(String name) throws RuleBaseException {
        Collection<String> imports = readFile();
        imports.remove(name);
        writeFile(imports);
        source.getRulebase().getPackage("org.openengsb").removeImport(name);
    }

    @Override
    public String get(String name) throws RuleBaseException {
        if (source.getPackage().getImports().containsKey(name)) {
            return name;
        }
        return null;
    }

    @Override
    public Collection<String> list() throws RuleBaseException {
        return source.getPackage().getImports().keySet();
    }
}
