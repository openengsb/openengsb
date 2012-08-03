/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;

public abstract class AbstractRuleManager implements DroolsRuleManager {

    protected KnowledgeBase rulebase;
    protected RulebaseBuilder builder;

    public AbstractRuleManager() {
        rulebase = KnowledgeBaseFactory.newKnowledgeBase();
        builder = new RulebaseBuilder(rulebase, this);
    }

    @Override
    public KnowledgeBase getRulebase() {
        return rulebase;
    }

    @Override
    public String getInstanceId() {
        return getClass().getName();
    }

    @Override
    public void addGlobalIfNotPresent(String className, String name) throws RuleBaseException {
        String currentType = listGlobals().get(name);
        if (currentType == null) {
            addGlobal(className, name);
            return;
        }
        if (!currentType.equals(className)) {
            throw new IllegalArgumentException(
                String
                    .format(
                        "Unable to add global of type %s."
                                + "Global with the same name but different type is already registered (%s)",
                        className, currentType));
        }
    }

    @Override
    public void addOrUpdate(RuleBaseElementId name, String code) throws RuleBaseException {
        if (get(name) == null) {
            add(name, code);
        } else {
            update(name, code);
        }
    }

    @Override
    public Collection<String> getAllGlobalsOfType(String type) {
        Set<String> result = new HashSet<String>();
        for (Map.Entry<String, String> entry : listGlobals().entrySet()) {
            if (entry.getValue().equals(type)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public String getGlobalType(String name) {
        return this.listGlobals().get(name);
    }
}
