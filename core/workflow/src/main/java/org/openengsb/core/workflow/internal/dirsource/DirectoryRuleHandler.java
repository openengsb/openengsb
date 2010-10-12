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

import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

public class DirectoryRuleHandler extends MultiFileResourceHandler {

    public DirectoryRuleHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    protected Collection<RuleBaseElementId> listElementsInPackage(KnowledgePackage p) {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (Rule rule : p.getRules()) {
            result.add(new RuleBaseElementId(RuleBaseElementType.Rule, p.getName(), rule.getName()));
        }
        return result;
    }

    @Override
    protected void removeFromRuleBase(RuleBaseElementId name) {
        source.getRulebase().removeRule(name.getPackageName(), name.getName());
    }
}
