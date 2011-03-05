/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.domain.auditing.internal;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.openengsb.core.common.workflow.RuleBaseException;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.openengsb.domain.auditing.AuditingDomain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditingConfig {

    private RuleManager ruleManager;
    private AuthenticationManager authManager;

    public final void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    public void setAuthManager(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public void init() {
        Authentication authentication = authManager.authenticate(new BundleAuthenticationToken("auditing-domain", ""));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            ruleManager.addImport(AuditingDomain.class.getCanonicalName());
            ruleManager.addGlobalIfNotPresent(AuditingDomain.class.getCanonicalName(), "auditing");
        } catch (RuleBaseException e) {
            throw new RuntimeException(e);
        }
        addRule("auditEvent");
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
