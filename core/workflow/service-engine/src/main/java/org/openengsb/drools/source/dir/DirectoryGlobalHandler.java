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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.source.DirectoryRuleSource;
import org.openengsb.drools.source.ResourceHandler;

public class DirectoryGlobalHandler extends ResourceHandler<DirectoryRuleSource> {

    private File globalsFile;

    public DirectoryGlobalHandler(DirectoryRuleSource source) {
        super(source);
        globalsFile = new File(source.getPath() + File.separator + "globals");
    }

    @Override
    public void create(String name, String code) throws RuleBaseException {
        Set<String> globalList = readGlobals();
        String line = String.format("%s %s", code, name);
        globalList.add(line);
        writeGlobals(globalList);
        source.readRuleBase();
    }

    @Override
    public void delete(String name) throws RuleBaseException {
        Set<String> globalList = readGlobals();
        Iterator<String> it = globalList.iterator();
        String line;
        for (line = it.next(); it.hasNext(); line = it.next()) {
            if (line.endsWith(" " + name)) {
                it.remove();
                break;
            }
        }
        writeGlobals(globalList);
    }

    @Override
    public String get(String name) throws RuleBaseException {
        return source.getPackage().getGlobals().get(name);
    }

    private Set<String> readGlobals() throws RuleBaseException {
        try {
            return doReadGlobals();
        } catch (IOException e) {
            throw new RuleBaseException("cannot read imports", e);
        }
    }

    private Set<String> doReadGlobals() throws IOException {
        Set<String> result = new TreeSet<String>();
        BufferedReader reader = new BufferedReader(new FileReader(globalsFile));
        String line;
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }
        reader.close();
        return result;
    }

    private void writeGlobals(Collection<String> list) throws RuleBaseException {
        try {
            doWriteGlobals(list);
        } catch (IOException e) {
            throw new RuleBaseException("cannot write imports", e);
        }
    }

    private void doWriteGlobals(Collection<String> list) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(globalsFile));
        for (String line : list) {
            bw.write(line);
            bw.newLine();
        }
        bw.close();
    }

    @Override
    public Collection<String> list() throws RuleBaseException {
        return source.getPackage().getGlobals().keySet();
    }

}
