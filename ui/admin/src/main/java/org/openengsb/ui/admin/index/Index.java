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

package org.openengsb.ui.admin.index;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.ui.admin.basePage.BasePage;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@PaxWicketMountPoint(mountPoint = "index")
@SecurityAttribute(key="org.openengsb.ui.component", value="INDEX")
public class Index extends BasePage {

    private static final long serialVersionUID = -445277092895685296L;

    public static final String PAGE_NAME_KEY = "dashboard.title";
    public static final String PAGE_DESCRIPTION_KEY = "dashboard.description";

    public Index() {
        super(PAGE_NAME_KEY);
    }

    public Index(PageParameters parameters) {
        super(parameters, PAGE_NAME_KEY);
    }
}
