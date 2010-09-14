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

package org.openengsb.core.workflow;

import java.util.Collection;

import org.drools.RuleBase;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

public interface RuleManager {

    /**
     * provides a reference to the rulebase. This reference remains valid as
     * long as the bundle is active. the rulebase is modified "on-the-fly".
     *
     * @return reference to the rulebase
     */
    RuleBase getRulebase();

    /**
     *
     * adds a new element to the rulebase
     *
     * @throws RuleBaseException if adding the new element would cause the
     *         rulebase to be invalid (e.g. parse error).
     */
    void add(RuleBaseElementId name, String code) throws RuleBaseException;

    String get(RuleBaseElementId name);

    /**
     * updates an existing element
     *
     * @throws RuleBaseException if the update would cause the rulebase to be
     *         invalid (e.g. parse error).
     */
    void update(RuleBaseElementId name, String newCode) throws RuleBaseException;

    /**
     *
     * @throws RuleBaseException if the deleting the element would cause the
     *         rulebase to be invalid (e.g. parse error).
     */
    void delete(RuleBaseElementId name) throws RuleBaseException;

    Collection<RuleBaseElementId> list(RuleBaseElementType type);

    Collection<RuleBaseElementId> list(RuleBaseElementType type, String packageName);

}