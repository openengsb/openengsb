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

package org.openengsb.domain.auditing.internal;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.security.SecurityUtils;
import org.openengsb.domain.auditing.AuditingDomain;

public class AuditingConfig {

    private RuleManager ruleManager;

    public final void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    public void init() {
        try {
            SecurityUtils.executeWithSystemPermissions(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    ruleManager.addImport(AuditingDomain.class.getCanonicalName());
                    ruleManager.addGlobalIfNotPresent(AuditingDomain.class.getCanonicalName(), "auditing");
                    addRule("auditEvent");
                    return null;
                }
            });
        } catch (ExecutionException e1) {
            throw new RuntimeException(e1);
        }

    }

    private void addRule(String rule) {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(rule + ".rule");
            String ruleText = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, rule);
            ruleManager.addOrUpdate(id, ruleText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
