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

import java.util.Collection;
import java.util.HashSet;

import org.drools.rule.Package;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

public class DirectoryProcessHandler extends MultiFileResourceHandler {

    public DirectoryProcessHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    public void create(RuleBaseElementId name, String code) throws RuleBaseException {
        super.create(name, code);
    }

    @Override
    protected void removeFromRuleBase(RuleBaseElementId name) throws RuleBaseException {
        source.getRulebase().getPackage(name.getPackageName()).removeRuleFlow(name.getName());
    }

    @Override
    protected Collection<RuleBaseElementId> listElementsInPackage(Package p) {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (String name : p.getRuleFlows().keySet()) {
            result.add(new RuleBaseElementId(RuleBaseElementType.Process, p.getName(), name));
        }
        return result;
    }

    @Override
    public String sanitize(RuleBaseElementId name, String code) {
        String replacedId = code.replaceFirst("id=\"\\w*\"", String.format("id=\"%s\"", name.getName()));
        String replacedPackage = replacedId.replaceFirst("package-name=\"\\w*\"",
                String.format("package-name=\"%s\"", name.getPackageName()));
        return replacedPackage;
    }
}
