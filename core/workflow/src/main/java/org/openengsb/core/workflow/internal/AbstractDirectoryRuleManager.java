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

import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;

public abstract class AbstractDirectoryRuleManager extends AbstractRuleManager {

    protected ResourceHandler<?> getRessourceHandler(RuleBaseElementType element) {
        throw new UnsupportedOperationException("not implemented for type " + getClass());
    }

    @Override
    public void add(RuleBaseElementId name, String code) throws RuleBaseException {
        getRessourceHandler(name.getType()).create(name, code);
    }

    @Override
    public String get(RuleBaseElementId name) {
        return getRessourceHandler(name.getType()).get(name);
    }

    @Override
    public void update(RuleBaseElementId name, String newCode) throws RuleBaseException {
        getRessourceHandler(name.getType()).update(name, newCode);
    }

    @Override
    public void delete(RuleBaseElementId name) throws RuleBaseException {
        getRessourceHandler(name.getType()).delete(name);
    }

    @Override
    public Collection<RuleBaseElementId> list(RuleBaseElementType type) {
        return getRessourceHandler(type).list();
    }

    @Override
    public Collection<RuleBaseElementId> list(RuleBaseElementType type, String packageName) {
        return getRessourceHandler(type).list(packageName);
    }
}
