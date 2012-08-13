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
package org.openengsb.core.workflow.drools.internal;

import org.drools.KnowledgeBase;
import org.openengsb.core.workflow.api.RuleManager;

/**
 * Extension of the RuleManager interface for this JBoss Drools-based implementations.
 * It just provides an additional method to translate the managed rules into a drools-rulebase
 */
public interface DroolsRuleManager extends RuleManager {


    /**
     * provides a reference to the rulebase. This reference remains valid as long as the bundle is active. the rulebase
     * is modified "on-the-fly".
     *
     * @return reference to the rulebase
     */
    KnowledgeBase getRulebase();
}
