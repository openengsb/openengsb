/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.internal.dirsource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.internal.ResourceHandler;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

public class DirectoryGlobalHandler extends ResourceHandler<DirectoryRuleSource> {

    private File file;

    public DirectoryGlobalHandler(DirectoryRuleSource source) {
        super(source);
        file = new File(source.getPath() + File.separator + DirectoryRuleSource.GLOBALS_FILENAME);
    }

    @Override
    public void create(RuleBaseElementId name, String code) throws RuleBaseException {
        Collection<String> globalList = readGlobalList();
        if (globalList.contains(name.getName())) {
            throw new RuleBaseException(String.format("global with name %s already registered", name.getName()));
        }
        String line = String.format("%s %s", code, name.getName());
        globalList.add(line);

        try {
            FileUtils.writeLines(file, globalList, IOUtils.LINE_SEPARATOR_UNIX);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        source.readRuleBase();
    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        Collection<String> globalList = readGlobalList();
        for (Iterator<String> it = globalList.iterator(); it.hasNext();) {
            String line = it.next();
            if (line.endsWith(" " + name.getName())) {
                it.remove();
                break;
            }
        }
        try {
            FileUtils.writeLines(file, globalList, IOUtils.LINE_SEPARATOR_UNIX);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        source.readRuleBase();
    }

    @Override
    public String get(RuleBaseElementId name) {
        // return source.getRulebase().getPackages()[0].getGlobals().get(name.getName());
        return null;
    }

    @Override
    public Collection<RuleBaseElementId> list() {
        Collection<String> globalList = readGlobalList();
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (String s : globalList) {
            String[] parts = s.split(" ");
            result.add(new RuleBaseElementId(RuleBaseElementType.Global, parts[1]));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> readGlobalList() {
        Collection<String> globalList;
        try {
            globalList = FileUtils.readLines(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return globalList;
    }

    @Override
    public Collection<RuleBaseElementId> list(String packageName) {
        return list();
    }

}
