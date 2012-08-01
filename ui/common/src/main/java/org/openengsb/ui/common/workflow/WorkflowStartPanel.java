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
package org.openengsb.ui.common.workflow;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.ops4j.pax.wicket.api.PaxWicketBean;

import com.google.common.collect.Lists;

public class WorkflowStartPanel extends Panel {

    private static final long serialVersionUID = -1610335004844921829L;

    @PaxWicketBean(name = "ruleManager")
    private RuleManager ruleManager;

    @PaxWicketBean(name = "workflowService")
    private WorkflowService workflowService;

    private Model<RuleBaseElementId> selectedFlowModel;

    private FeedbackPanel feedbackPanel;

    public WorkflowStartPanel(String id) {
        super(id);
        initContent();
    }

    public class WorkflowListModel extends LoadableDetachableModel<List<RuleBaseElementId>> {

        private static final long serialVersionUID = 4317594330367479098L;

        @Override
        public void detach() {
        }

        @Override
        protected List<RuleBaseElementId> load() {
            return Lists.newArrayList(ruleManager.list(RuleBaseElementType.Process));
        }
    }

    private void initContent() {
        Form<Object> form = new Form<Object>("startFlowForm");
        add(form);
        selectedFlowModel = new Model<RuleBaseElementId>();
        form.add(new DropDownChoice<RuleBaseElementId>("startFlowBox", selectedFlowModel, new WorkflowListModel()));
        form.add(new AjaxSubmitLink("startFlowButton") {

            private static final long serialVersionUID = 2613897821526291323L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                RuleBaseElementId flow = selectedFlowModel.getObject();
                long startFlow = workflowService.startFlow(flow.getName());
                info("workflow started with id " + startFlow);
                target.add(feedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        });
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);
    }

}
