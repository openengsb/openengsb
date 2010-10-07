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
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.drools.definition.KnowledgePackage;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

public class DirectoryFunctionHandler extends MultiFileResourceHandler {

    public DirectoryFunctionHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    protected Collection<RuleBaseElementId> listElementsInPackage(KnowledgePackage p) {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        String path = p.getName().replace(".", File.separator);
        File directory = new File(source.getPath(), path);
        @SuppressWarnings("unchecked")
        Collection<File> listFiles =
            FileUtils.listFiles(directory, new String[]{ DirectoryRuleSource.FUNC_EXTENSION }, false);

        for (File f : listFiles) {
            String s = f.getName().replace("." + DirectoryRuleSource.FUNC_EXTENSION, "");
            result.add(new RuleBaseElementId(RuleBaseElementType.Function, p.getName(), s));
        }
        return result;
    }

    @Override
    protected void removeFromRuleBase(RuleBaseElementId name) {
        source.getRulebase().removeFunction(name.getPackageName(), name.getName());
    }

}
