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
import java.util.Iterator;
import java.util.Set;

import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.source.DirectoryRuleSource;

public class DirectoryGlobalHandler extends SingleFileResourceHandler {

    public DirectoryGlobalHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public String getFileName() {
        return DirectoryRuleSource.GLOBALS_FILENAME;
    }

    @Override
    public void create(String name, String code) throws RuleBaseException {
        Set<String> globalList = readFile();
        String line = String.format("%s %s", code, name);
        globalList.add(line);
        writeFile(globalList);
        source.readRuleBase();
    }

    @Override
    public void delete(String name) throws RuleBaseException {
        Set<String> globalList = readFile();
        Iterator<String> it = globalList.iterator();
        String line;
        for (line = it.next(); it.hasNext(); line = it.next()) {
            if (line.endsWith(" " + name)) {
                it.remove();
                break;
            }
        }
        writeFile(globalList);
    }

    @Override
    public String get(String name) throws RuleBaseException {
        return source.getPackage().getGlobals().get(name);
    }

    @Override
    public Collection<String> list() throws RuleBaseException {
        return source.getPackage().getGlobals().keySet();
    }

}
