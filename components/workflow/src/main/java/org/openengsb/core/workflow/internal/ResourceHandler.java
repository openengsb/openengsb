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
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;

public abstract class ResourceHandler<SourceType extends RuleManager> {

    protected SourceType source;

    public ResourceHandler(SourceType source) {
        this.source = source;
    }

    public abstract void create(RuleBaseElementId name, String code) throws RuleBaseException;

    public abstract String get(RuleBaseElementId name);

    public void update(RuleBaseElementId name, String code) throws RuleBaseException {
        String oldelement = get(name);
        delete(name);
        try {
            create(name, code);
        } catch (RuleBaseException e) {
            create(name, oldelement);
            throw e;
        }
    }

    public abstract void delete(RuleBaseElementId name) throws RuleBaseException;

    public abstract Collection<RuleBaseElementId> list();

    public abstract Collection<RuleBaseElementId> list(String packageName);

}
