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

package org.openengsb.ui.admin.ruleEditorPage;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.ruleEditorPanel.RuleEditorPanel;
import org.openengsb.ui.admin.ruleEditorPanel.RuleManagerProvider;

@AuthorizeInstantiation("ROLE_USER")
public class RuleEditorPage extends BasePage implements RuleManagerProvider {

    @SpringBean
    private RuleManager ruleManager;

    public RuleEditorPage() {
        add(new RuleEditorPanel("ruleEditor", this));
    }

    @Override
    public RuleManager getRuleManager() {
        return ruleManager;
    }
}
