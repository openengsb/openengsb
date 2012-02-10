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
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.workflow.EventRegistrationService;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RemoteEvent;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationServiceImpl extends AbstractOpenEngSBService implements EventRegistrationService {

    private static final String EVENT_REGISTRATION_RULE_TEMPLATE = "when event : %s\n"
            + "then\n"
            + "RemoteEvent re = (RemoteEvent)ModelUtils.createEmptyModelObject(RemoteEvent.class, new OpenEngSBModelEntry[]{});\n"
            + "re.setType(event.getType());\n"
            + "%s\n";
    private static final String OSGI_HELPER_TEMPLATE1 = "OsgiHelper.sendRemoteEvent(\"%s\", \"%s\", re);";
    private static final String OSGI_HELPER_TEMPLATE2 =
        "OsgiHelper.sendRemoteEvent(\"%s\", \"%s\", re, \"%s\");";

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private RuleManager ruleManager;

    @Override
    public synchronized void registerEvent(RemoteEvent event, String portId, String returnAddress, String serviceId) {
        String name =
            String.format("Notify %s via %s when %s occurs", returnAddress.toString(), portId, event.getClassName());
        name = getUniqueRuleName(name);
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, name);
        String eventMatcher = makeEventMatcher(event);
        try {
            String osgiHelperStatement;
            if (serviceId == null) {
                osgiHelperStatement = String.format(OSGI_HELPER_TEMPLATE1, portId, returnAddress);
            } else {
                osgiHelperStatement = String.format(OSGI_HELPER_TEMPLATE2, portId, returnAddress, serviceId);
            }
            ruleManager.add(id, String.format(EVENT_REGISTRATION_RULE_TEMPLATE, eventMatcher, osgiHelperStatement));
        } catch (RuleBaseException e) {
            throw new IllegalArgumentException(e);
        }
        LOGGER.info("registering Event: {}", event);
    }

    private String getUniqueRuleName(String name) {
        Collection<RuleBaseElementId> list = ruleManager.list(RuleBaseElementType.Rule);
        while (list.contains(new RuleBaseElementId(RuleBaseElementType.Rule, name))) {
            name = name + "_";
        }
        return name;
    }

    @Override
    public void registerEvent(RemoteEvent reg, String portId, String returnAddress) {
        registerEvent(reg, portId, returnAddress, null);
    }

    private String makeEventMatcher(RemoteEvent event) {
        List<String> matchers = new LinkedList<String>();
        Set<Entry<String, String>> entrySet = event.getNestedEventProperties().entrySet();
        for (Entry<String, String> entry : entrySet) {
            matchers.add(String.format("%s == \"%s\"", entry.getKey(), entry.getValue()));
        }
        return event.getClassName() + "(" + StringUtils.join(matchers, ",") + ")";
    }

    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

}
