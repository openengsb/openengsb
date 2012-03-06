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

package org.openengsb.ui.admin.ruleEditorPage;

import org.apache.wicket.PageParameters;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.ruleEditorPanel.RuleEditorPanel;
import org.openengsb.ui.admin.ruleEditorPanel.RuleManagerProvider;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "WORKFLOW_ADMIN")
@PaxWicketMountPoint(mountPoint = "rules")
public class RuleEditorPage extends BasePage implements RuleManagerProvider {

    @PaxWicketBean(name = "ruleManager")
    private RuleManager ruleManager;

    public RuleEditorPage() {
        initContent();
    }

    public RuleEditorPage(PageParameters parameters) {
        super(parameters);
        initContent();
    }

    private void initContent() {
        add(new RuleEditorPanel("ruleEditor", this));
    }

    @Override
    public RuleManager getRuleManager() {
        return ruleManager;
    }

}
