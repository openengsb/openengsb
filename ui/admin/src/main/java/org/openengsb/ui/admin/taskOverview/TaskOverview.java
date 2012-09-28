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

package org.openengsb.ui.admin.taskOverview;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.common.taskbox.WebTaskboxService;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@SecurityAttribute(key = "org.openengsb.ui.component", value = "TASK_USER")
@PaxWicketMountPoint(mountPoint = "tasks")
public class TaskOverview extends BasePage {

    private static final long serialVersionUID = -2538133590170315082L;

    public static final String PAGE_NAME_KEY = "taskOverview.title";
    public static final String PAGE_DESCRIPTION_KEY = "taskOverview.description";

    @PaxWicketBean(name = "webtaskboxService")
    private WebTaskboxService taskboxService;

    public TaskOverview() {
        initContent();
    }

    public TaskOverview(PageParameters parameters) {
        super(parameters, PAGE_NAME_KEY);
        initContent();
    }

    private void initContent() {
        Panel p = taskboxService.getOverviewPanel();
        this.add(p);
    }

}
