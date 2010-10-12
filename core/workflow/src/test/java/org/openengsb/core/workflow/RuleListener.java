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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.drools.definition.rule.Rule;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;

public class RuleListener extends DefaultAgendaEventListener {
    private int numFired = 0;
    private Set<String> rulesFired = new HashSet<String>();

    @Override
    public void afterActivationFired(AfterActivationFiredEvent event) {
        Rule rule = event.getActivation().getRule();
        rulesFired.add(rule.getName());
        String fqName = rule.getPackageName() + "." + rule.getName();
        rulesFired.add(fqName);
        numFired++;
        super.afterActivationFired(event);
    }

    public boolean haveRulesFired(String... names) {
        return rulesFired.containsAll(Arrays.asList(names));
    }
}
