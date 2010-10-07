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

import org.apache.commons.io.FileUtils;
import org.drools.definition.KnowledgePackage;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.internal.ResourceHandler;
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
        try {
            code = sanitize(name, code);
            FileUtils.writeStringToFile(resFile, code);
        } catch (IOException e) {
            throw new RuleBaseException(String.format("could not write the %s to the filesystem", name.getType()), e);
        }
        try {
            source.readPackage(name.getPackageName());
        } catch (RuleBaseException e) {
            safeDelete(resFile);
            throw e;
        }
    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        File resFile = source.getFilePath(name);
        if (!resFile.exists()) {
            // fail silently if the element does not exist
            return;
        }
        safeDelete(resFile);
        removeFromRuleBase(name);
    }

    private void safeDelete(File resFile) throws RuleBaseException {
        try {
            FileUtils.forceDelete(resFile);
        } catch (IOException e) {
            throw new RuleBaseException(e);
        }
    }

    protected abstract void removeFromRuleBase(RuleBaseElementId name) throws RuleBaseException;

    @Override
    public String get(RuleBaseElementId name) {
        File ruleFile = source.getFilePath(name);
        try {
            return FileUtils.readFileToString(ruleFile);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Collection<RuleBaseElementId> list() {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (KnowledgePackage p : source.getRulebase().getKnowledgePackages()) {
            result.addAll(listElementsInPackage(p));
        }
        return result;
    }

    protected abstract Collection<RuleBaseElementId> listElementsInPackage(KnowledgePackage p);

    public String sanitize(RuleBaseElementId name, String code) {
        return code;
    }

    @Override
    public Collection<RuleBaseElementId> list(String packageName) {
        return listElementsInPackage(source.getRulebase().getKnowledgePackage(packageName));
    }
}
