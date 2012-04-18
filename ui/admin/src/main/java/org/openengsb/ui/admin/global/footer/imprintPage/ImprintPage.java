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

package org.openengsb.ui.admin.global.footer.imprintPage;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.common.imprint.ImprintPanel;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@PaxWicketMountPoint(mountPoint = "imprint")
public class ImprintPage extends BasePage {


    private static final long serialVersionUID = -5208568567882864747L;

    public ImprintPage() {
    }

    public ImprintPage() {
        initContent();
    }

    public ImprintPage(PageParameters parameters) {
        super(parameters);
        initContent();
    }

    private void initContent() {
        MyImprintPanel imprintPanel = new MyImprintPanel("imprintInclude");
        add(imprintPanel);
    }
}
