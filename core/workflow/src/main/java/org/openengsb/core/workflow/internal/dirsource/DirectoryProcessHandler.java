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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.process.Process;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

public class DirectoryProcessHandler extends MultiFileResourceHandler {

    private final Log log = LogFactory.getLog(DirectoryProcessHandler.class);

    public DirectoryProcessHandler(DirectoryRuleSource source) {
        super(source);
    }

    @Override
    protected void removeFromRuleBase(RuleBaseElementId name) throws RuleBaseException {
        source.getRulebase().removeProcess(name.getName());
    }

    @Override
    protected Collection<RuleBaseElementId> listElementsInPackage(KnowledgePackage p) {
        Collection<RuleBaseElementId> result = new HashSet<RuleBaseElementId>();
        for (Process process : p.getProcesses()) {
            String name = process.getId();
            result.add(new RuleBaseElementId(RuleBaseElementType.Process, p.getName(), name.replace(p.getName() + ".",
                    "")));
        }
        return result;
    }

    @Override
    public String sanitize(RuleBaseElementId name, String code) {
        String replacedId = code.replaceFirst("id=\"[^\"]+\"",
                String.format("id=\"%s\"", name.getName()));
        String replacedPackage = replacedId.replaceFirst("package-name=\"[^\"]+\"",
                String.format("package-name=\"%s\"", name.getPackageName()));
        log.debug("sanitized code: " + replacedPackage);
        return replacedPackage;
    }
}
