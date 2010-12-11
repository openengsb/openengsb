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

package org.openengsb.ui.web.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.workflow.RuleBaseException;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
import org.openengsb.domain.auditing.AuditingDomain;

public class AuditingConfig {

    @SpringBean
    private RuleManager ruleManager;

    @SpringBean
    private DomainService domainService;

    public final void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    public final void setDomainService(DomainService domainService) {
        this.domainService = domainService;
    }

    public void init() {
        try {
            ruleManager.addImport(AuditingDomain.class.getCanonicalName());
            try {
                ruleManager.addGlobal(AuditingDomain.class.getCanonicalName(), "auditing");
            } catch (RuntimeException e) {
                // thrown if there is already one global auditing... fine then, go on
            }
            addRule("auditEvent");
            List<ServiceManager> serviceManagersForDomain =
                domainService.serviceManagersForDomain(AuditingDomain.class);
            if (serviceManagersForDomain.size() > 0) {
                String defaultConnectorID = "auditing";
                serviceManagersForDomain.get(0).update(defaultConnectorID, new HashMap<String, String>());
            }
        } catch (RuleBaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addRule(String rule) {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(rule + ".rule");
            String ruleText = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, rule);
            ruleManager.add(id, ruleText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
